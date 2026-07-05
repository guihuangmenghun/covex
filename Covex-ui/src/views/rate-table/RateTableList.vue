<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">费率表管理</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/rate-table/create')">创建费率表</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadRateTables">
        <el-form-item label="产品">
          <el-select v-model="filterProductId" placeholder="全部产品" clearable filterable style="width: 200px" @change="loadRateTables">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRateTables">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 费率表列表 -->
    <el-card>
      <el-table :data="rateTables" stripe border v-loading="loading">
        <el-table-column prop="rateTableCode" label="表编码" width="150" />
        <el-table-column prop="rateTableName" label="表名称" min-width="180" />
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="effectiveDate" label="生效日期" width="120">
          <template #default="{ row }">{{ row.effectiveDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="失效日期" width="120">
          <template #default="{ row }">{{ row.expiryDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/rate-table/${row.id}`)">详情</el-button>
            <el-button size="small" type="success" link @click="handleLoadToRedis(row)">加载到 Redis</el-button>
            <el-button size="small" type="warning" link @click="handleEvictRedis(row)">清除缓存</el-button>
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
          @size-change="loadRateTables"
          @current-change="loadRateTables"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRateTablePage, loadRateTable, evictRateTable } from '@/api/rateTable'
import { getProductList } from '@/api/product'
import type { RateTable, Product } from '@/types'

const loading = ref(false)
const rateTables = ref<RateTable[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const filterProductId = ref<number | null>(null)
const productList = ref<Product[]>([])

async function loadProductList() {
  try {
    const res = await getProductList()
    productList.value = res.data?.records || []
  } catch { /* handled */ }
}

async function loadRateTables() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (filterProductId.value !== null) params.productId = filterProductId.value
    const res = await getRateTablePage(params)
    rateTables.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetSearch() {
  filterProductId.value = null
  currentPage.value = 1
  loadRateTables()
}

async function handleLoadToRedis(row: RateTable) {
  try {
    await ElMessageBox.confirm(
      `确定将费率表「${row.rateTableName}」(${row.rateTableCode} v${row.version}) 加载到 Redis 缓存吗？`,
      '确认加载',
      { type: 'info' }
    )
    await loadRateTable({ tableCode: row.rateTableCode, version: row.version })
    ElMessage.success('已加载到 Redis')
  } catch { /* cancel */ }
}

async function handleEvictRedis(row: RateTable) {
  try {
    await ElMessageBox.confirm(
      `确定清除费率表「${row.rateTableName}」(${row.rateTableCode} v${row.version}) 的 Redis 缓存吗？`,
      '确认清除',
      { type: 'warning' }
    )
    await evictRateTable({ tableCode: row.rateTableCode, version: row.version })
    ElMessage.success('缓存已清除')
  } catch { /* cancel */ }
}

onMounted(async () => {
  await loadProductList()
  await loadRateTables()
})
</script>
