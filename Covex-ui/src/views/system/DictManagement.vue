<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">数据字典管理</h2>
      <div>
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增字典项</el-button>
        <el-button type="warning" @click="handleEvictCache">清空缓存</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 左侧：字典类型列表 -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <el-input
              v-model="typeSearch"
              placeholder="搜索字典类型"
              :prefix-icon="Search"
              clearable
              size="small"
            />
          </template>
          <el-scrollbar height="600px">
            <el-menu
              :default-active="activeType"
              @select="handleTypeSelect"
              style="border-right: none"
            >
              <el-menu-item
                v-for="t in filteredTypes"
                :key="t"
                :index="t"
                style="height: 40px; line-height: 40px"
              >
                <span>{{ t }}</span>
              </el-menu-item>
            </el-menu>
          </el-scrollbar>
        </el-card>
      </el-col>

      <!-- 右侧：字典项列表 -->
      <el-col :span="18">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">{{ activeType || '请选择字典类型' }}</span>
              <el-button
                v-if="activeType"
                type="primary"
                size="small"
                :icon="Plus"
                @click="openAddDialog"
              >
                新增
              </el-button>
            </div>
          </template>

          <el-table :data="treeDictItems" row-key="id" :tree-props="{ children: 'children' }" stripe border v-loading="tableLoading" empty-text="请选择左侧字典类型" default-expand-all>
            <el-table-column prop="dictCode" label="字典编码" width="150" />
            <el-table-column prop="dictName" label="字典标签" />
            <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
            <el-table-column prop="parentCode" label="父编码" width="120">
              <template #default="{ row }">
                {{ row.parentCode || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注">
              <template #default="{ row }">
                {{ row.remark || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑字典项' : '新增字典项'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" :disabled="isEdit" placeholder="如 product_type" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="form.dictCode" placeholder="如 1" />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictName">
          <el-input v-model="form.dictName" placeholder="如 寿险" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="父编码" prop="parentCode">
          <el-input v-model="form.parentCode" placeholder="无父级留空" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAllDicts,
  getDictByType,
  createDict,
  updateDict,
  deleteDict,
  evictDictCache,
} from '@/api/dict'
import type { DictItem } from '@/types'

// ====== 状态 ======
const typeSearch = ref('')
const activeType = ref('')
const allDictData = ref<Record<string, DictItem[]>>({})
const dictItems = ref<DictItem[]>([])
const tableLoading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = ref({
  dictType: '',
  dictCode: '',
  dictName: '',
  sortOrder: 0,
  parentCode: '',
  remark: '',
})

const formRules: FormRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }],
}

// ====== 计算 ======
const dictTypes = computed(() => Object.keys(allDictData.value).sort())
const filteredTypes = computed(() => {
  if (!typeSearch.value) return dictTypes.value
  const keyword = typeSearch.value.toLowerCase()
  return dictTypes.value.filter((t) => t.toLowerCase().includes(keyword))
})

/** 将平铺列表转为树形结构（基于 parentCode） */
const treeDictItems = computed(() => {
  const items = dictItems.value
  if (!items.length) return []

  // 检测是否有层级关系
  const hasParent = items.some((i) => i.parentCode)
  if (!hasParent) return items

  // 构建 code -> item 映射
  const map = new Map<string, DictItem & { children?: DictItem[] }>()
  const roots: (DictItem & { children?: DictItem[] })[] = []

  for (const item of items) {
    map.set(item.dictCode, { ...item, children: [] })
  }

  for (const item of items) {
    const node = map.get(item.dictCode)!
    if (item.parentCode && map.has(item.parentCode)) {
      map.get(item.parentCode)!.children!.push(node)
    } else {
      roots.push(node)
    }
  }

  // 移除空 children 数组
  for (const node of map.values()) {
    if (node.children && node.children.length === 0) {
      delete node.children
    }
  }

  return roots
})

// ====== 方法 ======
async function loadAllDicts() {
  try {
    const res = await getAllDicts()
    allDictData.value = res.data || {}
  } catch {
    // 错误已在拦截器处理
  }
}

async function loadDictItems(type: string) {
  tableLoading.value = true
  try {
    const res = await getDictByType(type)
    dictItems.value = (res.data || []).sort((a, b) => a.sortOrder - b.sortOrder)
  } catch {
    dictItems.value = []
  } finally {
    tableLoading.value = false
  }
}

function handleTypeSelect(type: string) {
  activeType.value = type
  loadDictItems(type)
}

function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  form.value = {
    dictType: activeType.value,
    dictCode: '',
    dictName: '',
    sortOrder: 0,
    parentCode: '',
    remark: '',
  }
  dialogVisible.value = true
}

function openEditDialog(row: DictItem) {
  isEdit.value = true
  editingId.value = row.id
  form.value = {
    dictType: row.dictType,
    dictCode: row.dictCode,
    dictName: row.dictName,
    sortOrder: row.sortOrder,
    parentCode: row.parentCode || '',
    remark: row.remark || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      ...form.value,
      parentCode: form.value.parentCode || null,
      remark: form.value.remark || null,
    }
    if (isEdit.value && editingId.value) {
      await updateDict(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createDict(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    // 刷新数据
    await loadAllDicts()
    if (activeType.value) {
      await loadDictItems(activeType.value)
    }
  } catch {
    // 错误已在拦截器处理
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: DictItem) {
  try {
    await ElMessageBox.confirm(`确定删除字典项「${row.dictName}」(${row.dictCode}) 吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    await loadAllDicts()
    if (activeType.value) {
      await loadDictItems(activeType.value)
    }
  } catch {
    // 取消或错误已在拦截器处理
  }
}

async function handleEvictCache() {
  try {
    await ElMessageBox.confirm('确定清空所有字典缓存吗？', '确认', {
      type: 'warning',
    })
    await evictDictCache()
    ElMessage.success('缓存已清空')
  } catch {
    // 取消或错误已在拦截器处理
  }
}

// ====== 初始化 ======
onMounted(() => {
  loadAllDicts()
})
</script>
