<template>
  <div>
    <h2 style="margin: 0 0 16px">数据范围管理</h2>

    <el-card>
      <el-form :inline="true" style="margin-bottom: 16px">
        <el-form-item label="选择角色">
          <el-select
            v-model="selectedRoleId"
            placeholder="请选择角色"
            style="width: 300px"
            @change="loadScope"
          >
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id">
              {{ role.roleName }} <span style="color: #999; font-size: 12px">({{ role.roleCode }})</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="selectedRoleId">
        <el-divider content-position="left">当前配置</el-divider>
        <el-form v-loading="scopeLoading" label-width="120px">
          <el-form-item label="范围类型">
            <el-radio-group v-model="scopeType" @change="handleScopeTypeChange">
              <el-radio :value="1">全部数据</el-radio>
              <el-radio :value="2">本部门数据</el-radio>
              <el-radio :value="3">自定义数据</el-radio>
              <el-radio :value="4">仅本人数据</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="scopeType === 3" label="自定义范围">
            <el-checkbox-group v-model="customScopes">
              <el-checkbox v-for="scope in customScopeOptions" :key="scope" :value="scope" style="display: block; margin-bottom: 4px">
                {{ scope }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>

        <div style="text-align: right; margin-top: 16px">
          <el-button type="primary" :loading="submitLoading" @click="handleSave">保存配置</el-button>
        </div>
      </div>

      <el-empty v-else description="请先选择角色" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoleList, getDataScope, setDataScope } from '@/api/user'
import type { Role } from '@/types'

const route = useRoute()

const roles = ref<Role[]>([])
const selectedRoleId = ref<number | null>(null)
const scopeLoading = ref(false)
const submitLoading = ref(false)
const scopeType = ref<number>(1)
const customScopes = ref<string[]>([])

const customScopeOptions = [
  '产品管理', '投保单管理', '保单管理', '理赔管理',
  '客户管理', '渠道管理', '佣金管理', '核保管理',
]

async function loadRoles() {
  try {
    const res = await getRoleList()
    roles.value = res.data || []
    // 从路由 query 自动选中
    const queryRoleId = route.query.roleId
    if (queryRoleId && roles.value.some((r) => r.id === Number(queryRoleId))) {
      selectedRoleId.value = Number(queryRoleId)
      await loadScope()
    }
  } catch { /* handled */ }
}

async function loadScope() {
  if (!selectedRoleId.value) return
  scopeLoading.value = true
  try {
    const res = await getDataScope(selectedRoleId.value)
    const data = res.data
    if (data) {
      scopeType.value = data.scopeType || 1
      customScopes.value = data.customScopes || []
    } else {
      scopeType.value = 1
      customScopes.value = []
    }
  } catch {
    scopeType.value = 1
    customScopes.value = []
  } finally {
    scopeLoading.value = false
  }
}

function handleScopeTypeChange() {
  if (scopeType.value !== 3) {
    customScopes.value = []
  }
}

async function handleSave() {
  if (!selectedRoleId.value) return
  submitLoading.value = true
  try {
    await setDataScope(selectedRoleId.value, {
      scopeType: scopeType.value,
      customScopes: scopeType.value === 3 ? customScopes.value : [],
    })
    ElMessage.success('保存成功')
  } catch { /* handled */ } finally {
    submitLoading.value = false
  }
}

onMounted(() => { loadRoles() })
</script>
