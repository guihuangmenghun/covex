<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">权限管理</h2>
      <div>
        <el-button-group>
          <el-button :type="viewMode === 'group' ? 'primary' : 'default'" @click="viewMode = 'group'">模块分组</el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">列表视图</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 模块分组视图 -->
    <template v-if="viewMode === 'group'">
      <div v-loading="loading">
        <el-collapse v-model="expandedModules">
          <el-collapse-item v-for="(perms, module) in permissionModules" :key="module" :name="module">
            <template #title>
              <span style="font-weight: 500">{{ moduleLabelMap[module] || module }}</span>
              <el-tag size="small" type="info" style="margin-left: 8px">{{ perms.length }}</el-tag>
            </template>
            <el-table :data="perms" stripe size="small">
              <el-table-column prop="permissionCode" label="权限编码" width="200" />
            <el-table-column prop="permissionName" label="权限名称" width="160" />
            <el-table-column prop="action" label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="opTypeTag(row.action)">{{ opTypeLabel(row.action) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
        <el-empty v-if="Object.keys(permissionModules).length === 0 && !loading" description="暂无权限数据" />
      </div>
    </template>

    <!-- 列表视图 -->
    <template v-else>
      <el-card v-loading="loading">
        <el-table :data="allPermissions" stripe border>
          <el-table-column prop="permissionCode" label="权限编码" width="200" />
          <el-table-column prop="permissionName" label="权限名称" width="160" />
          <el-table-column prop="module" label="模块" width="140" />
          <el-table-column prop="action" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="opTypeTag(row.action)">{{ opTypeLabel(row.action) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 新建权限弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建权限" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" placeholder="如 product:view" />
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="如 查看产品" />
        </el-form-item>
        <el-form-item label="所属模块" prop="module">
          <el-select v-model="form.module" placeholder="选择模块" style="width: 100%" filterable allow-create>
            <el-option v-for="mod in moduleOptions" :key="mod" :label="mod" :value="mod" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型" prop="action">
          <el-select v-model="form.action" placeholder="选择操作类型" style="width: 100%">
            <el-option label="查看 (read)" value="read" />
            <el-option label="创建 (create)" value="create" />
            <el-option label="编辑 (update)" value="update" />
            <el-option label="删除 (delete)" value="delete" />
            <el-option label="审批 (approve)" value="approve" />
          </el-select>
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
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createPermission, getPermissionList, getPermissionModules } from '@/api/user'
import type { Permission } from '@/types'

const loading = ref(false)
const viewMode = ref<'group' | 'list'>('group')
const expandedModules = ref<string[]>([])
const allPermissions = ref<Permission[]>([])
const permissionModules = ref<Record<string, Permission[]>>({})

// ====== 表单 ======
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = ref({ permissionCode: '', permissionName: '', module: '', action: '' })
const formRules: FormRules = {
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  module: [{ required: true, message: '请选择模块', trigger: 'change' }],
  action: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
}

const moduleOptions = computed(() => Object.keys(permissionModules.value))

// 模块名称中文映射
const moduleLabelMap: Record<string, string> = {
  proposal: '投保单',
  product: '产品',
  role: '角色',
  channel: '渠道',
  claim: '理赔',
  commission: '佣金',
  customer: '客户',
  payment: '支付',
  policy: '保单',
  underwriting: '核保',
  user: '用户',
  permission: '权限',
  dict: '字典',
  system: '系统',
}

function opTypeLabel(type: string): string {
  const map: Record<string, string> = { view: '查看', read: '查看', create: '创建', edit: '编辑', update: '编辑', delete: '删除', approve: '审批', publish: '发布', assign_perm: '分配权限', assign_role: '分配角色', review: '审核', pay: '支付', settle: '结算' }
  return map[type] || type
}

function opTypeTag(type: string): string {
  const map: Record<string, string> = { view: 'info', read: 'info', create: 'success', edit: 'warning', update: 'warning', delete: 'danger', approve: 'primary', publish: 'success', assign_perm: 'primary', assign_role: 'primary', review: 'warning', pay: 'success', settle: 'success' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const [listRes, modulesRes] = await Promise.all([
      getPermissionList(),
      getPermissionModules(),
    ])
    allPermissions.value = listRes.data || []
    permissionModules.value = modulesRes.data || {}
    expandedModules.value = Object.keys(permissionModules.value)
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function openAddDialog() {
  form.value = { permissionCode: '', permissionName: '', module: '', action: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await createPermission(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    await loadData()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>
