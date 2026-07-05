<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">产品管理</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/product/create')">创建产品</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadProducts">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="产品编码/名称" clearable :prefix-icon="Search" @clear="loadProducts" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterProductType" placeholder="产品类型" clearable style="width: 140px" @change="loadProducts">
            <el-option label="寿险" :value="1" />
            <el-option label="意外险" :value="2" />
            <el-option label="健康险" :value="3" />
            <el-option label="车险" :value="4" />
            <el-option label="财产险" :value="5" />
            <el-option label="责任险" :value="6" />
            <el-option label="乘务险" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterVersionStatus" placeholder="版本状态" clearable style="width: 130px" @change="loadProducts">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已冻结" :value="2" />
            <el-option label="已驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 产品表格 -->
    <el-card>
      <el-table :data="products" stripe border v-loading="loading">
        <el-table-column prop="productCode" label="产品编码" width="130" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="productType" label="产品类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.productType]?.type || 'info'" size="small">
              {{ typeTagMap[row.productType]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="versionStatus" label="版本状态" width="100">
          <template #default="{ row }">
            <el-tag :type="versionStatusMap[row.versionStatus]?.type || 'info'" size="small">
              {{ versionStatusMap[row.versionStatus]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/product/${row.id}`)">详情</el-button>
            <el-button v-if="row.versionStatus === 0 || row.versionStatus === 3" size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="info" link @click="handleClone(row)">克隆</el-button>
            <el-button v-if="row.versionStatus === 0" size="small" type="success" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.versionStatus === 1" size="small" type="warning" link @click="handleFreeze(row)">冻结</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </el-card>

    <!-- 编辑产品弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑产品" width="550px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="产品编码" prop="productCode">
          <el-input v-model="editForm.productCode" disabled />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="editForm.productName" />
        </el-form-item>
        <el-form-item label="简称">
          <el-input v-model="editForm.shortName" />
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-select v-model="editForm.productType" style="width: 100%">
            <el-option label="寿险" :value="1" />
            <el-option label="意外险" :value="2" />
            <el-option label="健康险" :value="3" />
            <el-option label="车险" :value="4" />
            <el-option label="财产险" :value="5" />
            <el-option label="责任险" :value="6" />
            <el-option label="乘务险" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="editForm.version" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getProductPage, updateProduct, cloneProduct, publishProduct, freezeProduct } from '@/api/product'
import type { Product } from '@/types'

const loading = ref(false)
const products = ref<Product[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterProductType = ref<number | null>(null)
const filterVersionStatus = ref<number | null>(null)

const typeTagMap: Record<number, { label: string; type: string }> = {
  1: { label: '寿险', type: 'primary' },
  2: { label: '意外险', type: 'warning' },
  3: { label: '健康险', type: 'success' },
  4: { label: '车险', type: 'danger' },
  5: { label: '财产险', type: 'info' },
  6: { label: '责任险', type: '' },
  7: { label: '乘务险', type: 'warning' },
}

const versionStatusMap: Record<number, { label: string; type: string }> = {
  0: { label: '草稿', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已冻结', type: 'danger' },
  3: { label: '已驳回', type: 'warning' },
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const editForm = ref({ productCode: '', productName: '', shortName: '', productType: 1, version: '' })
const editRules: FormRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
}

async function loadProducts() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterProductType.value !== null) params.productType = filterProductType.value
    if (filterVersionStatus.value !== null) params.versionStatus = filterVersionStatus.value
    const res = await getProductPage(params)
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchKeyword.value = ''
  filterProductType.value = null
  filterVersionStatus.value = null
  currentPage.value = 1
  loadProducts()
}

function openEditDialog(row: Product) {
  editingId.value = row.id
  editForm.value = {
    productCode: row.productCode,
    productName: row.productName,
    shortName: row.shortName || '',
    productType: row.productType,
    version: row.version,
  }
  editDialogVisible.value = true
}

async function handleEditSubmit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!editingId.value) return
  editLoading.value = true
  try {
    await updateProduct(editingId.value, editForm.value)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadProducts()
  } catch { /* handled */ } finally {
    editLoading.value = false
  }
}

async function handleClone(row: Product) {
  try {
    await ElMessageBox.confirm(`确定克隆产品「${row.productName}」吗？`, '确认克隆', { type: 'info' })
    await cloneProduct(row.id)
    ElMessage.success('克隆成功')
    await loadProducts()
  } catch { /* cancel */ }
}

async function handlePublish(row: Product) {
  try {
    await ElMessageBox.confirm(`确定发布产品「${row.productName}」吗？发布后不可编辑。`, '确认发布', { type: 'warning' })
    await publishProduct(row.id)
    ElMessage.success('发布成功')
    await loadProducts()
  } catch { /* cancel */ }
}

async function handleFreeze(row: Product) {
  try {
    await ElMessageBox.confirm(`确定冻结产品「${row.productName}」吗？`, '确认冻结', { type: 'warning' })
    await freezeProduct(row.id)
    ElMessage.success('冻结成功')
    await loadProducts()
  } catch { /* cancel */ }
}

onMounted(() => { loadProducts() })
</script>
