<template>
  <div>
    <h2 style="margin: 0 0 16px">渠道管理</h2>

    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadChannels">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="渠道名称/编码" clearable style="width: 180px" @clear="loadChannels" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px" @change="loadChannels">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="冻结" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadChannels">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
        <el-button type="primary" @click="$router.push('/channel/create')">创建渠道商</el-button>
      </div>

      <el-table :data="channels" stripe border v-loading="loading">
        <el-table-column prop="channelCode" label="渠道编码" width="130" />
        <el-table-column prop="channelName" label="渠道名称" min-width="160" />
        <el-table-column prop="channelType" label="渠道类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ channelTypeLabel(row.channelType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractEnd" label="合同到期" width="120">
          <template #default="{ row }">{{ row.contractEnd || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/channel/${row.id}`)">详情</el-button>
            <el-button size="small" type="success" link @click="openStatusDialog(row)">状态</el-button>
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
          @size-change="loadChannels"
          @current-change="loadChannels"
        />
      </div>
    </el-card>

    <!-- 状态切换弹窗 -->
    <el-dialog v-model="statusDialogVisible" title="切换渠道商状态" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(selectedChannel?.status || 0)">{{ statusLabel(selectedChannel?.status || 0) }}</el-tag>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="newStatus" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
            <el-option label="冻结" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="handleStatusChange">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChannelPage, updateChannelStatus } from '@/api/channel'
import type { Channel } from '@/types'
import { useDictStore } from '@/stores/dict'

const dictStore = useDictStore()

const loading = ref(false)
const channels = ref<Channel[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)

const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const selectedChannel = ref<Channel | null>(null)
const newStatus = ref(1)

function channelTypeLabel(type: number): string {
  return dictStore.getDictLabel('channel_type', String(type))
}

function statusLabel(status: number): string {
  return dictStore.getDictLabel('channel_status', String(status))
}

function statusTagType(status: number): string {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

async function loadChannels() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterStatus.value !== undefined) params.status = filterStatus.value
    const res = await getChannelPage(params)
    channels.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = undefined
  currentPage.value = 1
  loadChannels()
}

function openStatusDialog(row: Channel) {
  selectedChannel.value = row
  newStatus.value = row.status
  statusDialogVisible.value = true
}

async function handleStatusChange() {
  if (!selectedChannel.value) return
  statusLoading.value = true
  try {
    await updateChannelStatus(selectedChannel.value.id, newStatus.value)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    await loadChannels()
  } catch { /* handled */ } finally {
    statusLoading.value = false
  }
}

onMounted(() => { loadChannels() })
</script>
