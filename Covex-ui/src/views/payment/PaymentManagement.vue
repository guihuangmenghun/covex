<template>
  <div>
    <h2 style="margin: 0 0 16px">支付管理</h2>

    <!-- 查询区 + 操作区 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="handleQuery">
        <el-form-item label="投保单ID">
          <el-input v-model="proposalId" placeholder="输入投保单ID" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="queryLoading" @click="handleQuery">查询支付记录</el-button>
        </el-form-item>
      </el-form>
      <el-divider style="margin: 12px 0" />
      <div style="display: flex; gap: 8px">
        <el-button type="success" :disabled="!proposalId" :loading="calcLoading" @click="handleCalculate">
          计算保费
        </el-button>
        <el-button type="primary" :disabled="!proposalId" @click="createDialogVisible = true">
          创建支付记录
        </el-button>
        <el-button type="warning" :loading="scanLoading" @click="handleTimeoutScan">
          超时扫描
        </el-button>
      </div>
    </el-card>

    <!-- 保费计算结果 -->
    <el-card v-if="calcResult" style="margin-bottom: 16px">
      <template #header>
        <span style="font-weight: 500">保费计算结果</span>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="总保费">
          <span style="font-size: 18px; font-weight: bold; color: #409eff">
            {{ formatMoney(calcResult.totalPremium) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="明细数量">{{ calcResult.details?.length || 0 }} 条</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="calcResult.details?.length" :data="calcResult.details" stripe size="small" style="margin-top: 12px">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="coverageName" label="保障名称" min-width="160" />
        <el-table-column prop="premium" label="保费" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.premium) }}</template>
        </el-table-column>
        <el-table-column prop="rate" label="费率" width="100" align="right">
          <template #default="{ row }">{{ row.rate?.toFixed(6) || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 支付记录列表 -->
    <el-card v-if="queried">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>支付记录（投保单 #{{ proposalId }}）</span>
          <el-tag :type="payments.length > 0 ? 'success' : 'info'" size="small">
            {{ payments.length }} 条
          </el-tag>
        </div>
      </template>
      <el-table :data="payments" stripe border v-loading="queryLoading">
        <el-table-column prop="paymentNo" label="支付流水号" width="180" />
        <el-table-column prop="paymentType" label="支付类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ paymentTypeLabel(row.paymentType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="payChannel" label="支付渠道" width="100" align="center">
          <template #default="{ row }">{{ payChannelLabel(row.payChannel) }}</template>
        </el-table-column>
        <el-table-column prop="payChannelNo" label="第三方流水号" width="180">
          <template #default="{ row }">{{ row.payChannelNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="支付时间" width="170">
          <template #default="{ row }">{{ row.paidAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100">
          <template #default="{ row }">{{ row.operator || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>
      <el-empty v-if="!queryLoading && payments.length === 0" description="暂无支付记录" :image-size="60" />
    </el-card>

    <!-- 创建支付记录弹窗 -->
    <el-dialog v-model="createDialogVisible" title="创建支付记录" width="450px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="投保单ID">
          <el-input :model-value="String(proposalId)" disabled />
        </el-form-item>
        <el-form-item label="支付渠道" prop="payChannel">
          <el-select v-model="createForm.payChannel" style="width: 100%">
            <el-option label="微信" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="银行转账" :value="3" />
            <el-option label="线下" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreatePayment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { calculatePremium, createPayment, queryPaymentByProposal, triggerTimeoutScan } from '@/api/payment'
import type { Payment } from '@/types'
import { useDictStore } from '@/stores/dict'

const dictStore = useDictStore()

const proposalId = ref<number | undefined>(undefined)
const queried = ref(false)
const payments = ref<Payment[]>([])

const queryLoading = ref(false)
const calcLoading = ref(false)
const scanLoading = ref(false)
const createLoading = ref(false)
const calcResult = ref<{ totalPremium: number; details: any[] } | null>(null)

const createDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = ref({ payChannel: 1 })
const createRules: FormRules = {
  payChannel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }],
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function paymentTypeLabel(type: number): string {
  return dictStore.getDictLabel('payment_type', String(type))
}

function payChannelLabel(channel: number | null): string {
  if (!channel) return '-'
  return dictStore.getDictLabel('pay_channel', String(channel))
}

function statusLabel(status: number): string {
  return dictStore.getDictLabel('payment_status', String(status))
}

function statusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

async function handleQuery() {
  if (!proposalId.value) {
    ElMessage.warning('请输入投保单ID')
    return
  }
  queryLoading.value = true
  queried.value = true
  try {
    const res = await queryPaymentByProposal(proposalId.value)
    payments.value = res.data || []
  } catch {
    payments.value = []
  } finally {
    queryLoading.value = false
  }
}

async function handleCalculate() {
  if (!proposalId.value) return
  try {
    await ElMessageBox.confirm(`确定计算投保单 #${proposalId.value} 的保费吗？`, '确认', { type: 'info' })
  } catch { return }
  calcLoading.value = true
  calcResult.value = null
  try {
    const res = await calculatePremium(proposalId.value)
    calcResult.value = res.data
    ElMessage.success('保费计算完成')
  } catch { /* handled */ } finally {
    calcLoading.value = false
  }
}

async function handleCreatePayment() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!proposalId.value) return
  createLoading.value = true
  try {
    await createPayment({ proposalId: proposalId.value, payChannel: createForm.value.payChannel })
    ElMessage.success('支付记录创建成功')
    createDialogVisible.value = false
    await handleQuery()
  } catch { /* handled */ } finally {
    createLoading.value = false
  }
}

async function handleTimeoutScan() {
  try {
    await ElMessageBox.confirm('确定触发超时扫描吗？将自动处理超时的支付记录。', '确认', { type: 'warning' })
  } catch { return }
  scanLoading.value = true
  try {
    const res = await triggerTimeoutScan()
    ElMessage.success(`扫描完成，处理了 ${res.data?.processedCount || 0} 条记录`)
    if (proposalId.value) await handleQuery()
  } catch { /* handled */ } finally {
    scanLoading.value = false
  }
}
</script>
