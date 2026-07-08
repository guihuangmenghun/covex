<template>
  <div>
    <h2 style="margin: 0 0 16px">核保工作台</h2>

    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadTasks">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="投保单号" clearable style="width: 180px" @clear="loadTasks" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="loadTasks">
            <el-option label="待核保" :value="2" />
            <el-option label="核保中" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tasks" stripe border v-loading="loading">
        <el-table-column prop="proposalNo" label="投保单号" width="200" />
        <el-table-column label="产品" min-width="160">
          <template #default="{ row }">{{ row.productSnapshot?.productName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="投保人" min-width="100" align="center" />
        <el-table-column prop="totalSumInsured" label="总保额" width="130" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalSumInsured) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'warning' : row.status === 3 ? 'warning' : 'info'" size="small">
              {{ row.status === 2 ? '待核保' : row.status === 3 ? '核保中' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitAt" label="提交时间" width="170">
          <template #default="{ row }">{{ row.submitAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/underwriting/${row.id}`)">详情</el-button>
            <el-button size="small" type="success" link @click="handleAutoUw(row)">自动核保</el-button>
            <el-button size="small" type="warning" link @click="openManualDialog(row)">人工核保</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>

    <!-- 人工核保弹窗 -->
    <el-dialog v-model="manualDialogVisible" title="人工核保" width="500px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        投保单：<strong>{{ selectedProposal?.proposalNo }}</strong>
      </el-alert>
      <el-form ref="manualFormRef" :model="manualForm" :rules="manualRules" label-width="100px">
        <el-form-item label="核保结论" prop="uwResult">
          <el-select v-model="manualForm.uwResult" style="width: 100%" @change="onUwResultChange">
            <el-option label="标准体（通过）" :value="1" />
            <el-option label="加费" :value="2" />
            <el-option label="除外" :value="3" />
            <el-option label="延期" :value="4" />
            <el-option label="拒保" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="manualForm.uwResult === 2" label="加费金额" prop="loadingAmount">
          <el-input-number v-model="manualForm.loadingAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="manualForm.uwResult === 3" label="除外责任" prop="exclusionDesc">
          <el-input v-model="manualForm.exclusionDesc" type="textarea" :rows="2" placeholder="描述除外责任内容" />
        </el-form-item>
        <el-form-item label="核保备注">
          <el-input v-model="manualForm.comment" type="textarea" :rows="3" placeholder="核保备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="manualLoading" @click="handleManualUw">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProposalPage } from '@/api/proposal'
import { autoUnderwrite, manualUnderwrite } from '@/api/underwriting'
import type { Proposal } from '@/types'

const loading = ref(false)
const tasks = ref<Proposal[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(2)

const manualDialogVisible = ref(false)
const manualLoading = ref(false)
const manualFormRef = ref<FormInstance>()
const selectedProposal = ref<Proposal | null>(null)

const manualForm = ref({
  uwResult: 1,
  loadingAmount: null as number | null,
  exclusionDesc: '',
  comment: '',
})

const manualRules: FormRules = {
  uwResult: [{ required: true, message: '请选择核保结论', trigger: 'change' }],
}

function formatMoney(val: number | null | undefined): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function onUwResultChange() {
  manualForm.value.loadingAmount = null
  manualForm.value.exclusionDesc = ''
}

async function loadTasks() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getProposalPage(params)
    tasks.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = 2
  currentPage.value = 1
  loadTasks()
}

async function handleAutoUw(row: Proposal) {
  try {
    await ElMessageBox.confirm(
      `确定对投保单「${row.proposalNo}」执行自动核保吗？`,
      '确认自动核保',
      { type: 'info' }
    )
    await autoUnderwrite(row.id)
    ElMessage.success('自动核保完成')
    await loadTasks()
  } catch { /* cancel or handled */ }
}

function openManualDialog(row: Proposal) {
  selectedProposal.value = row
  manualForm.value = { uwResult: 1, loadingAmount: null, exclusionDesc: '', comment: '' }
  manualDialogVisible.value = true
}

async function handleManualUw() {
  const valid = await manualFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!selectedProposal.value) return

  manualLoading.value = true
  try {
    await manualUnderwrite(selectedProposal.value.id, {
      uwResult: manualForm.value.uwResult,
      loadingAmount: manualForm.value.uwResult === 2 ? (manualForm.value.loadingAmount || undefined) : undefined,
      exclusionDesc: manualForm.value.uwResult === 3 ? manualForm.value.exclusionDesc : undefined,
      comment: manualForm.value.comment || undefined,
      operator: 'admin',
    })
    ElMessage.success('人工核保完成')
    manualDialogVisible.value = false
    await loadTasks()
  } catch { /* handled */ } finally {
    manualLoading.value = false
  }
}

onMounted(() => { loadTasks() })
</script>
