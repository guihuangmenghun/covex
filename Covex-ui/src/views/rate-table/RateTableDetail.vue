<template>
  <div>
    <el-page-header @back="$router.push('/rate-table')" title="返回" content="费率表详情" />

    <!-- 基本信息卡片 -->
    <el-card style="margin-top: 20px" v-loading="loading">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="表编码">{{ rateTable.rateTableCode }}</el-descriptions-item>
        <el-descriptions-item label="表名称">{{ rateTable.rateTableName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ rateTable.version }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ rateTable.effectiveDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ rateTable.expiryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ rateTable.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 16px; display: flex; gap: 8px">
        <el-button type="success" @click="handleLoadToRedis">加载到 Redis</el-button>
        <el-button type="warning" @click="handleEvictRedis">清除缓存</el-button>
      </div>
    </el-card>

    <!-- 行数据表格 -->
    <el-card style="margin-top: 16px">
      <div style="display: flex; justify-content: space-between; margin-bottom: 12px">
        <h3 style="margin: 0">行数据</h3>
        <el-button type="primary" :icon="Upload" @click="importDialogVisible = true">批量导入</el-button>
      </div>
      <el-table :data="rows" stripe border v-loading="rowsLoading" size="small">
        <el-table-column prop="dimensionKey" label="维度键" min-width="200" />
        <el-table-column prop="rateValue" label="费率值" width="120" align="right">
          <template #default="{ row }">{{ row.rateValue.toFixed(6) }}</template>
        </el-table-column>
        <el-table-column prop="dimensionJson" label="维度详情" min-width="250">
          <template #default="{ row }">
            <span v-if="row.dimensionJson" style="font-size: 12px; color: #606266">
              {{ JSON.stringify(row.dimensionJson) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="extraValues" label="扩展值" min-width="200">
          <template #default="{ row }">
            <span v-if="row.extraValues" style="font-size: 12px; color: #606266">
              {{ JSON.stringify(row.extraValues) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入行数据" width="700px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 12px">
        请输入 JSON 格式的行数据数组，每个元素包含 dimensionKey (必填)、rateValue (必填)、dimensionJson (可选)、extraValues (可选)。
      </el-alert>
      <el-input
        v-model="importJsonText"
        type="textarea"
        :rows="12"
        placeholder='[{"dimensionKey": "age:30|gender:1", "rateValue": 0.0015, "dimensionJson": {"age": 30, "gender": 1}}]'
      />
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getRateTableById, getRateTableRows, importRateTableRows, loadRateTable, evictRateTable } from '@/api/rateTable'
import type { RateTable, RateTableRow } from '@/types'

const route = useRoute()
const rateTableId = Number(route.params.id)

const loading = ref(false)
const rateTable = ref<RateTable>({} as RateTable)
const rowsLoading = ref(false)
const rows = ref<RateTableRow[]>([])

const importDialogVisible = ref(false)
const importLoading = ref(false)
const importJsonText = ref('')

async function loadRateTable() {
  loading.value = true
  try {
    const res = await getRateTableById(rateTableId)
    rateTable.value = res.data || ({} as RateTable)
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function loadRows() {
  rowsLoading.value = true
  try {
    const res = await getRateTableRows(rateTableId)
    rows.value = res.data || []
  } catch {
    rows.value = []
  } finally {
    rowsLoading.value = false
  }
}

async function handleLoadToRedis() {
  try {
    await ElMessageBox.confirm(
      `确定将费率表「${rateTable.value.rateTableName}」加载到 Redis 缓存吗？`,
      '确认加载',
      { type: 'info' }
    )
    await loadRateTable({ tableCode: rateTable.value.rateTableCode, version: rateTable.value.version })
    ElMessage.success('已加载到 Redis')
  } catch { /* cancel */ }
}

async function handleEvictRedis() {
  try {
    await ElMessageBox.confirm(
      `确定清除费率表「${rateTable.value.rateTableName}」的 Redis 缓存吗？`,
      '确认清除',
      { type: 'warning' }
    )
    await evictRateTable({ tableCode: rateTable.value.rateTableCode, version: rateTable.value.version })
    ElMessage.success('缓存已清除')
  } catch { /* cancel */ }
}

async function handleImport() {
  if (!importJsonText.value.trim()) {
    ElMessage.warning('请输入 JSON 数据')
    return
  }

  let parsedRows: any[]
  try {
    parsedRows = JSON.parse(importJsonText.value)
    if (!Array.isArray(parsedRows)) {
      ElMessage.error('JSON 必须是数组格式')
      return
    }
  } catch {
    ElMessage.error('JSON 格式不正确，请检查')
    return
  }

  // 验证必填字段
  for (let i = 0; i < parsedRows.length; i++) {
    const row = parsedRows[i]
    if (!row.dimensionKey || row.rateValue === undefined || row.rateValue === null) {
      ElMessage.error(`第 ${i + 1} 行缺少必填字段 (dimensionKey 或 rateValue)`)
      return
    }
  }

  importLoading.value = true
  try {
    await importRateTableRows(rateTableId, parsedRows)
    ElMessage.success(`成功导入 ${parsedRows.length} 条数据`)
    importDialogVisible.value = false
    importJsonText.value = ''
    await loadRows()
  } catch { /* handled */ } finally {
    importLoading.value = false
  }
}

onMounted(async () => {
  await loadRateTable()
  await loadRows()
})
</script>
