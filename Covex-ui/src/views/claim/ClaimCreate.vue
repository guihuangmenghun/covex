<template>
  <div>
    <el-page-header @back="$router.push('/claim')" title="返回" content="理赔报案" />

    <el-card style="margin-top: 20px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" style="max-width: 650px">
        <el-form-item label="保单ID" prop="policyId">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="form.policyId" placeholder="输入保单ID" style="flex: 1" />
            <el-button :loading="policyLoading" @click="loadCoverages">查询保障</el-button>
          </div>
        </el-form-item>
        <el-form-item label="出险日期" prop="accidentDate">
          <el-date-picker v-model="form.accidentDate" type="date" placeholder="选择出险日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="出险原因" prop="accidentType">
          <el-select v-model="form.accidentType" placeholder="选择出险原因" style="width: 100%">
            <el-option label="交通事故" value="交通事故" />
            <el-option label="疾病" value="疾病" />
            <el-option label="意外伤害" value="意外伤害" />
            <el-option label="自然灾害" value="自然灾害" />
            <el-option label="火灾" value="火灾" />
            <el-option label="盗窃" value="盗窃" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="出险地点">
          <el-input v-model="form.accidentLocation" placeholder="出险地点（可选）" />
        </el-form-item>
        <el-form-item label="报案描述" prop="accidentDesc">
          <el-input v-model="form.accidentDesc" type="textarea" :rows="4" placeholder="详细描述出险情况" />
        </el-form-item>
        <el-form-item label="理赔金额" prop="claimAmount">
          <el-input-number v-model="form.claimAmount" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="险种">
          <el-select v-model="form.coverageId" placeholder="选择保障责任" style="width: 100%" :disabled="coverages.length === 0" clearable>
            <el-option v-for="cov in coverages" :key="cov.id" :label="`${cov.coverageName} (保额: ${cov.sumInsured})`" :value="cov.id" />
          </el-select>
          <div v-if="coverages.length === 0 && policyQueried" style="color: #e6a23c; font-size: 12px; margin-top: 4px">该保单无保障责任</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交报案</el-button>
          <el-button @click="$router.push('/claim')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createClaim } from '@/api/claim'
import { getPolicyById } from '@/api/policy'
import type { ReportClaimRequest, PolicyCoverage } from '@/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const policyLoading = ref(false)
const policyQueried = ref(false)
const coverages = ref<PolicyCoverage[]>([])

const form = ref({
  policyId: null as number | null,
  accidentDate: '',
  accidentType: '',
  accidentLocation: '',
  accidentDesc: '',
  claimAmount: 0,
  coverageId: null as number | null,
})

const formRules: FormRules = {
  policyId: [{ required: true, message: '请输入保单ID', trigger: 'blur' }],
  accidentDate: [{ required: true, message: '请选择出险日期', trigger: 'change' }],
  accidentType: [{ required: true, message: '请选择出险原因', trigger: 'change' }],
  accidentDesc: [{ required: true, message: '请输入报案描述', trigger: 'blur' }],
  claimAmount: [{ required: true, message: '请输入理赔金额', trigger: 'blur' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: ReportClaimRequest = {
      policyId: form.value.policyId!,
      accidentDate: form.value.accidentDate,
      accidentType: form.value.accidentType,
      accidentDesc: form.value.accidentDesc,
      claimAmount: form.value.claimAmount,
      accidentLocation: form.value.accidentLocation || undefined,
      coverageId: form.value.coverageId || undefined,
    }
    const res = await createClaim(data)
    ElMessage.success('报案成功')
    router.push(`/claim/${res.data.id}`)
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function loadCoverages() {
  const policyId = form.value.policyId
  if (!policyId) {
    ElMessage.warning('请先输入保单ID')
    return
  }
  policyLoading.value = true
  policyQueried.value = true
  coverages.value = []
  form.value.coverageId = null
  try {
    const res = await getPolicyById(Number(policyId))
    coverages.value = res.data?.coverages || []
    if (coverages.value.length === 0) {
      ElMessage.warning('该保单无保障责任')
    } else {
      ElMessage.success(`加载到 ${coverages.value.length} 项保障责任`)
    }
  } catch {
    ElMessage.error('保单查询失败，请检查保单ID是否正确')
  } finally {
    policyLoading.value = false
  }
}
</script>
