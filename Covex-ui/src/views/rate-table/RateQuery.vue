<template>
  <div>
    <h2 style="margin: 0 0 16px">费率查询工具</h2>

    <el-card>
      <el-form label-width="100px" style="max-width: 600px">
        <el-form-item label="选择产品" required>
          <el-select
            v-model="selectedProductId"
            placeholder="搜索并选择产品"
            filterable
            style="width: 100%"
            @change="onProductChange"
          >
            <el-option v-for="p in products" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="费率表" v-if="rateTables.length > 0">
          <el-select v-model="selectedRateTableId" placeholder="选择费率表" style="width: 100%" @change="onRateTableChange">
            <el-option v-for="rt in rateTables" :key="rt.id" :label="`${rt.tableName || rt.tableCode} (v${rt.version})`" :value="rt.id" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">查询条件</el-divider>

        <el-form-item label="年龄" required>
          <el-input-number v-model="queryAge" :min="0" :max="100" :step="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="性别" required>
          <el-radio-group v-model="queryGender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="保额" required>
          <el-input-number v-model="querySumInsured" :min="1000" :step="10000" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="queryLoading" :disabled="!canQuery" @click="handleQuery">查询费率</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 查询结果 -->
    <el-card v-if="hasResult" style="margin-top: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>查询结果</span>
          <el-tag :type="resultSource === 'redis' ? 'success' : 'warning'" size="small">
            {{ resultSource === 'redis' ? 'Redis 命中' : 'DB 回源' }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="费率表">{{ queryResult.tableCode }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ queryResult.version }}</el-descriptions-item>
        <el-descriptions-item label="维度键" :span="2">{{ queryResult.dimensionKey }}</el-descriptions-item>
        <el-descriptions-item label="费率值" :span="2">
          <span style="font-size: 18px; font-weight: bold; color: #409eff">
            {{ queryResult.rateValue?.toFixed(6) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="参考保费" :span="2">
          <span style="font-size: 22px; font-weight: bold; color: #67c23a">
            ¥ {{ referencePremium }}
          </span>
          <span style="color: #909399; font-size: 12px; margin-left: 8px">
            (保额 {{ formatMoney(querySumInsured) }} × 费率 {{ queryResult.rateValue?.toFixed(6) }})
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 无结果提示 -->
    <el-card v-else-if="queried && !queryLoading" style="margin-top: 16px">
      <el-empty description="未查询到费率数据" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { queryRate } from '@/api/rateTable'
import { getProductList } from '@/api/product'
import { getRateTablePage } from '@/api/rateTable'
import type { Product } from '@/types'

// 产品列表
const products = ref<Product[]>([])
const selectedProductId = ref<number | null>(null)

// 费率表
const rateTables = ref<any[]>([])
const selectedRateTableId = ref<number | null>(null)

// 查询条件
const queryAge = ref(30)
const queryGender = ref(1)
const querySumInsured = ref(100000)

// 查询状态
const queryLoading = ref(false)
const queried = ref(false)
const hasResult = ref(false)
const resultSource = ref<'redis' | 'db'>('db')

const queryResult = ref<{
  tableCode: string
  version: string
  dimensionKey: string
  rateValue: number
  source?: string
} | null>(null)

const canQuery = computed(() => {
  return selectedProductId.value && selectedRateTableId.value && queryAge.value >= 0 && querySumInsured.value > 0
})

const referencePremium = computed(() => {
  if (!queryResult.value?.rateValue) return '0.00'
  return (queryResult.value.rateValue * querySumInsured.value).toFixed(2)
})

function formatMoney(val: number): string {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadProducts() {
  try {
    const res = await getProductList()
    products.value = res.data?.records || []
  } catch { /* handled */ }
}

async function onProductChange(productId: number | null) {
  rateTables.value = []
  selectedRateTableId.value = null
  hasResult.value = false
  queried.value = false
  if (!productId) return

  try {
    const res = await getRateTablePage({ page: 1, size: 99, productId })
    rateTables.value = res.data?.records || []
    if (rateTables.value.length > 0) {
      selectedRateTableId.value = rateTables.value[0].id
    }
  } catch { /* handled */ }
}

function onRateTableChange() {
  hasResult.value = false
  queried.value = false
}

async function handleQuery() {
  const rt = rateTables.value.find(r => r.id === selectedRateTableId.value)
  if (!rt) {
    ElMessage.warning('请选择费率表')
    return
  }

  const dimensionKey = `age:${queryAge.value}|gender:${queryGender.value}`

  queryLoading.value = true
  queried.value = true
  hasResult.value = false

  try {
    const res = await queryRate({
      tableCode: rt.tableCode,
      version: rt.version || '1.0',
      dimensionKey,
    })

    if (res.data && res.data.rateValue !== undefined && res.data.rateValue !== null) {
      queryResult.value = res.data
      hasResult.value = true
      resultSource.value = res.data.source === 'redis' ? 'redis' : 'db'
    }
  } catch {
    hasResult.value = false
  } finally {
    queryLoading.value = false
  }
}

function resetQuery() {
  selectedProductId.value = null
  rateTables.value = []
  selectedRateTableId.value = null
  queryAge.value = 30
  queryGender.value = 1
  querySumInsured.value = 100000
  queried.value = false
  hasResult.value = false
  queryResult.value = null
}

onMounted(() => { loadProducts() })
</script>
