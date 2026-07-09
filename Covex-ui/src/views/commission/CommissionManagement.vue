<template>
  <div>
    <h2 style="margin: 0 0 16px">佣金管理</h2>

    <!-- 筛选栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadCommissions">
        <el-form-item label="渠道">
          <el-select v-model="filterChannelId" placeholder="全部渠道" clearable filterable style="width: 180px" @change="loadCommissions">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="filterMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 140px"
            @change="loadCommissions"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px" @change="loadCommissions">
            <el-option label="待结算" :value="0" />
            <el-option label="已结算" :value="1" />
            <el-option label="已支付" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCommissions">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; gap: 12px; flex-wrap: wrap">
        <el-button type="primary" @click="calculateDialogVisible = true">计算佣金</el-button>
        <el-button type="success" @click="settleDialogVisible = true">月度结算</el-button>
        <el-button type="info" @click="summaryDialogVisible = true">月度统计</el-button>
      </div>
    </el-card>

    <!-- 佣金列表 -->
    <el-card>
      <el-table :data="commissions" stripe border v-loading="loading">
        <el-table-column prop="commissionNo" label="佣金编号" width="160" />
        <el-table-column prop="channelId" label="渠道ID" width="90" align="center" />
        <el-table-column prop="channelUserId" label="渠道用户ID" width="110" align="center">
          <template #default="{ row }">{{ row.channelUserId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="policyId" label="保单ID" width="90" align="center" />
        <el-table-column prop="commissionType" label="佣金类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.commissionType === 1 ? 'primary' : 'success'" size="small">
              {{ row.commissionType === 1 ? '首期' : '续期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="premiumAmount" label="保费金额" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.premiumAmount) }}</template>
        </el-table-column>
        <el-table-column prop="commissionRate" label="佣金费率" width="100" align="right">
          <template #default="{ row }">{{ (row.commissionRate * 100).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="commissionAmount" label="佣金金额" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.commissionAmount) }}</template>
        </el-table-column>
        <el-table-column prop="settleMonth" label="结算月份" width="100" />
        <el-table-column prop="settleStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.settleStatus)" size="small">
              {{ getStatusLabel(row.settleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settledAt" label="结算时间" width="170">
          <template #default="{ row }">{{ row.settledAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.settleStatus === 2"
              size="small"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              确认支付
            </el-button>
            <el-button
              v-if="row.settleStatus === 2"
              size="small"
              type="danger"
              link
              @click="handleReject(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && commissions.length === 0" description="暂无佣金记录" :image-size="60" />
    </el-card>

    <!-- 计算佣金弹窗 -->
    <el-dialog v-model="calculateDialogVisible" title="计算佣金" width="500px" destroy-on-close>
      <el-form ref="calcFormRef" :model="calcForm" :rules="calcRules" label-width="110px">
        <el-form-item label="保单ID" prop="policyId">
          <el-input-number v-model="calcForm.policyId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="渠道" prop="channelId">
          <el-select v-model="calcForm.channelId" placeholder="选择渠道" filterable style="width: 100%">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="渠道用户ID">
          <el-input-number v-model="calcForm.channelUserId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保费金额" prop="premiumAmount">
          <el-input-number v-model="calcForm.premiumAmount" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="佣金类型" prop="commissionType">
          <el-select v-model="calcForm.commissionType" style="width: 100%">
            <el-option label="首期佣金" :value="1" />
            <el-option label="续期佣金" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="佣金费率" prop="commissionRate">
          <el-input-number v-model="calcForm.commissionRate" :min="0" :max="1" :precision="4" :step="0.01" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="calculateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="calcLoading" @click="handleCalculate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 月度结算弹窗 -->
    <el-dialog v-model="settleDialogVisible" title="月度结算" width="400px" destroy-on-close>
      <el-form ref="settleFormRef" :model="settleForm" :rules="settleRules" label-width="100px">
        <el-form-item label="结算月份" prop="yearMonth">
          <el-date-picker
            v-model="settleForm.yearMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="settleLoading" @click="handleSettle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 月度统计弹窗 -->
    <el-dialog v-model="summaryDialogVisible" title="月度统计" width="500px" destroy-on-close>
      <el-form ref="summaryFormRef" :model="summaryForm" :rules="summaryRules" label-width="100px">
        <el-form-item label="渠道" prop="channelId">
          <el-select v-model="summaryForm.channelId" placeholder="选择渠道" filterable style="width: 100%">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计月份" prop="yearMonth">
          <el-date-picker
            v-model="summaryForm.yearMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <el-descriptions v-if="summaryResult" :column="2" border style="margin-top: 16px">
        <el-descriptions-item label="总佣金">{{ formatMoney(summaryResult.totalCommission) }}</el-descriptions-item>
        <el-descriptions-item label="已结算">{{ summaryResult.settledCount }} 笔</el-descriptions-item>
        <el-descriptions-item label="未结算">{{ summaryResult.unsettledCount }} 笔</el-descriptions-item>
        <el-descriptions-item label="已支付">{{ summaryResult.paidCount }} 笔</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="summaryDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="summaryLoading" @click="handleSummary">查询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCommissionList, calculateCommission, settleCommission, getCommissionSummary, confirmCommission, rejectCommission } from '@/api/commission'
import { getChannelPage } from '@/api/channel'
import type { Commission, Channel } from '@/types'

const loading = ref(false)
const commissions = ref<Commission[]>([])
const channels = ref<Channel[]>([])

const filterChannelId = ref<number | undefined>(undefined)
const filterMonth = ref<string>('')
const filterStatus = ref<number | undefined>(undefined)

// 计算佣金
const calculateDialogVisible = ref(false)
const calcLoading = ref(false)
const calcFormRef = ref<FormInstance>()
const calcForm = ref({
  policyId: null as number | null,
  channelId: null as number | null,
  channelUserId: null as number | null,
  premiumAmount: 10000,
  commissionType: 1,
  commissionRate: 0.1,
})
const calcRules: FormRules = {
  policyId: [{ required: true, message: '请输入保单ID', trigger: 'blur' }],
  channelId: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  premiumAmount: [{ required: true, message: '请输入保费金额', trigger: 'blur' }],
  commissionType: [{ required: true, message: '请选择佣金类型', trigger: 'change' }],
  commissionRate: [{ required: true, message: '请输入佣金费率', trigger: 'blur' }],
}

// 月度结算
const settleDialogVisible = ref(false)
const settleLoading = ref(false)
const settleFormRef = ref<FormInstance>()
const settleForm = ref({ yearMonth: '' })
const settleRules: FormRules = {
  yearMonth: [{ required: true, message: '请选择结算月份', trigger: 'change' }],
}

// 月度统计
const summaryDialogVisible = ref(false)
const summaryLoading = ref(false)
const summaryFormRef = ref<FormInstance>()
const summaryForm = ref({ channelId: null as number | null, yearMonth: '' })
const summaryResult = ref<{ totalCommission: number; settledCount: number; unsettledCount: number; paidCount: number } | null>(null)
const summaryRules: FormRules = {
  channelId: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  yearMonth: [{ required: true, message: '请选择统计月份', trigger: 'change' }],
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStatusLabel(status: number): string {
  const map: Record<number, string> = { 1: '待结算', 2: '已确认', 3: '已支付', 4: '已驳回' }
  return map[status] || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

async function loadChannels() {
  try {
    const res = await getChannelPage({ page: 1, size: 999 })
    channels.value = res.data?.records || []
  } catch { /* handled */ }
}

async function loadCommissions() {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (filterChannelId.value !== undefined) params.channelId = filterChannelId.value
    if (filterMonth.value) params.month = filterMonth.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getCommissionList(params)
    commissions.value = res.data || []
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  filterChannelId.value = undefined
  filterMonth.value = ''
  filterStatus.value = undefined
  loadCommissions()
}

async function handleCalculate() {
  const valid = await calcFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!calcForm.value.policyId || !calcForm.value.channelId) return

  calcLoading.value = true
  try {
    await calculateCommission({
      policyId: calcForm.value.policyId,
      channelId: calcForm.value.channelId,
      channelUserId: calcForm.value.channelUserId,
      premiumAmount: calcForm.value.premiumAmount,
      commissionType: calcForm.value.commissionType,
      commissionRate: calcForm.value.commissionRate,
    })
    ElMessage.success('佣金计算成功')
    calculateDialogVisible.value = false
    await loadCommissions()
  } catch { /* handled */ } finally {
    calcLoading.value = false
  }
}

async function handleSettle() {
  const valid = await settleFormRef.value?.validate().catch(() => false)
  if (!valid) return

  settleLoading.value = true
  try {
    const res = await settleCommission({ yearMonth: settleForm.value.yearMonth })
    ElMessage.success(`结算完成，处理了 ${res.data?.settledCount || 0} 笔佣金，总金额：${formatMoney(res.data?.totalAmount)}`)
    settleDialogVisible.value = false
    await loadCommissions()
  } catch { /* handled */ } finally {
    settleLoading.value = false
  }
}

async function handleSummary() {
  const valid = await summaryFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!summaryForm.value.channelId) return

  summaryLoading.value = true
  try {
    const res = await getCommissionSummary({
      channelId: summaryForm.value.channelId,
      yearMonth: summaryForm.value.yearMonth,
    })
    summaryResult.value = res.data
  } catch { /* handled */ } finally {
    summaryLoading.value = false
  }
}

async function handleConfirm(row: Commission) {
  try {
    await ElMessageBox.confirm('确定确认支付此佣金吗？', '确认', { type: 'info' })
    await confirmCommission(row.id)
    ElMessage.success('确认支付成功')
    await loadCommissions()
  } catch { /* cancel or handled */ }
}

async function handleReject(row: Commission) {
  try {
    await ElMessageBox.confirm('确定驳回此佣金吗？', '确认驳回', { type: 'warning' })
    await rejectCommission(row.id)
    ElMessage.success('已驳回')
    await loadCommissions()
  } catch { /* cancel or handled */ }
}

onMounted(async () => {
  await Promise.all([loadChannels(), loadCommissions()])
})
</script>
