<template>
  <div>
    <el-page-header @back="$router.back()" :title="isEdit ? '编辑产品' : '产品详情'" />

    <el-card style="margin-top: 20px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品编码">{{ product.productCode }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ product.version }}</el-descriptions-item>
        <el-descriptions-item label="产品名称" :span="2">{{ product.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品类型">{{ typeLabel }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag.type">{{ statusTag.label }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>保障责任</template>
      <el-table :data="coverages" stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sumInsured" label="保额" width="120" />
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>缴费计划</template>
      <el-table :data="premiums" stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="frequency" label="频率" width="100" />
        <el-table-column prop="periodPremium" label="每期保费" width="120" />
      </el-table>
    </el-card>

    <div style="margin-top: 20px; text-align: right" v-if="isEdit">
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const isEdit = computed(() => route.name === 'product-edit' || route.name === 'product-create')

const typeLabels: Record<string, string> = {
  '1': '寿险', '2': '意外险', '3': '健康险', '4': '车险', '5': '财产险',
}
const statusLabels: Record<string, { label: string; type: string }> = {
  '1': { label: '草稿', type: 'info' },
  '3': { label: '已发布', type: 'success' },
}

const product = ref({
  productCode: 'P001',
  productName: '定期寿险A',
  productType: '1',
  version: '1.0.0',
  status: '3',
})

const typeLabel = computed(() => typeLabels[product.value.productType] || '未知')
const statusTag = computed(() => statusLabels[product.value.status] || { label: '未知', type: 'info' })

const coverages = ref([
  { code: 'C001', name: '身故保险金', sumInsured: '500,000' },
  { code: 'C002', name: '全残保险金', sumInsured: '500,000' },
])

const premiums = ref([
  { code: 'PP01', name: '20年交', frequency: '年交', periodPremium: '3,680.00' },
])

function handleSave() {
  ElMessage.success('保存成功')
}
</script>
