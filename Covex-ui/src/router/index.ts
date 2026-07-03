import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 显式 import 所有页面组件（禁止懒加载）
import Login from '@/views/Login.vue'
import MainLayout from '@/views/MainLayout.vue'
import Dashboard from '@/views/dashboard/Dashboard.vue'
import ProductList from '@/views/product/ProductList.vue'
import ProductDetail from '@/views/product/ProductDetail.vue'
import NotFound from '@/views/error/NotFound.vue'
import Forbidden from '@/views/error/Forbidden.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' },
  },
  {
    path: '/403',
    name: 'forbidden',
    component: Forbidden,
    meta: { requiresAuth: false, title: '无权限' },
  },
  {
    path: '/404',
    name: 'not-found',
    component: NotFound,
    meta: { requiresAuth: false, title: '页面不存在' },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: Dashboard,
        meta: { title: '工作台', icon: 'Monitor' },
      },
      {
        path: 'product',
        name: 'product-list',
        component: ProductList,
        meta: { title: '产品列表', icon: 'Goods', roles: ['admin', 'product_mgr'] },
      },
      {
        path: 'product/create',
        name: 'product-create',
        component: ProductDetail,
        meta: { title: '创建产品', icon: 'Plus', roles: ['admin', 'product_mgr'] },
      },
      {
        path: 'product/:id',
        name: 'product-detail',
        component: ProductDetail,
        meta: { title: '产品详情', icon: 'View' },
        props: true,
      },
    ],
  },
  // 兜底路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 导航守卫 - 每个分支都有明确的 next() 出口
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Covex` : 'Covex'

  // 1. 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) {
    return next()
  }

  // 2. 检查 token
  const userStore = useUserStore()
  if (!userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 3. 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userStore.hasAnyRole(to.meta.roles as string[])) {
      return next('/403')
    }
  }

  // 4. 兜底放行
  next()
})

export default router
