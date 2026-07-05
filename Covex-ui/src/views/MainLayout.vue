<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" style="transition: width 0.3s; overflow-x: hidden">
      <div class="logo">
        <span v-if="!isCollapsed">Covex</span>
        <span v-else>C</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          router
          :collapse="isCollapsed"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          :collapse-transition="false"
        >
          <!-- 工作台 -->
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>

          <!-- 产品配置 -->
          <el-sub-menu index="sub-product">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>产品配置</span>
            </template>
            <el-menu-item index="/product">产品列表</el-menu-item>
            <el-menu-item index="/rate-table">费率表管理</el-menu-item>
            <el-menu-item index="/rate-table/query">费率查询</el-menu-item>
          </el-sub-menu>

          <!-- 承保管理 -->
          <el-sub-menu index="sub-underwriting">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>承保管理</span>
            </template>
            <el-menu-item index="/proposal">投保单</el-menu-item>
            <el-menu-item index="/underwriting">核保工作台</el-menu-item>
            <el-menu-item index="/payment">支付管理</el-menu-item>
          </el-sub-menu>

          <!-- 保单管理 -->
          <el-menu-item index="/policy">
            <el-icon><Tickets /></el-icon>
            <template #title>保单管理</template>
          </el-menu-item>

          <!-- 理赔管理 -->
          <el-sub-menu index="sub-claim">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>理赔管理</span>
            </template>
            <el-menu-item index="/claim">理赔工作台</el-menu-item>
            <el-menu-item index="/claim/create">理赔报案</el-menu-item>
          </el-sub-menu>

          <!-- 佣金管理 -->
          <el-menu-item index="/commission">
            <el-icon><Money /></el-icon>
            <template #title>佣金管理</template>
          </el-menu-item>

          <!-- 客户管理 -->
          <el-menu-item index="/customer">
            <el-icon><User /></el-icon>
            <template #title>客户管理</template>
          </el-menu-item>

          <!-- 渠道管理 -->
          <el-menu-item index="/channel">
            <el-icon><Shop /></el-icon>
            <template #title>渠道管理</template>
          </el-menu-item>

          <!-- 系统管理 -->
          <el-sub-menu index="sub-system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/permission">权限管理</el-menu-item>
            <el-menu-item index="/system/data-scope">数据范围</el-menu-item>
            <el-menu-item index="/system/dict">数据字典</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header
        style="
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-bottom: 1px solid #eee;
          padding: 0 20px;
          height: 56px;
        "
      >
        <div style="display: flex; align-items: center; gap: 16px">
          <el-icon style="cursor: pointer; font-size: 20px" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <Breadcrumb />
        </div>
        <div style="display: flex; align-items: center; gap: 16px">
          <span style="color: #606266">{{ userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main style="background-color: #f5f7fa; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Breadcrumb from '@/components/Breadcrumb.vue'
import {
  Monitor,
  Goods,
  Document,
  Tickets,
  Warning,
  Money,
  User,
  Shop,
  Setting,
  Fold,
  Expand,
} from '@element-plus/icons-vue'

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

:deep(.el-menu) {
  border-right: none;
}
</style>
