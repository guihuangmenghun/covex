<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">客户管理</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/customer/create')">新建客户</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="loadCustomers">
        <el-form-item>
          <el-input v-model="searchKeyword" placeholder="姓名/证件号/手机号" clearable :prefix-icon="Search" @clear="loadCustomers" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCustomers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 客户列表 -->
    <el-card>
      <el-table :data="customers" stripe border v-loading="loading">
        <el-table-column prop="customerCode" label="客户编号" width="130" />
        <el-table-column prop="customerName" label="姓名" width="120" />
        <el-table-column prop="idType" label="证件类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ idTypeLabel(row.idType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="idNo" label="证件号" width="180">
          <template #default="{ row }">{{ maskIdNo(row.idNo) }}</template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/customer/${row.id}`)">详情</el-button>
            <el-button size="small" type="primary" link @click="$router.push(`/customer/${row.id}?edit=true`)">编辑</el-button>
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
          @size-change="loadCustomers"
          @current-change="loadCustomers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { getCustomerPage } from '@/api/customer'
import type { Customer } from '@/types'

const loading = ref(false)
const customers = ref<Customer[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

function maskPhone(phone: string | null): string {
  if (!phone || phone.length < 7) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

function maskIdNo(idNo: string | null): string {
  if (!idNo || idNo.length < 8) return idNo || '-'
  return idNo.slice(0, 4) + '****' + idNo.slice(-4)
}

function idTypeLabel(type: number): string {
  const map: Record<number, string> = { 1: '身份证', 2: '护照', 3: '军官证', 4: '其他' }
  return map[type] || '未知'
}

async function loadCustomers() {
  loading.value = true
  try {
    const res = await getCustomerPage({ page: currentPage.value, size: pageSize.value, keyword: searchKeyword.value })
    customers.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchKeyword.value = ''
  currentPage.value = 1
  loadCustomers()
}

onMounted(() => { loadCustomers() })
</script>
