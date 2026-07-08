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

        <!-- 保障责任选择（选择产品后显示） -->
        <template v-if="coverages.length > 0">
          <el-divider content-position="left">保障责任 <span style="color: #f56c6c; font-size: 12px">（至少选择一项）</span></el-divider>
          <el-form-item
            v-for="cov in coverages"
            :key="cov.id"
            :label="cov.coverageName"
          >
            <div style="display: flex; align-items: center; gap: 12px; width: 100%">
              <el-checkbox v-model="coverageSelections[cov.id]">选择</el-checkbox>
              <el-input-number
                v-if="coverageSelections[cov.id]"
                v-model="coverageSumInsureds[cov.id]"
                :min="1000"
                :step="10000"
                :precision="2"
                placeholder="保额"
                style="flex: 1"
              />
              <span v-if="coverageSelections[cov.id]" style="color: #909399; font-size: 12px; white-space: nowrap">元</span>
            </div>
          </el-form-item>
        </template>

        <!-- 缴费计划选择（选择产品后显示） -->
        <el-form-item label="缴费计划" v-if="premiums.length > 0">
          <el-select v-model="selectedPremiumPlanId" placeholder="选择缴费计划" style="width: 100%">
            <el-option
              v-for="p in premiums"
              :key="p.id"
              :label="`${p.premiumPlanName} (${freqLabel(p.paymentFrequency)}, ${p.paymentTerm}${p.paymentTermUnit === 1 ? '年' : '月'})`"
              :value="p.id"
            />
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createProposal } from '@/api/proposal'
import { getProductList, getProductById } from '@/api/product'
import { getCustomerPage } from '@/api/customer'
import { getChannelPage, getChannelUsers } from '@/api/channel'
import type { Product, ProductCoverage, ProductPremium, Customer, Channel, ChannelUser, CreateProposalRequest } from '@/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const products = ref<Product[]>([])
const customers = ref<Customer[]>([])
const channels = ref<Channel[]>([])
const channelUsers = ref<ChannelUser[]>([])

// 保障 & 缴费相关
const coverages = ref<ProductCoverage[]>([])
const premiums = ref<ProductPremium[]>([])
const coverageSelections = ref<Record<number, boolean>>({})
const coverageSumInsureds = ref<Record<number, number>>({})
const selectedPremiumPlanId = ref<number | null>(null)

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

async function onProductChange() {
  // 重置保障和缴费选择
  coverages.value = []
  premiums.value = []
  coverageSelections.value = {}
  coverageSumInsureds.value = {}
  selectedPremiumPlanId.value = null

  if (!form.value.productId) return

  try {
    const res = await getProductById(form.value.productId)
    const detail = (res.data || {}) as any
    coverages.value = detail.coverages || []
    premiums.value = detail.premiums || []
  } catch { /* handled */ }
}

function freqLabel(f: number) {
  return ({ 1: '趸交', 2: '年交', 4: '半年交', 12: '月交' } as Record<number, string>)[f] || `频率${f}`
}

const selectedCoveragesData = computed(() => {
  return coverages.value
    .filter(c => coverageSelections.value[c.id])
    .map(c => ({
      coverageId: c.id,
      coverageCode: c.coverageCode,
      coverageName: c.coverageName,
      sumInsured: coverageSumInsureds.value[c.id] || 0,
    }))
})

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

  // 校验保障责任
  if (coverages.value.length > 0 && selectedCoveragesData.value.length === 0) {
    ElMessage.warning('请至少选择一项保障责任')
    return
  }
  // 校验已选保障的保额
  for (const cov of selectedCoveragesData.value) {
    if (!cov.sumInsured || cov.sumInsured <= 0) {
      ElMessage.warning(`请输入「${cov.coverageName}」的保额`)
      return
    }
  }

  submitLoading.value = true
  try {
    const selectedPremium = premiums.value.find(p => p.id === selectedPremiumPlanId.value) || null
    const data: CreateProposalRequest = {
      productId: form.value.productId!,
      applicantId: form.value.applicantId!,
      insuredId: form.value.insuredId!,
      totalSumInsured: form.value.totalSumInsured,
      channelId: form.value.channelId || undefined,
      channelUserId: form.value.channelUserId || undefined,
      selectedCoverages: selectedCoveragesData.value.length > 0 ? selectedCoveragesData.value : undefined,
      selectedPremiumPlan: selectedPremium ? {
        premiumPlanId: selectedPremium.id,
        premiumPlanCode: selectedPremium.premiumPlanCode,
        premiumPlanName: selectedPremium.premiumPlanName,
        paymentFrequency: selectedPremium.paymentFrequency,
        paymentTerm: selectedPremium.paymentTerm,
        paymentTermUnit: selectedPremium.paymentTermUnit,
        gracePeriod: selectedPremium.gracePeriod,
      } : undefined,
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
