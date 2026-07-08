<template>
  <div>
    <el-page-header @back="$router.push('/channel')" title="返回" content="渠道商详情" />

    <el-card style="margin-top: 20px" v-loading="loading">
      <el-tabs v-model="activeTab">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="渠道编码">{{ channel.channelCode }}</el-descriptions-item>
            <el-descriptions-item label="渠道名称">{{ channel.channelName }}</el-descriptions-item>
            <el-descriptions-item label="渠道类型">{{ channelTypeLabel(channel.channelType) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(channel.status)">{{ statusLabel(channel.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="许可证号">{{ channel.licenseNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="许可证到期">{{ channel.licenseExpiry || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ channel.contactName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ channel.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="联系邮箱">{{ channel.contactEmail || '-' }}</el-descriptions-item>
            <el-descriptions-item label="区域编码">{{ channel.regionCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同编号">{{ channel.contractNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="合同期限">{{ channel.contractStart || '-' }} ~ {{ channel.contractEnd || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top: 16px; display: flex; gap: 8px">
            <el-button type="primary" @click="editDialogVisible = true">编辑</el-button>
            <el-button v-if="channel.status === 1" type="success" @click="handleApprove">审核通过</el-button>
            <el-button v-if="channel.status === 1" type="danger" @click="handleReject">驳回</el-button>
            <el-button type="warning" @click="openStatusDialog">切换状态</el-button>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 账号管理 -->
        <el-tab-pane label="账号管理" name="user">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
            <el-button type="primary" :icon="Plus" @click="openUserDialog()">新增账号</el-button>
          </div>
          <el-table :data="users" stripe border size="small">
            <el-table-column prop="username" label="用户名" width="130" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column prop="agentLicenseNo" label="代理许可证号" width="150">
              <template #default="{ row }">{{ row.agentLicenseNo || '-' }}</template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="130">
              <template #default="{ row }">{{ row.phone || '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginAt" label="最后登录" width="170">
              <template #default="{ row }">{{ row.lastLoginAt || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="openUserDialog(row)">编辑</el-button>
                <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="handleUserStatus(row)">
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: 产品授权 -->
        <el-tab-pane label="产品授权" name="product">
          <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
            <el-button type="primary" :icon="Plus" @click="authDialogVisible = true">授权产品</el-button>
          </div>
          <el-table :data="products" stripe border size="small">
            <el-table-column prop="productId" label="产品ID" width="100" align="center" />
            <el-table-column prop="firstYearRate" label="首年费率" width="120" align="right">
              <template #default="{ row }">{{ (row.firstYearRate * 100).toFixed(2) }}%</template>
            </el-table-column>
            <el-table-column prop="renewalRate" label="续期费率" width="120" align="right">
              <template #default="{ row }">{{ (row.renewalRate * 100).toFixed(2) }}%</template>
            </el-table-column>
            <el-table-column prop="saleRegion" label="销售区域" width="120">
              <template #default="{ row }">{{ row.saleRegion || '-' }}</template>
            </el-table-column>
            <el-table-column prop="isActive" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">
                  {{ row.isActive === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="授权时间" width="170" />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button size="small" type="danger" link @click="handleRevokeAuth(row)">撤销</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑渠道商" width="600px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="渠道编码"><el-input v-model="editForm.channelCode" disabled /></el-form-item>
        <el-form-item label="渠道名称" prop="channelName"><el-input v-model="editForm.channelName" /></el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-select v-model="editForm.channelType" style="width: 100%">
            <el-option label="代理人" :value="1" />
            <el-option label="经纪人" :value="2" />
            <el-option label="银保" :value="3" />
            <el-option label="互联网" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName"><el-input v-model="editForm.contactName" /></el-form-item>
        <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="editForm.contactPhone" /></el-form-item>
        <el-form-item label="联系邮箱"><el-input v-model="editForm.contactEmail" /></el-form-item>
        <el-form-item label="区域编码"><el-input v-model="editForm.regionCode" /></el-form-item>
        <el-form-item label="合同编号"><el-input v-model="editForm.contractNo" /></el-form-item>
        <el-form-item label="合同起始">
          <el-date-picker v-model="editForm.contractStart" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="合同到期">
          <el-date-picker v-model="editForm.contractEnd" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 状态切换弹窗 -->
    <el-dialog v-model="statusDialogVisible" title="切换状态" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(channel.status)">{{ statusLabel(channel.status) }}</el-tag>
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

    <!-- 账号弹窗 -->
    <el-dialog v-model="userDialogVisible" :title="editingUser ? '编辑账号' : '新增账号'" width="500px" destroy-on-close>
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!editingUser" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="userForm.realName" /></el-form-item>
        <el-form-item label="代理许可证号"><el-input v-model="userForm.agentLicenseNo" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="userForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="userLoading" @click="handleUserSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 授权产品弹窗 -->
    <el-dialog v-model="authDialogVisible" title="授权产品" width="500px" destroy-on-close>
      <el-form ref="authFormRef" :model="authForm" :rules="authRules" label-width="100px">
        <el-form-item label="选择产品" prop="productId">
          <el-select v-model="authForm.productId" placeholder="搜索并选择产品" filterable style="width: 100%">
            <el-option v-for="p in allProducts" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="首年费率" prop="firstYearRate">
          <el-input-number v-model="authForm.firstYearRate" :min="0" :max="1" :precision="4" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续期费率" prop="renewalRate">
          <el-input-number v-model="authForm.renewalRate" :min="0" :max="1" :precision="4" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="销售区域">
          <el-input v-model="authForm.saleRegion" placeholder="如 310000（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="authLoading" @click="handleAuthSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getChannelById, updateChannel, updateChannelStatus, approveChannel, rejectChannel,
  getChannelUsers, createChannelUser, updateChannelUser, toggleChannelUserStatus,
  getChannelProducts, authorizeProduct, revokeProductAuth,
} from '@/api/channel'
import { getProductList } from '@/api/product'
import type { Channel, ChannelUser, ChannelProduct, Product } from '@/types'

const route = useRoute()
const channelId = Number(route.params.id)

const loading = ref(false)
const channel = ref<Channel>({} as Channel)
const activeTab = ref('info')

const users = ref<ChannelUser[]>([])
const products = ref<ChannelProduct[]>([])
const allProducts = ref<Product[]>([])

// 编辑
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref({
  channelCode: '', channelName: '', channelType: 1, contactName: '', contactPhone: '',
  contactEmail: '', regionCode: '', contractNo: '', contractStart: null as string | null, contractEnd: null as string | null,
})
const editRules: FormRules = {
  channelName: [{ required: true, message: '请输入', trigger: 'blur' }],
  channelType: [{ required: true, message: '请选择', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入', trigger: 'blur' }],
}

// 状态
const statusDialogVisible = ref(false)
const statusLoading = ref(false)
const newStatus = ref(1)

// 账号
const userDialogVisible = ref(false)
const userLoading = ref(false)
const userFormRef = ref<FormInstance>()
const editingUser = ref<ChannelUser | null>(null)
const userForm = ref({ username: '', password: '', realName: '', agentLicenseNo: '', phone: '' })
const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
}

// 授权
const authDialogVisible = ref(false)
const authLoading = ref(false)
const authFormRef = ref<FormInstance>()
const authForm = ref({ productId: null as number | null, firstYearRate: 0.1, renewalRate: 0.05, saleRegion: '' })
const authRules: FormRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  firstYearRate: [{ required: true, message: '请输入首年费率', trigger: 'blur' }],
  renewalRate: [{ required: true, message: '请输入续期费率', trigger: 'blur' }],
}

function channelTypeLabel(type: number): string {
  const map: Record<number, string> = { 1: '代理人', 2: '经纪人', 3: '银保', 4: '互联网', 5: '其他' }
  return map[type] || '未知'
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 0: '停用', 1: '待审核', 2: '已签约', 3: '已终止', 4: '已冻结', 5: '已驳回' }
  return map[status] || '未知'
}

function statusTagType(status: number): string {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'warning', 5: 'danger' }
  return map[status] || 'info'
}

async function loadChannel() {
  loading.value = true
  try {
    const res = await getChannelById(channelId)
    channel.value = res.data || ({} as Channel)
    editForm.value = {
      channelCode: channel.value.channelCode,
      channelName: channel.value.channelName,
      channelType: channel.value.channelType,
      contactName: channel.value.contactName,
      contactPhone: channel.value.contactPhone,
      contactEmail: channel.value.contactEmail || '',
      regionCode: channel.value.regionCode || '',
      contractNo: channel.value.contractNo || '',
      contractStart: channel.value.contractStart,
      contractEnd: channel.value.contractEnd,
    }
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function loadUsers() {
  try { const r = await getChannelUsers(channelId); users.value = r.data || [] } catch { users.value = [] }
}

async function loadProducts() {
  try { const r = await getChannelProducts(channelId); products.value = r.data || [] } catch { products.value = [] }
}

async function loadAllProducts() {
  try { const r = await getProductList(); allProducts.value = r.data?.records || [] } catch { allProducts.value = [] }
}

function openStatusDialog() {
  newStatus.value = channel.value.status
  statusDialogVisible.value = true
}

async function handleStatusChange() {
  statusLoading.value = true
  try {
    await updateChannelStatus(channelId, newStatus.value)
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    await loadChannel()
  } catch { /* handled */ } finally {
    statusLoading.value = false
  }
}

async function handleApprove() {
  try {
    await ElMessageBox.confirm(`确定审核通过渠道商「${channel.value.channelName}」吗？`, '确认', { type: 'success' })
    await approveChannel(channelId)
    ElMessage.success('审核通过')
    await loadChannel()
  } catch { /* handled */ }
}

async function handleReject() {
  try {
    await ElMessageBox.confirm(`确定驳回渠道商「${channel.value.channelName}」吗？`, '确认驳回', { type: 'warning' })
    await rejectChannel(channelId)
    ElMessage.success('已驳回')
    await loadChannel()
  } catch { /* handled */ }
}

async function handleEdit() {
  const v = await editFormRef.value?.validate().catch(() => false)
  if (!v) return
  editLoading.value = true
  try {
    await updateChannel(channelId, editForm.value as any)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    await loadChannel()
  } catch { /* handled */ } finally {
    editLoading.value = false
  }
}

function openUserDialog(row?: ChannelUser) {
  if (row) {
    editingUser.value = row
    userForm.value = { username: row.username, password: '', realName: row.realName, agentLicenseNo: row.agentLicenseNo || '', phone: row.phone || '' }
  } else {
    editingUser.value = null
    userForm.value = { username: '', password: '', realName: '', agentLicenseNo: '', phone: '' }
  }
  userDialogVisible.value = true
}

async function handleUserSubmit() {
  const v = await userFormRef.value?.validate().catch(() => false)
  if (!v) return
  userLoading.value = true
  try {
    if (editingUser.value) {
      await updateChannelUser(channelId, editingUser.value.id, userForm.value)
      ElMessage.success('更新成功')
    } else {
      await createChannelUser(channelId, userForm.value)
      ElMessage.success('创建成功')
    }
    userDialogVisible.value = false
    await loadUsers()
  } catch { /* handled */ } finally {
    userLoading.value = false
  }
}

async function handleUserStatus(row: ChannelUser) {
  const newSt = row.status === 1 ? 0 : 1
  try {
    await toggleChannelUserStatus(channelId, row.id, newSt)
    ElMessage.success('状态更新成功')
    await loadUsers()
  } catch { /* handled */ }
}

async function handleAuthSubmit() {
  const v = await authFormRef.value?.validate().catch(() => false)
  if (!v) return
  if (!authForm.value.productId) return
  authLoading.value = true
  try {
    await authorizeProduct(channelId, {
      productId: authForm.value.productId,
      firstYearRate: authForm.value.firstYearRate,
      renewalRate: authForm.value.renewalRate,
    })
    ElMessage.success('授权成功')
    authDialogVisible.value = false
    await loadProducts()
  } catch { /* handled */ } finally {
    authLoading.value = false
  }
}

async function handleRevokeAuth(row: ChannelProduct) {
  try {
    await ElMessageBox.confirm('确定撤销此产品授权吗？', '确认', { type: 'warning' })
    await revokeProductAuth(channelId, row.productId)
    ElMessage.success('撤销成功')
    await loadProducts()
  } catch { /* cancel or handled */ }
}

onMounted(async () => {
  await Promise.all([loadChannel(), loadUsers(), loadProducts(), loadAllProducts()])
})
</script>
