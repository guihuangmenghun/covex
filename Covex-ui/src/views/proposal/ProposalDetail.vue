<template>
  <div>
    <el-page-header @back="$router.push('/proposal')" title="返回" content="投保单详情" />

    <el-card style="margin-top: 20px" v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 500">{{ proposal.proposalNo }}</span>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-tag :type="getStatusTagType(proposal.status)" size="large">
              {{ getStatusLabel(proposal.status) }}
            </el-tag>
            <el-button v-if="proposal.status === 1" type="success" @click="handleSubmit">提交投保单</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品名称">
          {{ proposal.productSnapshot?.productName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="产品编码">
          {{ proposal.productSnapshot?.productCode || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="投保人">{{ proposal.applicantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="被保人">{{ proposal.insuredName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总保额">{{ formatMoney(proposal.totalSumInsured) }}</el-descriptions-item>
        <el-descriptions-item label="总保费">
          {{ proposal.totalPremium != null ? formatMoney(proposal.totalPremium) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="渠道商">{{ proposal.channelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="渠道用户ID">{{ proposal.channelUserId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ proposal.submitAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ proposal.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ proposal.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ proposal.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 产品快照 -->
    <el-card v-if="proposal.productSnapshot" style="margin-top: 16px">
      <template #header>
        <span style="font-weight: 500">产品快照</span>
      </template>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="产品编码">{{ proposal.productSnapshot.productCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ proposal.productSnapshot.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简称">{{ proposal.productSnapshot.shortName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">{{ productTypeLabel(proposal.productSnapshot.productType) }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ proposal.productSnapshot.version || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品性质">{{ productNatureLabel(proposal.productSnapshot.productNature) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 选择保障 -->
    <el-card v-if="proposal.selectedCoverages" style="margin-top: 16px">
      <template #header>
        <span style="font-weight: 500">选择保障</span>
      </template>
      <pre style="font-size: 12px; background: #f5f7fa; padding: 12px; border-radius: 4px; overflow-x: auto">{{ JSON.stringify(proposal.selectedCoverages, null, 2) }}</pre>
    </el-card>

    <!-- 缴费计划 -->
    <el-card v-if="proposal.selectedPremiumPlan" style="margin-top: 16px">
      <template #header>
        <span style="font-weight: 500">缴费计划</span>
      </template>
      <pre style="font-size: 12px; background: #f5f7fa; padding: 12px; border-radius: 4px; overflow-x: auto">{{ JSON.stringify(proposal.selectedPremiumPlan, null, 2) }}</pre>
    </el-card>

    <!-- 健康声明 -->
    <el-card v-if="proposal.healthDeclaration" style="margin-top: 16px">
      <template #header>
        <span style="font-weight: 500">健康声明</span>
      </template>
      <pre style="font-size: 12px; background: #f5f7fa; padding: 12px; border-radius: 4px; overflow-x: auto">{{ JSON.stringify(proposal.healthDeclaration, null, 2) }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProposalById, submitProposal } from '@/api/proposal'
import type { Proposal } from '@/types'
import { useDictStore } from '@/stores/dict'

const route = useRoute()
const proposalId = Number(route.params.id)

const loading = ref(false)
const proposal = ref<Proposal>({} as Proposal)

const statusOptions = [
  { value: 1, label: '待校验', type: 'info' },
  { value: 2, label: '待核保', type: 'warning' },
  { value: 3, label: '核保中', type: 'warning' },
  { value: 4, label: '待支付', type: 'warning' },
  { value: 5, label: '已支付', type: 'success' },
  { value: 6, label: '已出单', type: 'success' },
  { value: 7, label: '已拒保', type: 'danger' },
  { value: 8, label: '已撤销', type: 'info' },
]

function getStatusLabel(status: number): string {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

function getStatusTagType(status: number): string {
  return statusOptions.find(s => s.value === status)?.type || 'info'
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function productTypeLabel(type: number): string {
  return useDictStore().getDictLabel('product_type', String(type))
}

function productNatureLabel(nature: number | null | undefined): string {
  if (nature == null) return '-'
  return useDictStore().getDictLabel('product_nature', String(nature))
}

async function loadProposal() {
  loading.value = true
  try {
    const res = await getProposalById(proposalId)
    proposal.value = res.data || ({} as Proposal)
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('确定提交此投保单吗？提交后将触发校验和核保流程。', '确认提交', { type: 'warning' })
    const res = await submitProposal(proposalId)
    ElMessage.success('提交成功')
    proposal.value = res.data || proposal.value
    await loadProposal()
  } catch { /* cancel or handled */ }
}

onMounted(() => { loadProposal() })
</script>
