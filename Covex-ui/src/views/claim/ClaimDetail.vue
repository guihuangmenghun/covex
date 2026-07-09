<template>
  <div>
    <el-page-header @back="$router.push('/claim')" title="返回" content="理赔详情" />

    <!-- 基本信息 -->
    <el-card style="margin-top: 20px" v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 500">{{ claim.claimNo }}</span>
          <el-tag :type="getStatusTagType(claim.status)" size="large">
            {{ getStatusLabel(claim.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="保单号">{{ claim.policyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保障名称">{{ claim.coverageName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ claim.claimHandler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出险日期">{{ claim.accidentDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出险原因">{{ claim.accidentType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出险地点">{{ claim.accidentLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报案描述" :span="3">{{ claim.accidentDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="理赔金额">{{ formatMoney(claim.claimAmount) }}</el-descriptions-item>
        <el-descriptions-item label="批准金额">{{ formatMoney(claim.approvedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="报案人">{{ claim.reporterName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报案时间">{{ claim.reportedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结案时间">{{ claim.closedAt || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮组 -->
      <div style="margin-top: 16px; display: flex; gap: 8px; flex-wrap: wrap">
        <el-button v-if="claim.status === 1" type="success" @click="handleAssign">分配理赔员</el-button>
        <el-button v-if="claim.status === 2 || claim.status === 3" type="primary" @click="reviewDialogVisible = true">提交审核</el-button>
        <el-button v-if="claim.status === 2 || claim.status === 3" type="warning" @click="handleInvestigate">启动调查</el-button>
        <el-button v-if="claim.status === 4" type="warning" @click="investigationDialogVisible = true">提交调查结论</el-button>
        <el-button v-if="claim.status === 3 || claim.status === 4" type="info" @click="handleCalculate">赔付计算</el-button>
        <el-button v-if="claim.status === 5" type="success" @click="handleProcessPayment">触发赔付</el-button>
        <el-button v-if="claim.status === 6" type="success" @click="handleClose">结案</el-button>
        <el-button v-if="claim.status === 8" type="warning" @click="handleDispute">拒赔申诉</el-button>
        <el-button v-if="claim.status === 10" type="warning" @click="supervisorDialogVisible = true">主管审批</el-button>
      </div>
    </el-card>

    <!-- Tab 区域 -->
    <el-card style="margin-top: 16px">
      <el-tabs v-model="activeTab">
        <!-- 理赔材料 -->
        <el-tab-pane label="理赔材料" name="document">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
            <el-button type="primary" :icon="Upload" size="small" @click="docDialogVisible = true">上传材料</el-button>
          </div>
          <el-table :data="documents" stripe border size="small">
            <el-table-column prop="fileName" label="文件名" min-width="180" />
            <el-table-column prop="documentType" label="文档类型" width="100" align="center" />
            <el-table-column prop="fileUrl" label="文件路径" min-width="200" />
            <el-table-column prop="uploadedBy" label="上传人" width="100">
              <template #default="{ row }">{{ row.uploadedBy || '-' }}</template>
            </el-table-column>
            <el-table-column prop="uploadedAt" label="上传时间" width="170" />
          </el-table>
          <el-empty v-if="documents.length === 0" description="暂无理赔材料" :image-size="60" />
        </el-tab-pane>

        <!-- 审核记录 -->
        <el-tab-pane label="审核记录" name="review">
          <el-timeline v-if="reviews.length > 0">
            <el-timeline-item
              v-for="rev in reviews"
              :key="rev.id"
              :timestamp="rev.reviewedAt"
              placement="top"
              :type="rev.reviewResult === 1 ? 'success' : rev.reviewResult === 2 ? 'danger' : 'warning'"
            >
              <el-card shadow="never" style="padding: 8px 12px">
                <div><strong>{{ rev.reviewType === 1 ? '初审' : '复审' }}</strong> -
                  <el-tag :type="rev.reviewResult === 1 ? 'success' : 'danger'" size="small">
                    {{ rev.reviewResult === 1 ? '通过' : '拒绝' }}
                  </el-tag>
                </div>
                <div v-if="rev.approvedAmount" style="margin-top: 4px; font-size: 13px">批准金额：{{ formatMoney(rev.approvedAmount) }}</div>
                <div v-if="rev.rejectReason" style="margin-top: 4px; font-size: 13px; color: #f56c6c">拒绝原因：{{ rev.rejectReason }}</div>
                <div v-if="rev.reviewComment" style="margin-top: 4px; font-size: 13px; color: #606266">备注：{{ rev.reviewComment }}</div>
                <div style="margin-top: 4px; font-size: 12px; color: #909399">审核人：{{ rev.reviewer }}</div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审核记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="提交审核" width="500px" destroy-on-close>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="审核类型" prop="reviewType">
          <el-select v-model="reviewForm.reviewType" style="width: 100%">
            <el-option label="初审" :value="1" />
            <el-option label="复审" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核结论" prop="reviewResult">
          <el-select v-model="reviewForm.reviewResult" style="width: 100%" @change="onReviewResultChange">
            <el-option label="通过" :value="1" />
            <el-option label="拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="reviewForm.reviewResult === 1" label="批准金额">
          <el-input-number v-model="reviewForm.approvedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="reviewForm.reviewResult === 2" label="拒绝原因">
          <el-input v-model="reviewForm.rejectReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="handleReview">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调查结论弹窗 -->
    <el-dialog v-model="investigationDialogVisible" title="提交调查结论" width="500px" destroy-on-close>
      <el-form ref="investFormRef" :model="investForm" :rules="investRules" label-width="100px">
        <el-form-item label="调查结论" prop="result">
          <el-select v-model="investForm.result" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="部分欺诈" :value="2" />
            <el-option label="完全欺诈" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="调查报告" prop="comment">
          <el-input v-model="investForm.comment" type="textarea" :rows="4" placeholder="详细描述调查结论" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="investigationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="investLoading" @click="handleInvestigationSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 主管审批弹窗 -->
    <el-dialog v-model="supervisorDialogVisible" title="主管审批" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="批准金额">
          <el-input-number v-model="supervisorAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="supervisorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="supervisorLoading" @click="handleSupervisorApprove">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传材料弹窗 -->
    <el-dialog v-model="docDialogVisible" title="上传理赔材料" width="500px" destroy-on-close>
      <el-form ref="docFormRef" :model="docForm" :rules="docRules" label-width="100px">
        <el-form-item label="文档类型" prop="documentType">
          <el-select v-model="docForm.documentType" style="width: 100%">
            <el-option label="医疗报告" :value="1" />
            <el-option label="事故报告" :value="2" />
            <el-option label="身份证明" :value="3" />
            <el-option label="财务证明" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="docForm.fileName" />
        </el-form-item>
        <el-form-item label="文件路径" prop="fileUrl">
          <el-input v-model="docForm.fileUrl" placeholder="如 /uploads/claim/doc.pdf" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="docDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="docLoading" @click="handleUploadDoc">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getClaimById, assignClaim, reviewClaim, calculateClaim,
  investigateClaim, submitInvestigationResult,
  processClaimPayment, closeClaim, disputeClaim, supervisorApproveClaim,
  getClaimDocuments, uploadClaimDocument,
} from '@/api/claim'
import type { Claim, ClaimReview, ClaimDocument } from '@/types'

const route = useRoute()
const claimId = Number(route.params.id)

const loading = ref(false)
const claim = ref<Claim>({} as Claim)
const reviews = ref<ClaimReview[]>([])
const documents = ref<ClaimDocument[]>([])
const activeTab = ref('document')

// 审核弹窗
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewFormRef = ref<FormInstance>()
const reviewForm = ref({ reviewType: 1, reviewResult: 1, approvedAmount: null as number | null, rejectReason: '', comment: '' })
const reviewRules: FormRules = {
  reviewType: [{ required: true, message: '请选择', trigger: 'change' }],
  reviewResult: [{ required: true, message: '请选择', trigger: 'change' }],
}

// 调查弹窗
const investigationDialogVisible = ref(false)
const investLoading = ref(false)
const investFormRef = ref<FormInstance>()
const investForm = ref({ result: 1, comment: '' })
const investRules: FormRules = {
  result: [{ required: true, message: '请选择调查结论', trigger: 'change' }],
  comment: [{ required: true, message: '请输入调查报告', trigger: 'blur' }],
}

// 材料弹窗
const docDialogVisible = ref(false)
const docLoading = ref(false)
const docFormRef = ref<FormInstance>()
const docForm = ref({ documentType: 1, fileName: '', fileUrl: '' })
const docRules: FormRules = {
  documentType: [{ required: true, message: '请选择', trigger: 'change' }],
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入文件路径', trigger: 'blur' }],
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStatusLabel(status: number): string {
  const map: Record<number, string> = { 1: '已报案', 2: '已分配', 3: '审核中', 4: '调查中', 5: '待赔付', 6: '已赔付', 7: '已结案', 8: '已拒赔', 9: '申诉中', 10: '待主管复审', 11: '待总经理审批' }
  return map[status] || '未知'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'warning', 4: 'warning', 5: 'warning', 6: 'success', 7: 'info', 8: 'danger', 9: 'warning' }
  return map[status] || 'info'
}

function onReviewResultChange() {
  reviewForm.value.approvedAmount = null
  reviewForm.value.rejectReason = ''
}

async function loadClaim() {
  loading.value = true
  try {
    const res = await getClaimById(claimId)
    const data = res.data || {} as any
    claim.value = data.claim || ({} as Claim)
    reviews.value = data.reviews || []
  } catch { /* handled */ } finally { loading.value = false }
}

async function loadDocuments() {
  try { const r = await getClaimDocuments(claimId); documents.value = r.data || [] } catch { documents.value = [] }
}

async function handleAssign() {
  try { await ElMessageBox.confirm('确定分配此理赔案件吗？', '确认', { type: 'info' }) } catch { return }
  try { await assignClaim(claimId); ElMessage.success('分配成功'); await loadClaim() } catch { /* handled */ }
}

async function handleReview() {
  const v = await reviewFormRef.value?.validate().catch(() => false); if (!v) return
  reviewLoading.value = true
  try {
    await reviewClaim(claimId, {
      reviewType: reviewForm.value.reviewType, reviewResult: reviewForm.value.reviewResult,
      approvedAmount: reviewForm.value.reviewResult === 1 ? (reviewForm.value.approvedAmount || undefined) : undefined,
      rejectReason: reviewForm.value.reviewResult === 2 ? reviewForm.value.rejectReason : undefined,
      comment: reviewForm.value.comment || undefined, reviewer: 'admin',
    })
    ElMessage.success('审核提交成功'); reviewDialogVisible.value = false; await loadClaim()
  } catch { /* handled */ } finally { reviewLoading.value = false }
}

async function handleInvestigate() {
  try { await ElMessageBox.confirm('确定启动调查吗？', '确认', { type: 'warning' }) } catch { return }
  try { await investigateClaim(claimId); ElMessage.success('已启动调查'); await loadClaim() } catch { /* handled */ }
}

async function handleInvestigationSubmit() {
  const v = await investFormRef.value?.validate().catch(() => false); if (!v) return
  investLoading.value = true
  try {
    await submitInvestigationResult(claimId, { result: investForm.value.result, comment: investForm.value.comment })
    ElMessage.success('调查结论提交成功'); investigationDialogVisible.value = false; await loadClaim()
  } catch { /* handled */ } finally { investLoading.value = false }
}

async function handleCalculate() {
  try { await ElMessageBox.confirm('确定执行赔付计算吗？', '确认', { type: 'info' }) } catch { return }
  try { const r = await calculateClaim(claimId); ElMessage.success(`赔付计算完成，金额：${formatMoney(r.data)}`); await loadClaim() } catch { /* handled */ }
}

async function handleProcessPayment() {
  try { await ElMessageBox.confirm('确定触发赔付吗？', '确认', { type: 'warning' }) } catch { return }
  try { await processClaimPayment(claimId); ElMessage.success('赔付已触发'); await loadClaim() } catch { /* handled */ }
}

async function handleClose() {
  try { await ElMessageBox.confirm('确定结案吗？', '确认', { type: 'warning' }) } catch { return }
  try { await closeClaim(claimId); ElMessage.success('已结案'); await loadClaim() } catch { /* handled */ }
}

async function handleDispute() {
  try { await ElMessageBox.confirm('确定发起拒赔申诉吗？', '确认', { type: 'warning' }) } catch { return }
  try { await disputeClaim(claimId); ElMessage.success('申诉已发起'); await loadClaim() } catch { /* handled */ }
}

// 主管审批弹窗
const supervisorDialogVisible = ref(false)
const supervisorLoading = ref(false)
const supervisorAmount = ref<number | null>(null)
async function handleSupervisorApprove() {
  if (supervisorAmount.value == null || supervisorAmount.value <= 0) { ElMessage.warning('请输入批准金额'); return }
  supervisorLoading.value = true
  try {
    await supervisorApproveClaim(claimId, supervisorAmount.value)
    ElMessage.success('主管审批成功'); supervisorDialogVisible.value = false; await loadClaim()
  } catch { /* handled */ } finally { supervisorLoading.value = false }
}

async function handleUploadDoc() {
  const v = await docFormRef.value?.validate().catch(() => false); if (!v) return
  docLoading.value = true
  try {
    await uploadClaimDocument(claimId, docForm.value)
    ElMessage.success('上传成功'); docDialogVisible.value = false; await loadDocuments()
  } catch { /* handled */ } finally { docLoading.value = false }
}

onMounted(async () => { await Promise.all([loadClaim(), loadDocuments()]) })
</script>
