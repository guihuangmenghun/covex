<template>
  <div>
    <el-page-header @back="$router.push('/proposal')" title="返回" content="新建投保单" />

    <el-card style="margin-top: 20px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" style="max-width: 650px">
        <el-form-item label="选择产品" prop="productId">
          <el-select
            v-model="form.productId"
            placeholder="搜索并选择产品"
            filterable
            style="width: 100%"
            @change="onProductChange"
          >
            <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="投保人" prop="applicantId">
          <el-select v-model="form.applicantId" placeholder="搜索并选择投保人" filterable style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.customerName} (ID: ${c.id})`" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="被保人" prop="insuredId">
          <el-select v-model="form.insuredId" placeholder="搜索并选择被保人" filterable style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.customerName} (ID: ${c.id})`" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="总保额" prop="totalSumInsured">
          <el-input-number v-model="form.totalSumInsured" :min="0" :precision="2" :step="10000" style="width: 100%" />
        </el-form-item>

        <el-form-item label="渠道商">
          <el-select
            v-model="form.channelId"
            placeholder="选择渠道商（可选）"
            clearable
            filterable
            style="width: 100%"
            @change="onChannelChange"
          >
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="渠道用户" v-if="form.channelId">
          <el-select v-model="form.channelUserId" placeholder="选择渠道用户（可选）" clearable filterable style="width: 100%">
            <el-option v-for="u in channelUsers" :key="u.id" :label="`${u.realName || u.username}`" :value="u.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">创建投保单</el-button>
          <el-button @click="$router.push('/proposal')">取消</el-button>
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
import { createProposal } from '@/api/proposal'
import { getProductList } from '@/api/product'
import { getCustomerPage } from '@/api/customer'
import { getChannelPage, getChannelUsers } from '@/api/channel'
import type { Product, Customer, Channel, ChannelUser, CreateProposalRequest } from '@/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const products = ref<Product[]>([])
const customers = ref<Customer[]>([])
const channels = ref<Channel[]>([])
const channelUsers = ref<ChannelUser[]>([])

const form = ref({
  productId: null as number | null,
  applicantId: null as number | null,
  insuredId: null as number | null,
  totalSumInsured: 100000,
  channelId: null as number | null,
  channelUserId: null as number | null,
})

const formRules: FormRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  applicantId: [{ required: true, message: '请选择投保人', trigger: 'change' }],
  insuredId: [{ required: true, message: '请选择被保人', trigger: 'change' }],
  totalSumInsured: [{ required: true, message: '请输入总保额', trigger: 'blur' }],
}

function onProductChange() {
  // Reset dependent fields when product changes
}

async function onChannelChange(channelId: number | null) {
  form.value.channelUserId = null
  channelUsers.value = []
  if (channelId) {
    try {
      const res = await getChannelUsers(channelId)
      channelUsers.value = res.data || []
    } catch { /* handled */ }
  }
}

async function loadProducts() {
  try {
    const res = await getProductList()
    products.value = res.data?.records || []
  } catch { /* handled */ }
}

async function loadCustomers() {
  try {
    const res = await getCustomerPage({ page: 1, size: 999 })
    customers.value = res.data?.records || []
  } catch { /* handled */ }
}

async function loadChannels() {
  try {
    const res = await getChannelPage({ page: 1, size: 999 })
    channels.value = res.data?.records || []
  } catch { /* handled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data: CreateProposalRequest = {
      productId: form.value.productId!,
      applicantId: form.value.applicantId!,
      insuredId: form.value.insuredId!,
      totalSumInsured: form.value.totalSumInsured,
      channelId: form.value.channelId || undefined,
      channelUserId: form.value.channelUserId || undefined,
    }
    const res = await createProposal(data)
    ElMessage.success('创建成功')
    router.push(`/proposal/${res.data.id}`)
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadCustomers(), loadChannels()])
})
</script>
