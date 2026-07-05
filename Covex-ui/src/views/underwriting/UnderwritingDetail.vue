<template>
  <div>
    <el-page-header @back="$router.push('/underwriting')" title="返回" content="核保详情" />

    <!-- 投保单基本信息 -->
    <el-card style="margin-top: 20px" v-loading="proposalLoading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 500">{{ proposal.proposalNo }}</span>
          <div style="display: flex; gap: 8px">
            <el-tag :type="proposal.status === 2 || proposal.status === 3 ? 'warning' : 'info'" size="large">
              {{ getStatusLabel(proposal.status) }}
            </el-tag>
            <el-button v-if="proposal.status === 2 || proposal.status === 3" type="success" @click="handleAutoUw">自动核保</el-button>
            <el-button v-if="proposal.status === 2 || proposal.status === 3" type="warning" @click="manualDialogVisible = true">人工核保</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品名称">{{ proposal.productSnapshot?.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ proposal.productSnapshot?.productCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投保人ID">{{ proposal.applicantId }}</el-descriptions-item>
        <el-descriptions-item label="被保人ID">{{ proposal.insuredId }}</el-descriptions-item>
        <el-descriptions-item label="总保额">{{ formatMoney(proposal.totalSumInsured) }}</el-descriptions-item>
        <el-descriptions-item label="总保费">{{ proposal.totalPremium != null ? formatMoney(proposal.totalPremium) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ proposal.submitAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ proposal.operator || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 核保记录时间线 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <span style="font-weight: 500">核保记录</span>
      </template>
      <div v-loading="recordsLoading">
        <el-timeline v-if="records.length > 0">
          <el-timeline-item
            v-for="rec in records"
            :key="rec.id"
            :timestamp="rec.uwAt"
            placement="top"
            :type="getRecordType(rec.uwResult)"
          >
            <el-card shadow="never" style="padding: 8px 12px">
              <div style="display: flex; justify-content: space-between; margin-bottom: 4px">
                <strong>{{ rec.uwType === 1 ? '自动核保' : '人工核保' }}</strong>
                <el-tag :type="getRecordType(rec.uwResult)" size="small">{{ uwResultLabel(rec.uwResult) }}</el-tag>
              </div>
              <div v-if="rec.loadingAmount" style="color: #e6a23c; font-size: 13px">
                加费金额：{{ formatMoney(rec.loadingAmount) }}
              </div>
              <div v-if="rec.exclusionDesc" style="color: #f56c6c; font-size: 13px">
                除外责任：{{ rec.exclusionDesc }}
              </div>
              <div v-if="rec.uwComment" style="color: #606266; font-size: 13px; margin-top: 4px">
                备注：{{ rec.uwComment }}
              </div>
              <div style="color: #909399; font-size: 12px; margin-top: 4px">
                操作人：{{ rec.uwOperator }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无核保记录" :image-size="60" />
      </div>
    </el-card>

    <!-- 人工核保弹窗 -->
    <el-dialog v-model="manualDialogVisible" title="人工核保" width="500px" destroy-on-close>
      <el-form ref="manualFormRef" :model="manualForm" :rules="manualRules" label-width="100px">
        <el-form-item label="核保结论" prop="uwResult">
          <el-select v-model="manualForm.uwResult" style="width: 100%" @change="onUwResultChange">
            <el-option label="标准体（通过）" :value="1" />
            <el-option label="加费" :value="2" />
            <el-option label="除外" :value="3" />
            <el-option label="延期" :value="4" />
            <el-option label="拒保" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="manualForm.uwResult === 2" label="加费金额" prop="loadingAmount">
          <el-input-number v-model="manualForm.loadingAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="manualForm.uwResult === 3" label="除外责任" prop="exclusionDesc">
          <el-input v-model="manualForm.exclusionDesc" type="textarea" :rows="2" placeholder="描述除外责任内容" />
        </el-form-item>
        <el-form-item label="核保备注">
          <el-input v-model="manualForm.comment" type="textarea" :rows="3" placeholder="核保备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="manualLoading" @click="handleManualUw">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProposalById } from '@/api/proposal'
import { autoUnderwrite, manualUnderwrite, getUnderwritingRecords } from '@/api/underwriting'
import type { Proposal, UnderwritingRecord } from '@/types'

const route = useRoute()
const proposalId = Number(route.params.proposalId)

const proposalLoading = ref(false)
const proposal = ref<Proposal>({} as Proposal)
const recordsLoading = ref(false)
const records = ref<UnderwritingRecord[]>([])

const manualDialogVisible = ref(false)
const manualLoading = ref(false)
const manualFormRef = ref<FormInstance>()
const manualForm = ref({
  uwResult: 1,
  loadingAmount: null as number | null,
  exclusionDesc: '',
  comment: '',
})
const manualRules: FormRules = {
  uwResult: [{ required: true, message: '请选择核保结论', trigger: 'change' }],
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStatusLabel(status: number): string {
  const map: Record<number, string> = { 1: '待校验', 2: '待核保', 3: '核保中', 4: '待支付', 5: '已支付', 6: '已出单', 7: '已拒保', 8: '已撤销' }
  return map[status] || '未知'
}

function uwResultLabel(result: number): string {
  const map: Record<number, string> = { 1: '标准体', 2: '加费', 3: '除外', 4: '延期', 5: '拒保' }
  return map[result] || '未知'
}

function getRecordType(result: number): string {
  if (result === 1) return 'success'
  if (result === 5) return 'danger'
  return 'warning'
}

function onUwResultChange() {
  manualForm.value.loadingAmount = null
  manualForm.value.exclusionDesc = ''
}

async function loadProposal() {
  proposalLoading.value = true
  try {
    const res = await getProposalById(proposalId)
    proposal.value = res.data || ({} as Proposal)
  } catch { /* handled */ } finally {
    proposalLoading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const res = await getUnderwritingRecords(proposalId)
    records.value = res.data || []
  } catch { records.value = [] } finally {
    recordsLoading.value = false
  }
}

async function handleAutoUw() {
  try {
    await ElMessageBox.confirm('确定执行自动核保吗？', '确认', { type: 'info' })
    await autoUnderwrite(proposalId)
    ElMessage.success('自动核保完成')
    await Promise.all([loadProposal(), loadRecords()])
  } catch { /* cancel or handled */ }
}

async function handleManualUw() {
  const valid = await manualFormRef.value?.validate().catch(() => false)
  if (!valid) return

  manualLoading.value = true
  try {
    await manualUnderwrite(proposalId, {
      uwResult: manualForm.value.uwResult,
      loadingAmount: manualForm.value.uwResult === 2 ? (manualForm.value.loadingAmount || undefined) : undefined,
      exclusionDesc: manualForm.value.uwResult === 3 ? manualForm.value.exclusionDesc : undefined,
      comment: manualForm.value.comment || undefined,
      operator: 'admin',
    })
    ElMessage.success('人工核保完成')
    manualDialogVisible.value = false
    await Promise.all([loadProposal(), loadRecords()])
  } catch { /* handled */ } finally {
    manualLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadProposal(), loadRecords()])
})
</script>
