<template>
  <div>
    <el-page-header @back="$router.push('/policy')" title="返回" content="保单详情" />

    <!-- 基本信息卡片 -->
    <el-card style="margin-top: 20px" v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 500">{{ policy.policyNo }}</span>
          <el-tag :type="getStatusTagType(policy.status)" size="large">
            {{ getStatusLabel(policy.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="产品名称">{{ policy.productSnapshot?.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ policy.productSnapshot?.productCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">{{ productTypeLabel(policy.productSnapshot?.productType) }}</el-descriptions-item>
        <el-descriptions-item label="投保人ID">{{ policy.applicantId }}</el-descriptions-item>
        <el-descriptions-item label="被保人ID">{{ policy.insuredId }}</el-descriptions-item>
        <el-descriptions-item label="渠道商ID">{{ policy.channelId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总保额">{{ formatMoney(policy.totalSumInsured) }}</el-descriptions-item>
        <el-descriptions-item label="总保费">{{ formatMoney(policy.totalPremium) }}</el-descriptions-item>
        <el-descriptions-item label="缴费方式">{{ paymentModeLabel(policy.paymentMode) }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ policy.effectiveDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ policy.expiryDate || '终身' }}</el-descriptions-item>
        <el-descriptions-item label="投保单ID">{{ policy.proposalId }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions v-if="policy.terminationReason" :column="2" border style="margin-top: 12px">
        <el-descriptions-item label="终止原因">{{ terminationReasonLabel(policy.terminationReason) }}</el-descriptions-item>
        <el-descriptions-item label="终止时间">{{ policy.terminatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 险种明细 + 缴费计划 Tab -->
    <el-card style="margin-top: 16px">
      <el-tabs v-model="activeTab">
        <!-- 险种明细 -->
        <el-tab-pane label="险种明细" name="coverage">
          <el-table :data="coverages" stripe border size="small">
            <el-table-column prop="coverageCode" label="保障编码" width="130" />
            <el-table-column prop="coverageName" label="保障名称" min-width="160" />
            <el-table-column prop="sumInsured" label="保额" width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.sumInsured) }}</template>
            </el-table-column>
            <el-table-column prop="premium" label="保费" width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.premium) }}</template>
            </el-table-column>
            <el-table-column prop="deductible" label="免赔额" width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.deductible) }}</template>
            </el-table-column>
            <el-table-column prop="cumulativePaid" label="累计赔付" width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.cumulativePaid) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '有效' : '已终止' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="coverages.length === 0" description="暂无险种明细" :image-size="60" />
        </el-tab-pane>

        <!-- 缴费计划 -->
        <el-tab-pane label="缴费计划" name="premium">
          <el-table :data="premiums" stripe border size="small">
            <el-table-column prop="premiumPlanCode" label="计划编码" width="130" />
            <el-table-column prop="paymentFrequency" label="缴费频率" width="100" align="center">
              <template #default="{ row }">{{ frequencyLabel(row.paymentFrequency) }}</template>
            </el-table-column>
            <el-table-column prop="periodPremium" label="每期保费" width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.periodPremium) }}</template>
            </el-table-column>
            <el-table-column prop="totalPeriods" label="总期数" width="80" align="center" />
            <el-table-column prop="paidPeriods" label="已缴期数" width="90" align="center" />
            <el-table-column prop="nextDueDate" label="下期应缴日" width="120">
              <template #default="{ row }">{{ row.nextDueDate || '-' }}</template>
            </el-table-column>
            <el-table-column prop="paymentTerm" label="缴费期限" width="90" align="center" />
            <el-table-column prop="gracePeriod" label="宽限天数" width="90" align="center" />
          </el-table>
          <el-empty v-if="premiums.length === 0" description="暂无缴费计划" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPolicyById } from '@/api/policy'
import type { Policy, PolicyCoverage, PolicyPremium } from '@/types'
import { useDictStore } from '@/stores/dict'

const route = useRoute()
const policyId = Number(route.params.id)

const loading = ref(false)
const policy = ref<Policy>({} as Policy)
const coverages = ref<PolicyCoverage[]>([])
const premiums = ref<PolicyPremium[]>([])
const activeTab = ref('coverage')

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStatusLabel(status: number): string {
  return useDictStore().getDictLabel('policy_status', String(status))
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'info' }
  return map[status] || 'info'
}

function productTypeLabel(type: number): string {
  return useDictStore().getDictLabel('product_type', String(type))
}

function paymentModeLabel(mode: number): string {
  return useDictStore().getDictLabel('payment_mode', String(mode))
}

function frequencyLabel(freq: number): string {
  return useDictStore().getDictLabel('payment_frequency', String(freq))
}

function terminationReasonLabel(reason: number): string {
  return useDictStore().getDictLabel('termination_reason', String(reason))
}

async function loadPolicy() {
  loading.value = true
  try {
    const res = await getPolicyById(policyId)
    const data = res.data || {} as any
    policy.value = data.policy || ({} as Policy)
    coverages.value = data.coverages || []
    premiums.value = data.premiums || []
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

onMounted(() => { loadPolicy() })
</script>
