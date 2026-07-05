<template>
  <div>
    <h2 style="margin: 0 0 20px">工作台</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/product')">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon :size="20"><Goods /></el-icon>
              <span>产品总数</span>
            </div>
          </template>
          <div class="stat-number" v-loading="statsLoading.product">{{ stats.productCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/policy')">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon :size="20"><Document /></el-icon>
              <span>有效保单</span>
            </div>
          </template>
          <div class="stat-number" v-loading="statsLoading.policy">{{ stats.policyCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/proposal')">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon :size="20"><EditPen /></el-icon>
              <span>待核保</span>
            </div>
          </template>
          <div class="stat-number" v-loading="statsLoading.proposal">{{ stats.pendingProposalCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/claim')">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-icon :size="20"><WarningFilled /></el-icon>
              <span>待理赔</span>
            </div>
          </template>
          <div class="stat-number" v-loading="statsLoading.claim">{{ stats.pendingClaimCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">待核保投保单</span>
              <el-button link type="primary" @click="$router.push('/proposal')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingProposals" stripe size="small" v-loading="todoLoading.proposals" empty-text="暂无待办">
            <el-table-column prop="proposalNo" label="投保单号" width="140" />
            <el-table-column prop="applicantName" label="投保人" min-width="100" />
            <el-table-column prop="totalPremium" label="保费" width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalPremium) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="$router.push(`/proposal/${row.id}`)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">待处理理赔</span>
              <el-button link type="primary" @click="$router.push('/claim')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingClaims" stripe size="small" v-loading="todoLoading.claims" empty-text="暂无待办">
            <el-table-column prop="claimNo" label="理赔号" width="140" />
            <el-table-column prop="policyId" label="保单ID" width="80" align="center" />
            <el-table-column prop="claimAmount" label="理赔金额" width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.claimAmount) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="$router.push(`/claim/${row.id}`)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 500">待支付投保单</span>
              <el-button link type="primary" @click="$router.push('/proposal')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingPayments" stripe size="small" v-loading="todoLoading.payments" empty-text="暂无待办">
            <el-table-column prop="proposalNo" label="投保单号" width="140" />
            <el-table-column prop="applicantName" label="投保人" min-width="100" />
            <el-table-column prop="totalPremium" label="保费" width="100" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalPremium) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="$router.push(`/proposal/${row.id}`)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作 -->
    <el-card>
      <template #header>
        <span style="font-weight: 500">最近操作</span>
      </template>
      <el-table :data="recentOps" stripe v-loading="recentLoading" empty-text="暂无记录">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '投保单' ? 'primary' : row.type === '理赔' ? 'warning' : 'info'" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="no" label="单号" width="160" />
        <el-table-column prop="action" label="操作" min-width="200" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Goods, Document, EditPen, WarningFilled } from '@element-plus/icons-vue'
import { getProductPage } from '@/api/product'
import { getPolicyPage } from '@/api/policy'
import { getProposalPage } from '@/api/proposal'
import { getClaimPage } from '@/api/claim'
import type { Proposal, Claim } from '@/types'

// 统计数据
const stats = reactive({
  productCount: 0,
  policyCount: 0,
  pendingProposalCount: 0,
  pendingClaimCount: 0,
})

const statsLoading = reactive({
  product: false,
  policy: false,
  proposal: false,
  claim: false,
})

// 待办事项
const pendingProposals = ref<Proposal[]>([])
const pendingClaims = ref<Claim[]>([])
const pendingPayments = ref<Proposal[]>([])

const todoLoading = reactive({
  proposals: false,
  claims: false,
  payments: false,
})

// 最近操作
const recentOps = ref<any[]>([])
const recentLoading = ref(false)

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getProposalStatusLabel(status: number): { label: string; type: string } {
  const map: Record<number, { label: string; type: string }> = {
    1: { label: '待校验', type: 'info' },
    2: { label: '待核保', type: 'warning' },
    3: { label: '核保中', type: 'warning' },
    4: { label: '待支付', type: 'warning' },
    5: { label: '已支付', type: 'success' },
    6: { label: '已出单', type: 'success' },
    7: { label: '已拒保', type: 'danger' },
    8: { label: '已撤销', type: 'info' },
  }
  return map[status] || { label: '未知', type: 'info' }
}

function getClaimStatusLabel(status: number): { label: string; type: string } {
  const map: Record<number, { label: string; type: string }> = {
    1: { label: '已报案', type: 'info' },
    2: { label: '已分配', type: 'warning' },
    3: { label: '审核中', type: 'warning' },
    4: { label: '调查中', type: 'warning' },
    5: { label: '待赔付', type: 'warning' },
    6: { label: '已赔付', type: 'success' },
    7: { label: '已结案', type: 'info' },
    8: { label: '已拒赔', type: 'danger' },
    9: { label: '申诉中', type: 'warning' },
  }
  return map[status] || { label: '未知', type: 'info' }
}

function getStatusTagType(status: number): string {
  return getProposalStatusLabel(status).type
}

// 加载统计数据
async function loadStats() {
  // 产品总数
  statsLoading.product = true
  try {
    const res = await getProductPage({ page: 1, size: 1 })
    stats.productCount = res.data?.total || 0
  } catch { /* handled */ } finally {
    statsLoading.product = false
  }

  // 有效保单数
  statsLoading.policy = true
  try {
    const res = await getPolicyPage({ page: 1, size: 1, status: 1 })
    stats.policyCount = res.data?.total || 0
  } catch { /* handled */ } finally {
    statsLoading.policy = false
  }

  // 待核保投保单数
  statsLoading.proposal = true
  try {
    const res = await getProposalPage({ page: 1, size: 1, status: 2 })
    stats.pendingProposalCount = res.data?.total || 0
  } catch { /* handled */ } finally {
    statsLoading.proposal = false
  }

  // 待处理理赔数
  statsLoading.claim = true
  try {
    const res = await getClaimPage({ page: 1, size: 1, status: 1 })
    stats.pendingClaimCount = res.data?.total || 0
  } catch { /* handled */ } finally {
    statsLoading.claim = false
  }
}

// 加载待办事项
async function loadTodos() {
  // 待核保投保单（status=2）
  todoLoading.proposals = true
  try {
    const res = await getProposalPage({ page: 1, size: 5, status: 2 })
    pendingProposals.value = res.data?.records || []
  } catch { /* handled */ } finally {
    todoLoading.proposals = false
  }

  // 待处理理赔（status=1 或 2）
  todoLoading.claims = true
  try {
    const res = await getClaimPage({ page: 1, size: 5, status: 1 })
    pendingClaims.value = res.data?.records || []
  } catch { /* handled */ } finally {
    todoLoading.claims = false
  }

  // 待支付投保单（status=4）
  todoLoading.payments = true
  try {
    const res = await getProposalPage({ page: 1, size: 5, status: 4 })
    pendingPayments.value = res.data?.records || []
  } catch { /* handled */ } finally {
    todoLoading.payments = false
  }
}

// 加载最近操作
async function loadRecentOps() {
  recentLoading.value = true
  try {
    // 获取最近的投保单和理赔
    const [proposalsRes, claimsRes] = await Promise.all([
      getProposalPage({ page: 1, size: 10 }),
      getClaimPage({ page: 1, size: 10 }),
    ])

    const proposals = proposalsRes.data?.records || []
    const claims = claimsRes.data?.records || []

    // 合并并按时间排序
    const ops: any[] = []

    proposals.forEach(p => {
      const statusInfo = getProposalStatusLabel(p.status)
      ops.push({
        time: p.createdAt,
        type: '投保单',
        no: p.proposalNo,
        action: `投保单 ${p.proposalNo}`,
        status: p.status,
        statusLabel: statusInfo.label,
      })
    })

    claims.forEach(c => {
      const statusInfo = getClaimStatusLabel(c.status)
      ops.push({
        time: c.reportedAt || c.createdAt,
        type: '理赔',
        no: c.claimNo,
        action: `理赔 ${c.claimNo}`,
        status: c.status,
        statusLabel: statusInfo.label,
      })
    })

    // 按时间倒序排序，取前 10 条
    ops.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
    recentOps.value = ops.slice(0, 10)
  } catch { /* handled */ } finally {
    recentLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadTodos(), loadRecentOps()])
})
</script>

<style scoped>
.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-number {
  font-size: 36px;
  font-weight: 600;
  text-align: center;
  color: #409eff;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
