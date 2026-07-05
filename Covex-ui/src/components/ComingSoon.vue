<template>
  <div class="coming-soon">
    <el-empty description="页面开发中，敬请期待">
      <template #image>
        <el-icon :size="64" color="#909399"><SetUp /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { SetUp } from '@element-plus/icons-vue'
</script>

<style scoped>
.coming-soon {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}
</style>
