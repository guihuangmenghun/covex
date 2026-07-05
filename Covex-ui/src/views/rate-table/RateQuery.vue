<template>
  <div>
    <h2 style="margin: 0 0 16px">费率查询工具</h2>

    <el-card>
      <el-form :inline="true" @submit.prevent="handleQuery">
        <el-form-item label="表编码" required>
          <el-input v-model="queryForm.tableCode" placeholder="如 LIFE_RATE_001" style="width: 180px" />
        </el-form-item>
        <el-form-item label="版本" required>
          <el-input v-model="queryForm.version" placeholder="如 1.0" style="width: 100px" />
        </el-form-item>
        <el-form-item label="维度键" required>
          <el-input v-model="queryForm.dimensionKey" placeholder="如 age:30|gender:1" style="width: 250px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="queryLoading" @click="handleQuery">查询</el-button>
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
        <el-descriptions-item label="表编码">{{ queryResult.tableCode }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ queryResult.version }}</el-descriptions-item>
        <el-descriptions-item label="维度键" :span="2">{{ queryResult.dimensionKey }}</el-descriptions-item>
        <el-descriptions-item label="费率值" :span="2">
          <span style="font-size: 18px; font-weight: bold; color: #409eff">
            {{ queryResult.rateValue?.toFixed(6) }}
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { queryRate } from '@/api/rateTable'

const queryLoading = ref(false)
const queried = ref(false)
const hasResult = ref(false)
const resultSource = ref<'redis' | 'db'>('db')

const queryForm = ref({
  tableCode: '',
  version: '',
  dimensionKey: '',
})

const queryResult = ref<{
  tableCode: string
  version: string
  dimensionKey: string
  rateValue: number
  source?: string
} | null>(null)

async function handleQuery() {
  if (!queryForm.value.tableCode || !queryForm.value.version || !queryForm.value.dimensionKey) {
    ElMessage.warning('请填写所有必填字段')
    return
  }

  queryLoading.value = true
  queried.value = true
  hasResult.value = false

  try {
    const res = await queryRate({
      tableCode: queryForm.value.tableCode,
      version: queryForm.value.version,
      dimensionKey: queryForm.value.dimensionKey,
    })

    if (res.data && res.data.rateValue !== undefined && res.data.rateValue !== null) {
      queryResult.value = res.data
      hasResult.value = true
      // 判断数据来源：后端返回 source 字段或通过其他方式判断
      resultSource.value = res.data.source === 'redis' ? 'redis' : 'db'
    }
  } catch {
    hasResult.value = false
  } finally {
    queryLoading.value = false
  }
}

function resetQuery() {
  queryForm.value = { tableCode: '', version: '', dimensionKey: '' }
  queried.value = false
  hasResult.value = false
  queryResult.value = null
}
</script>
