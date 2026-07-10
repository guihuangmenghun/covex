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
          <template v-for="item in visibleMenu" :key="item.index">
            <!-- 子菜单组 -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.index">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.index"
                :index="child.index"
              >{{ child.title }}</el-menu-item>
            </el-sub-menu>
            <!-- 单独菜单项 -->
            <el-menu-item v-else :index="item.index">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
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
          <el-tag size="small" type="info" v-if="userStore.roles.length > 0">{{ userStore.roles.join(', ') }}</el-tag>
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Breadcrumb from '@/components/Breadcrumb.vue'
import {
  Monitor, Goods, Document, Tickets, Warning, Money,
  User, Shop, Setting, Fold, Expand, SetUp, DataLine, Check,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

// ====== 菜单定义（数据驱动） ======
interface MenuItem {
  index: string
  title: string
  icon?: any
  roles?: string[]
  children?: MenuItem[]
}

const allMenu: MenuItem[] = [
  { index: '/dashboard', title: '工作台', icon: Monitor },
  {
    index: 'sub-product', title: '产品配置', icon: Goods,
    roles: ['admin', 'product_mgr', 'actuary'],
    children: [
      { index: '/product', title: '产品列表', roles: ['admin', 'product_mgr', 'actuary'] },
      { index: '/rate-table', title: '费率表管理', roles: ['admin', 'product_mgr', 'actuary'] },
      { index: '/rate-table/query', title: '费率查询', roles: ['admin', 'product_mgr', 'actuary', 'agent'] },
    ],
  },
  {
    index: 'sub-underwriting', title: '承保管理', icon: Document,
    roles: ['admin', 'agent', 'service_rep', 'underwriter', 'product_mgr', 'finance'],
    children: [
      { index: '/proposal', title: '投保单', roles: ['admin', 'agent', 'service_rep', 'underwriter', 'product_mgr'] },
      { index: '/underwriting', title: '核保工作台', roles: ['admin', 'underwriter'] },
      { index: '/payment', title: '支付管理', roles: ['admin', 'underwriter', 'finance'] },
    ],
  },
  { index: '/policy', title: '保单管理', icon: Tickets, roles: ['admin', 'conservation', 'underwriter', 'service_rep'] },
  {
    index: 'sub-claim', title: '理赔管理', icon: Warning,
    roles: ['admin', 'claim_handler', 'investigator', 'conservation', 'service_rep'],
    children: [
      { index: '/claim', title: '理赔工作台', roles: ['admin', 'claim_handler', 'investigator', 'conservation'] },
      { index: '/claim/create', title: '理赔报案', roles: ['admin', 'claim_handler', 'service_rep'] },
    ],
  },
  { index: '/commission', title: '佣金管理', icon: Money, roles: ['admin', 'channel_mgr', 'finance'] },
  { index: '/customer', title: '客户管理', icon: User, roles: ['admin', 'agent', 'service_rep', 'claim_handler', 'underwriter', 'conservation'] },
  { index: '/channel', title: '渠道管理', icon: Shop, roles: ['admin', 'channel_mgr'] },
  { index: '/rule-center', title: '规则配置中心', icon: SetUp, roles: ['admin', 'actuary'] },
  {
    index: 'sub-finance', title: '财务管理', icon: DataLine,
    roles: ['admin', 'finance'],
    children: [
      { index: '/finance/settlement', title: '佣金结算', roles: ['admin', 'finance'] },
      { index: '/finance/report', title: '财务报表', roles: ['admin', 'finance'] },
    ],
  },
  {
    index: 'sub-compliance', title: '合规管理', icon: Check,
    roles: ['admin', 'compliance'],
    children: [
      { index: '/compliance/review', title: '合规审核', roles: ['admin', 'compliance'] },
      { index: '/compliance/regulatory', title: '监管报告', roles: ['admin', 'compliance'] },
    ],
  },
  {
    index: 'sub-system', title: '系统管理', icon: Setting,
    roles: ['admin', 'sub_admin'],
    children: [
      { index: '/system/user', title: '用户管理', roles: ['admin', 'sub_admin'] },
      { index: '/system/role', title: '角色管理', roles: ['admin'] },
      { index: '/system/permission', title: '角色权限', roles: ['admin'] },
      { index: '/system/data-scope', title: '数据范围', roles: ['admin'] },
      { index: '/system/dict', title: '数据字典', roles: ['admin'] },
    ],
  },
]

// ====== 角色过滤 ======
const visibleMenu = computed(() => {
  return allMenu
    .map(item => {
      if (item.children) {
        const visibleChildren = item.children.filter(
          child => !child.roles || userStore.hasAnyRole(child.roles)
        )
        if (visibleChildren.length === 0) return null
        return { ...item, children: visibleChildren }
      }
      return item
    })
    .filter(item => {
      if (!item) return false
      if (!item.roles) return true
      return userStore.hasAnyRole(item.roles)
    }) as MenuItem[]
})
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
