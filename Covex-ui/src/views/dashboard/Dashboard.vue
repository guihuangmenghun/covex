<template>
  <div>
    <h2>工作台</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>产品总数</template>
          <div style="font-size: 32px; text-align: center">12</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>有效保单</template>
          <div style="font-size: 32px; text-align: center">1,234</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>待核保</template>
          <div style="font-size: 32px; text-align: center">5</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>待理赔</template>
          <div style="font-size: 32px; text-align: center">3</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>最近操作</template>
      <el-table :data="recentOps" stripe>
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="action" label="操作" />
        <el-table-column prop="result" label="结果" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const recentOps = ref([
  { time: '2026-07-03 10:30', operator: 'admin', action: '创建产品：定期寿险A', result: '成功' },
  { time: '2026-07-03 09:15', operator: 'admin', action: '核保通过：投保单 P001', result: '通过' },
  { time: '2026-07-03 08:00', operator: 'SYSTEM', action: '续期账单生成：200份', result: '成功' },
])
</script>
