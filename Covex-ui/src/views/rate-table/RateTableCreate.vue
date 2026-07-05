<template>
  <div>
    <el-page-header @back="$router.push('/rate-table')" title="返回" content="创建费率表" />

    <el-card style="margin-top: 20px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width: 600px">
        <el-form-item label="表编码" prop="rateTableCode">
          <el-input v-model="form.rateTableCode" placeholder="如 LIFE_RATE_001" />
        </el-form-item>
        <el-form-item label="表名称" prop="rateTableName">
          <el-input v-model="form.rateTableName" placeholder="如 定期寿险费率表" />
        </el-form-item>
        <el-form-item label="关联产品" prop="productId">
          <el-select v-model="form.productId" placeholder="选择产品" filterable style="width: 100%">
            <el-option v-for="p in productList" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="如 1.0" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择生效日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker v-model="form.expiryDate" type="date" placeholder="选择失效日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">创建</el-button>
          <el-button @click="$router.push('/rate-table')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createRateTable } from '@/api/rateTable'
import { getProductList } from '@/api/product'
import type { Product } from '@/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const productList = ref<Product[]>([])

const form = ref({
  rateTableCode: '',
  rateTableName: '',
  productId: null as number | null,
  version: '1.0',
  effectiveDate: null as string | null,
  expiryDate: null as string | null,
})

const formRules: FormRules = {
  rateTableCode: [{ required: true, message: '请输入表编码', trigger: 'blur' }],
  rateTableName: [{ required: true, message: '请输入表名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择关联产品', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
}

async function loadProductList() {
  try {
    const res = await getProductList()
    productList.value = res.data?.records || []
  } catch { /* handled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!form.value.productId) return

  submitLoading.value = true
  try {
    const res = await createRateTable({
      rateTableCode: form.value.rateTableCode,
      rateTableName: form.value.rateTableName,
      productId: form.value.productId,
      version: form.value.version,
      effectiveDate: form.value.effectiveDate,
      expiryDate: form.value.expiryDate,
      tableSchema: null,
      isDeleted: 0,
      deletedAt: null,
      createdBy: null,
      createdAt: '',
      tenantId: 0,
    })
    ElMessage.success('创建成功')
    router.push(`/rate-table/${res.data.id}`)
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

onMounted(() => { loadProductList() })
</script>

