<template>
  <div>
    <h2 style="margin: 0 0 16px">保单管理</h2>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadPolicies">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="保单号" clearable style="width: 180px" @clear="loadPolicies" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="loadPolicies">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPolicies">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 保单列表 -->
    <el-card>
      <el-table :data="policies" stripe border v-loading="loading">
        <el-table-column prop="policyNo" label="保单号" width="180" />
        <el-table-column label="产品" min-width="160">
          <template #default="{ row }">
            {{ row.productSnapshot?.productName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="投保人" min-width="100" align="center" />
        <el-table-column prop="insuredName" label="被保人" min-width="100" align="center" />
        <el-table-column prop="totalSumInsured" label="总保额" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalSumInsured) }}</template>
        </el-table-column>
        <el-table-column prop="totalPremium" label="总保费" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalPremium) }}</template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120">
          <template #default="{ row }">{{ row.effectiveDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="到期日期" width="120">
          <template #default="{ row }">{{ row.expiryDate || '终身' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/policy/${row.id}`)">详情</el-button>
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
          @size-change="loadPolicies"
          @current-change="loadPolicies"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPolicyPage } from '@/api/policy'
import type { Policy } from '@/types'

const loading = ref(false)
const policies = ref<Policy[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)

const statusOptions = [
  { value: 1, label: '有效' },
  { value: 2, label: '中止' },
  { value: 3, label: '终止' },
]

function getStatusLabel(status: number): string {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'info' }
  return map[status] || 'info'
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadPolicies() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getPolicyPage(params)
    policies.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = undefined
  currentPage.value = 1
  loadPolicies()
}

onMounted(() => { loadPolicies() })
</script>
