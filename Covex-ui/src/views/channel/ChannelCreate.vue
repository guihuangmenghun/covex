<template>
  <div>
    <el-page-header @back="$router.push('/channel')" title="返回" content="创建渠道商" />

    <el-card style="margin-top: 20px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" style="max-width: 650px">
        <el-form-item label="渠道编码" prop="channelCode">
          <el-input v-model="form.channelCode" placeholder="如 CH001" />
        </el-form-item>
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="form.channelName" placeholder="渠道商全称" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-select v-model="form.channelType" style="width: 100%">
            <el-option label="代理人" :value="1" />
            <el-option label="经纪人" :value="2" />
            <el-option label="银保" :value="3" />
            <el-option label="互联网" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="许可证号">
          <el-input v-model="form.licenseNo" placeholder="经营许可证编号（可选）" />
        </el-form-item>
        <el-form-item label="许可证到期">
          <el-date-picker v-model="form.licenseExpiry" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="区域编码">
          <el-input v-model="form.regionCode" placeholder="如 310000" />
        </el-form-item>
        <el-form-item label="合同编号">
          <el-input v-model="form.contractNo" placeholder="合同编号（可选）" />
        </el-form-item>
        <el-form-item label="合同起始">
          <el-date-picker v-model="form.contractStart" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="合同到期">
          <el-date-picker v-model="form.contractEnd" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">创建</el-button>
          <el-button @click="$router.push('/channel')">取消</el-button>
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
import { createChannel } from '@/api/channel'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const form = ref({
  channelCode: '',
  channelName: '',
  channelType: 1,
  licenseNo: '',
  licenseExpiry: null as string | null,
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  regionCode: '',
  contractNo: '',
  contractStart: null as string | null,
  contractEnd: null as string | null,
})

const formRules: FormRules = {
  channelCode: [{ required: true, message: '请输入渠道编码', trigger: 'blur' }],
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  channelType: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: any = { ...form.value }
    if (!data.licenseNo) data.licenseNo = null
    if (!data.contactEmail) data.contactEmail = null
    if (!data.regionCode) data.regionCode = null
    if (!data.contractNo) data.contractNo = null
    const res = await createChannel(data)
    ElMessage.success('创建成功')
    router.push(`/channel/${res.data.id}`)
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}
</script>
