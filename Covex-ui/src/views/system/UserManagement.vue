<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">用户管理</h2>
      <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="openAddDialog">新建用户</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadUsers">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="用户名/姓名/手机号" clearable :prefix-icon="Search" @clear="loadUsers" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card>
      <el-table :data="users" stripe border v-loading="loading">
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="{ row }">
            <template v-if="isAdmin">
              <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="primary" link @click="openRoleDialog(row)">分配角色</el-button>
            </template>
            <el-button size="small" type="info" link @click="viewPermissions(row)">查看权限</el-button>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 新建/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新建用户'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="登录账号" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="角色模板" prop="initialRole">
          <el-select v-model="form.initialRole" placeholder="选择角色" style="width: 100%">
            <el-option-group v-for="group in roleGroups" :key="group.label" :label="group.label">
              <el-option
                v-for="r in group.roles"
                :key="r.code"
                :label="r.name"
                :value="r.code"
                :disabled="['admin', 'sub_admin'].includes(r.code) && !isAdmin"
              />
            </el-option-group>
          </el-select>
          <div v-if="form.initialRole && roleDescMap[form.initialRole]" style="color: #909399; font-size: 12px; margin-top: 4px">
            {{ roleDescMap[form.initialRole] }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        用户：<strong>{{ selectedUser?.realName || selectedUser?.username }}</strong>
      </el-alert>
      <el-checkbox-group v-model="selectedRoleIds" v-loading="roleLoading">
        <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" style="display: block; margin-bottom: 8px">
          {{ role.roleName }} <span style="color: #999; font-size: 12px">({{ role.roleCode }})</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="handleSubmitRoles">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="用户权限" width="600px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        用户：<strong>{{ selectedUser?.realName || selectedUser?.username }}</strong>
      </el-alert>
      <div v-loading="permLoading">
        <template v-if="userPermissions.length > 0">
          <el-collapse>
            <el-collapse-item v-for="(perms, module) in groupedPermissions" :key="module" :title="module">
              <el-tag v-for="perm in perms" :key="perm.id" style="margin: 2px 4px" size="small">
                {{ perm.permissionName }}
              </el-tag>
            </el-collapse-item>
          </el-collapse>
        </template>
        <el-empty v-else description="暂无权限" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createUser, getUserPage, updateUser, toggleUserStatus,
  assignRoles, getUserRoles, getUserPermissions, getRoleList,
} from '@/api/user'
import type { User, Role, Permission } from '@/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('admin'))

// 13 角色分 4 组
const roleGroups = [
  { label: '系统管理组', roles: [
    { code: 'admin', name: '管理员' },
    { code: 'sub_admin', name: '副管理员' },
  ]},
  { label: '产品精算组', roles: [
    { code: 'product_mgr', name: '产品经理' },
    { code: 'actuary', name: '精算师' },
  ]},
  { label: '销售服务组', roles: [
    { code: 'channel_mgr', name: '渠道经理' },
    { code: 'agent', name: '代理人' },
    { code: 'service_rep', name: '客服录入员' },
  ]},
  { label: '核保核赔组', roles: [
    { code: 'underwriter', name: '核保员' },
    { code: 'conservation', name: '保全专员' },
    { code: 'claim_handler', name: '理赔员' },
    { code: 'investigator', name: '调查员' },
  ]},
  { label: '后台支撑组', roles: [
    { code: 'finance', name: '财务人员' },
    { code: 'compliance', name: '合规人员' },
  ]},
]

const roleDescMap: Record<string, string> = {
  admin: '超级管理员，拥有全部权限',
  sub_admin: '副管理员，可查看系统配置但不可创建用户',
  product_mgr: '负责产品创建与配置',
  actuary: '负责费率表设计与规则配置',
  channel_mgr: '负责渠道商管理与佣金',
  agent: '负责投保单录入和客户管理',
  service_rep: '负责客户信息录入，仅看自己的数据',
  underwriter: '负责投保单核保审批',
  conservation: '负责保单保全和续期管理',
  claim_handler: '负责理赔案件处理与审核',
  investigator: '负责理赔案件调查取证',
  finance: '负责佣金结算和财务报表',
  compliance: '负责产品合规审核和监管报告',
}

// ====== 列表状态 ======
const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// ====== 表单弹窗 ======
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  initialRole: '',
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}
// ====== 角色分配 ======
const roleDialogVisible = ref(false)
const roleLoading = ref(false)
const roleSubmitLoading = ref(false)
const selectedUser = ref<User | null>(null)
const allRoles = ref<Role[]>([])
const selectedRoleIds = ref<number[]>([])

// ====== 权限查看 ======
const permDialogVisible = ref(false)
const permLoading = ref(false)
const userPermissions = ref<Permission[]>([])

const groupedPermissions = computed(() => {
  const groups: Record<string, Permission[]> = {}
  for (const perm of userPermissions.value) {
    const mod = perm.module || '未分类'
    if (!groups[mod]) groups[mod] = []
    groups[mod].push(perm)
  }
  return groups
})

// ====== 工具函数 ======
function maskPhone(phone: string | null): string {
  if (!phone || phone.length < 7) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}


// ====== 数据加载 ======
async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserPage({ page: currentPage.value, size: pageSize.value, keyword: searchKeyword.value })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled by interceptor */ } finally {
    loading.value = false
  }
}

async function loadAllRoles() {
  try {
    const res = await getRoleList()
    allRoles.value = res.data || []
  } catch { /* handled by interceptor */ }
}

function resetSearch() {
  searchKeyword.value = ''
  currentPage.value = 1
  loadUsers()
}

// ====== 表单操作 ======
function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  form.value = { username: '', password: '', realName: '', phone: '', email: '', initialRole: '' }
  dialogVisible.value = true
}

function openEditDialog(row: User) {
  isEdit.value = true
  editingId.value = row.id
  form.value = {
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone || '',
    email: row.email || '',
    initialRole: '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && editingId.value) {
      const { password: _p, initialRole: _r, ...rest } = form.value
      await updateUser(editingId.value, rest)
      ElMessage.success('更新成功')
    } else {
      const { initialRole, ...userData } = form.value
      const res = await createUser(userData)
      ElMessage.success('创建成功')
      // 创建后自动分配角色
      if (initialRole && res.data?.id) {
        try {
          const rolesRes = await getRoleList()
          const allRolesList = rolesRes.data || []
          const matchedRole = allRolesList.find((r: Role) => r.roleCode === initialRole)
          if (matchedRole) {
            await assignRoles(res.data.id, [matchedRole.id])
          }
        } catch { /* role assignment failed, user can assign manually */ }
      }
    }
    dialogVisible.value = false
    await loadUsers()
  } catch { /* handled by interceptor */ } finally {
    submitLoading.value = false
  }
}

// ====== 状态切换 ======
async function handleToggleStatus(row: User) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.realName || row.username}」吗？`, '确认', { type: 'warning' })
    await toggleUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    await loadUsers()
  } catch { /* cancel or error */ }
}

// ====== 角色分配 ======
async function openRoleDialog(row: User) {
  selectedUser.value = row
  roleDialogVisible.value = true
  roleLoading.value = true
  try {
    await loadAllRoles()
    const res = await getUserRoles(row.id)
    const userRoleIds = (res.data || []).map((r: Role) => r.id)
    selectedRoleIds.value = userRoleIds
  } catch { /* handled by interceptor */ } finally {
    roleLoading.value = false
  }
}

async function handleSubmitRoles() {
  if (!selectedUser.value) return
  roleSubmitLoading.value = true
  try {
    await assignRoles(selectedUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch { /* handled by interceptor */ } finally {
    roleSubmitLoading.value = false
  }
}

// ====== 查看权限 ======
async function viewPermissions(row: User) {
  selectedUser.value = row
  permDialogVisible.value = true
  permLoading.value = true
  try {
    const res = await getUserPermissions(row.id)
    userPermissions.value = res.data || []
  } catch {
    userPermissions.value = []
  } finally {
    permLoading.value = false
  }
}

// ====== 初始化 ======
onMounted(() => { loadUsers() })
</script>
