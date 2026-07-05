<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">投保单管理</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/proposal/create')">新建投保单</el-button>
    </div>

    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadProposals">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="投保单号" clearable style="width: 160px" @clear="loadProposals" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="loadProposals">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterChannelId" placeholder="渠道" clearable filterable style="width: 160px" @change="loadProposals">
            <el-option v-for="c in channels" :key="c.id" :label="c.channelName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProposals">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="proposals" stripe border v-loading="loading">
        <el-table-column prop="proposalNo" label="投保单号" width="180" />
        <el-table-column label="产品" min-width="140">
          <template #default="{ row }">
            {{ row.productSnapshot?.productName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantId" label="投保人ID" width="100" align="center" />
        <el-table-column prop="totalSumInsured" label="总保额" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.totalSumInsured) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPremium" label="总保费" width="120" align="right">
          <template #default="{ row }">
            {{ row.totalPremium != null ? formatMoney(row.totalPremium) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/proposal/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="success" link @click="handleSubmit(row)">提交</el-button>
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
          @size-change="loadProposals"
          @current-change="loadProposals"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProposalPage, submitProposal } from '@/api/proposal'
import { getChannelPage } from '@/api/channel'
import type { Proposal, Channel } from '@/types'

const loading = ref(false)
const proposals = ref<Proposal[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const filterChannelId = ref<number | undefined>(undefined)
const channels = ref<Channel[]>([])

const statusOptions = [
  { value: 1, label: '待校验' },
  { value: 2, label: '待核保' },
  { value: 3, label: '核保中' },
  { value: 4, label: '待支付' },
  { value: 5, label: '已支付' },
  { value: 6, label: '已出单' },
  { value: 7, label: '已拒保' },
  { value: 8, label: '已撤销' },
]

function getStatusLabel(status: number): string {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'warning', 4: 'warning', 5: 'success', 6: 'success', 7: 'danger', 8: 'info' }
  return map[status] || 'info'
}

function formatMoney(val: number | null): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadChannels() {
  try {
    const res = await getChannelPage({ page: 1, size: 999 })
    channels.value = res.data?.records || []
  } catch { /* handled */ }
}

async function loadProposals() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    if (filterChannelId.value !== undefined) params.channelId = filterChannelId.value
    const res = await getProposalPage(params)
    proposals.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = undefined
  filterChannelId.value = undefined
  currentPage.value = 1
  loadProposals()
}

async function handleSubmit(row: Proposal) {
  try {
    await ElMessageBox.confirm('确定提交此投保单吗？提交后将触发校验和核保流程。', '确认提交', { type: 'warning' })
    await submitProposal(row.id)
    ElMessage.success('提交成功')
    await loadProposals()
  } catch { /* cancel or handled */ }
}

onMounted(async () => {
  await loadChannels()
  await loadProposals()
})
</script>
