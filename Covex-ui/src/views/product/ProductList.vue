<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">产品列表</h2>
      <el-button type="primary" @click="$router.push('/product/create')">创建产品</el-button>
    </div>

    <el-table :data="products" stripe border>
      <el-table-column prop="productCode" label="产品编码" width="120" />
      <el-table-column prop="productName" label="产品名称" />
      <el-table-column prop="productType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTagMap[row.productType]?.type || 'info'">
            {{ typeTagMap[row.productType]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagMap[row.status]?.type || 'info'">
            {{ statusTagMap[row.status]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/product/${row.id}`)">详情</el-button>
          <el-button size="small" type="primary" @click="$router.push(`/product/${row.id}/edit`)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const typeTagMap: Record<string, { label: string; type: string }> = {
  '1': { label: '寿险', type: 'primary' },
  '2': { label: '意外险', type: 'warning' },
  '3': { label: '健康险', type: 'success' },
  '4': { label: '车险', type: 'danger' },
  '5': { label: '财产险', type: 'info' },
}

const statusTagMap: Record<string, { label: string; type: string }> = {
  '1': { label: '草稿', type: 'info' },
  '2': { label: '待审批', type: 'warning' },
  '3': { label: '已发布', type: 'success' },
  '4': { label: '已冻结', type: 'danger' },
}

const products = ref([
  { id: 1, productCode: 'P001', productName: '定期寿险A', productType: '1', version: '1.0.0', status: '3' },
  { id: 2, productCode: 'P002', productName: '百万医疗险', productType: '3', version: '2.1.0', status: '3' },
  { id: 3, productCode: 'P003', productName: '车险综合险', productType: '4', version: '1.0.0', status: '2' },
  { id: 4, productCode: 'P004', productName: '家庭财产险', productType: '5', version: '1.0.0', status: '1' },
])
</script>
