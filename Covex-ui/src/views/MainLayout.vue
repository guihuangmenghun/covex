<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" style="transition: width 0.3s">
      <div class="logo">
        <span v-if="!isCollapsed">Covex</span>
        <span v-else>C</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        :collapse="isCollapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-sub-menu index="sub-product">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>产品配置</span>
          </template>
          <el-menu-item index="/product">产品列表</el-menu-item>
          <el-menu-item index="/product/create">创建产品</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee">
        <el-icon style="cursor: pointer; font-size: 20px" @click="isCollapsed = !isCollapsed">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
        <div style="display: flex; align-items: center; gap: 16px">
          <span>{{ userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Monitor, Goods, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background-color: #263445;
}
</style>
