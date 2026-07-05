<template>
  <el-breadcrumb separator="/" style="margin-bottom: 16px">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
      <span v-if="index === breadcrumbs.length - 1" style="color: #303133; font-weight: 500">
        {{ item.title }}
      </span>
      <router-link v-else :to="item.path" style="color: #606266">
        {{ item.title }}
      </router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title)
  return matched.map((item) => ({
    title: item.meta.title as string,
    path: item.path || '/',
  }))
})
</script>
