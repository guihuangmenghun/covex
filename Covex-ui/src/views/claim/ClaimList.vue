<template>
  <div>
    <h2 style="margin: 0 0 16px">理赔管理</h2>

    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadClaims">
        <el-form-item>
          <el-input v-model="searchPolicyNo" placeholder="保单号" clearable style="width: 180px" @clear="loadClaims" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="loadClaims">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="searchHandler" placeholder="处理人" clearable style="width: 140px" @clear="loadClaims" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadClaims">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
        <el-button type="primary" @click="$router.push('/claim/create')">理赔报案</el-button>
      </div>

      <el-table :data="claims" stripe border v-loading="loading">
        <el-table-column prop="claimNo" label="理赔号" width="180" />
        <el-table-column prop="policyId" label="保单ID" width="90" align="center" />
        <el-table-column prop="accidentDate" label="出险日期" width="120" />
        <el-table-column prop="accidentType" label="出险原因" width="120" />
        <el-table-column prop="claimAmount" label="理赔金额" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.claimAmount) }}</template>
        </el-table-column>
        <el-table-column prop="approvedAmount" label="批准金额" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.approvedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="claimHandler" label="处理人" width="100">
          <template #default="{ row }">{{ row.claimHandler || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reportedAt" label="报案时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/claim/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="success" link @click="handleAssign(row)">分配</el-button>
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
          @size-change="loadClaims"
          @current-change="loadClaims"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClaimPage, assignClaim } from '@/api/claim'
import type { Claim } from '@/types'

const loading = ref(false)
const claims = ref<Claim[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchPolicyNo = ref('')
const filterStatus = ref<number | undefined>(undefined)
const searchHandler = ref('')

const statusOptions = [
  { value: 1, label: '已报案' },
  { value: 2, label: '已分配' },
  { value: 3, label: '审核中' },
  { value: 4, label: '调查中' },
  { value: 5, label: '待赔付' },
  { value: 6, label: '已赔付' },
  { value: 7, label: '已结案' },
  { value: 8, label: '已拒赔' },
  { value: 9, label: '申诉中' },
]

function getStatusLabel(status: number): string {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = {
    1: 'info', 2: 'warning', 3: 'warning', 4: 'warning',
    5: 'warning', 6: 'success', 7: 'info', 8: 'danger', 9: 'warning',
  }
  return map[status] || 'info'
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadClaims() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchPolicyNo.value) params.policyNo = searchPolicyNo.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    if (searchHandler.value) params.handler = searchHandler.value
    const res = await getClaimPage(params)
    claims.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchPolicyNo.value = ''
  filterStatus.value = undefined
  searchHandler.value = ''
  currentPage.value = 1
  loadClaims()
}

async function handleAssign(row: Claim) {
  try {
    await ElMessageBox.confirm('确定分配此理赔案件给当前用户吗？', '确认分配', { type: 'info' })
    await assignClaim(row.id)
    ElMessage.success('分配成功')
    await loadClaims()
  } catch { /* cancel or handled */ }
}

onMounted(() => { loadClaims() })
</script>
