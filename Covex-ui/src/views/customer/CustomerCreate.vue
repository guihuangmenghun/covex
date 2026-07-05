<template>
  <div>
    <el-page-header @back="$router.back()" title="返回" content="新建客户" />

    <el-card style="margin-top: 20px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width: 600px">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="证件类型" prop="idType">
          <el-select v-model="form.idType" placeholder="选择证件类型" style="width: 100%">
            <el-option label="身份证" :value="1" />
            <el-option label="护照" :value="2" />
            <el-option label="军官证" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号" prop="idNo">
          <el-input v-model="form.idNo" placeholder="请输入证件号" />
        </el-form-item>
        <el-form-item label="证件有效期" prop="idExpiry">
          <el-date-picker v-model="form.idExpiry" type="date" placeholder="选择证件有效期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker v-model="form.birthDate" type="date" placeholder="选择出生日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="国籍" prop="nationality">
          <el-input v-model="form.nationality" placeholder="请输入国籍" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-select v-model="form.customerType" placeholder="选择客户类型" style="width: 100%">
            <el-option label="个人" :value="1" />
            <el-option label="团体" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
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
import { createCustomer } from '@/api/customer'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const form = ref({
  customerName: '',
  idType: 1,
  idNo: '',
  idExpiry: null as string | null,
  gender: 1,
  birthDate: null as string | null,
  nationality: '中国',
  phone: '',
  email: '',
  customerType: 1,
})

const formRules: FormRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  idType: [{ required: true, message: '请选择证件类型', trigger: 'change' }],
  idNo: [{ required: true, message: '请输入证件号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  customerType: [{ required: true, message: '请选择客户类型', trigger: 'change' }],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const res = await createCustomer(form.value)
    ElMessage.success('创建成功')
    router.push(`/customer/${res.data.id}`)
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}
</script>
