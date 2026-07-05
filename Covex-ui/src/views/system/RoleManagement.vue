<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">角色管理</h2>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">新建角色</el-button>
    </div>

    <el-card>
      <el-table :data="roles" stripe border v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="340" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button size="small" type="primary" link @click="openPermDialog(row)">分配权限</el-button>
            <el-button size="small" type="warning" link @click="goDataScope(row)">数据范围</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新建角色'" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="如 admin" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如 管理员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        角色：<strong>{{ selectedRole?.roleName }}</strong>（{{ selectedRole?.roleCode }}）
      </el-alert>

      <div v-loading="permLoading" style="max-height: 400px; overflow-y: auto">
        <el-checkbox
          v-model="selectAll"
          :indeterminate="isIndeterminate"
          @change="handleSelectAll"
          style="margin-bottom: 12px; font-weight: bold"
        >全选 / 反选</el-checkbox>

        <div v-for="(perms, module) in permissionModules" :key="module" style="margin-bottom: 12px">
          <div style="font-weight: 500; margin-bottom: 6px; color: #303133">{{ module }}</div>
          <el-checkbox-group v-model="selectedPermIds" style="display: flex; flex-wrap: wrap; gap: 4px">
            <el-checkbox v-for="perm in perms" :key="perm.id" :value="perm.id">
              {{ perm.permissionName }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <el-empty v-if="Object.keys(permissionModules).length === 0" description="暂无权限数据" :image-size="60" />
      </div>

      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handleSubmitPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createRole, getRoleList, updateRole, deleteRole,
  assignPermissions, getRolePermissions, getPermissionModules,
} from '@/api/user'
import type { Role, Permission } from '@/types'

const router = useRouter()

// ====== 列表状态 ======
const loading = ref(false)
const roles = ref<Role[]>([])

// ====== 表单弹窗 ======
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = ref({ roleCode: '', roleName: '', description: '' })
const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

// ====== 权限分配 ======
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitLoading = ref(false)
const selectedRole = ref<Role | null>(null)
const permissionModules = ref<Record<string, Permission[]>>({})
const selectedPermIds = ref<number[]>([])

const allPermIds = computed(() =>
  Object.values(permissionModules.value).flat().map((p) => p.id)
)
const selectAll = ref(false)
const isIndeterminate = ref(false)

watch(selectedPermIds, (val) => {
  const total = allPermIds.value.length
  selectAll.value = val.length === total && total > 0
  isIndeterminate.value = val.length > 0 && val.length < total
}, { deep: true })

function handleSelectAll(val: boolean | string | number) {
  selectedPermIds.value = val ? [...allPermIds.value] : []
}

// ====== 数据加载 ======
async function loadRoles() {
  loading.value = true
  try {
    const res = await getRoleList()
    roles.value = res.data || []
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

// ====== 表单操作 ======
function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  form.value = { roleCode: '', roleName: '', description: '' }
  dialogVisible.value = true
}

function openEditDialog(row: Role) {
  isEdit.value = true
  editingId.value = row.id
  form.value = { roleCode: row.roleCode, roleName: row.roleName, description: row.description || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value && editingId.value) {
      await updateRole(editingId.value, { roleName: form.value.roleName, description: form.value.description })
      ElMessage.success('更新成功')
    } else {
      await createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadRoles()
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '确认删除', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    await loadRoles()
  } catch { /* cancel */ }
}

// ====== 权限分配 ======
async function openPermDialog(row: Role) {
  selectedRole.value = row
  permDialogVisible.value = true
  permLoading.value = true
  try {
    const [modulesRes, rolePermsRes] = await Promise.all([
      getPermissionModules(),
      getRolePermissions(row.id),
    ])
    permissionModules.value = modulesRes.data || {}
    selectedPermIds.value = (rolePermsRes.data || []).map((p: Permission) => p.id)
  } catch { /* handled */ } finally {
    permLoading.value = false
  }
}

async function handleSubmitPerms() {
  if (!selectedRole.value) return
  permSubmitLoading.value = true
  try {
    await assignPermissions(selectedRole.value.id, selectedPermIds.value)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch { /* handled */ } finally {
    permSubmitLoading.value = false
  }
}

// ====== 跳转数据范围 ======
function goDataScope(row: Role) {
  router.push({ path: '/system/data-scope', query: { roleId: String(row.id) } })
}

// ====== 初始化 ======
onMounted(() => { loadRoles() })
</script>
