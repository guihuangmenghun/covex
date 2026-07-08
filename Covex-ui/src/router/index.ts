import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 显式 import 所有页面组件（禁止懒加载）
import Login from '@/views/Login.vue'
import MainLayout from '@/views/MainLayout.vue'
import Dashboard from '@/views/dashboard/Dashboard.vue'
import ProductList from '@/views/product/ProductList.vue'
import ProductDetail from '@/views/product/ProductDetail.vue'
import ProductCreate from '@/views/product/ProductCreate.vue'
import NotFound from '@/views/error/NotFound.vue'
import Forbidden from '@/views/error/Forbidden.vue'
import ComingSoon from '@/components/ComingSoon.vue'
import DictManagement from '@/views/system/DictManagement.vue'
import UserManagement from '@/views/system/UserManagement.vue'
import RoleManagement from '@/views/system/RoleManagement.vue'
import PermissionManagement from '@/views/system/PermissionManagement.vue'
import DataScopeManagement from '@/views/system/DataScopeManagement.vue'
import CustomerList from '@/views/customer/CustomerList.vue'
import CustomerCreate from '@/views/customer/CustomerCreate.vue'
import CustomerDetail from '@/views/customer/CustomerDetail.vue'
import RateTableList from '@/views/rate-table/RateTableList.vue'
import RateTableCreate from '@/views/rate-table/RateTableCreate.vue'
import RateTableDetail from '@/views/rate-table/RateTableDetail.vue'
import RateQuery from '@/views/rate-table/RateQuery.vue'
import ProposalList from '@/views/proposal/ProposalList.vue'
import ProposalCreate from '@/views/proposal/ProposalCreate.vue'
import ProposalDetail from '@/views/proposal/ProposalDetail.vue'
import UnderwritingList from '@/views/underwriting/UnderwritingList.vue'
import UnderwritingDetail from '@/views/underwriting/UnderwritingDetail.vue'
import PaymentManagement from '@/views/payment/PaymentManagement.vue'
import PolicyList from '@/views/policy/PolicyList.vue'
import PolicyDetail from '@/views/policy/PolicyDetail.vue'
import ClaimList from '@/views/claim/ClaimList.vue'
import ClaimCreate from '@/views/claim/ClaimCreate.vue'
import ClaimDetail from '@/views/claim/ClaimDetail.vue'
import CommissionManagement from '@/views/commission/CommissionManagement.vue'
import ChannelList from '@/views/channel/ChannelList.vue'
import ChannelCreate from '@/views/channel/ChannelCreate.vue'
import ChannelDetail from '@/views/channel/ChannelDetail.vue'

// ====== 角色常量 ======
const ADMIN = 'admin'
const SUB_ADMIN = 'sub_admin'
const PRODUCT_MGR = 'product_mgr'
const ACTUARY = 'actuary'
const CHANNEL_MGR = 'channel_mgr'
const AGENT = 'agent'
const SERVICE_REP = 'service_rep'
const UNDERWRITER = 'underwriter'
const CONSERVATION = 'conservation'
const CLAIM_HANDLER = 'claim_handler'
const INVESTIGATOR = 'investigator'
const FINANCE = 'finance'
const COMPLIANCE = 'compliance'

const routes = [
  // ====== 公共页面 ======
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

  // ====== 主布局（需要认证） ======
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true, title: 'Covex' },
    children: [
      // 工作台 — 所有已认证用户可见
      {
        path: 'dashboard',
        name: 'dashboard',
        component: Dashboard,
        meta: { title: '工作台', icon: 'Monitor' },
      },

      // ====== 产品配置域 ======
      {
        path: 'product',
        name: 'product-list',
        component: ProductList,
        meta: { title: '产品列表', roles: [ADMIN, PRODUCT_MGR, ACTUARY] },
      },
      {
        path: 'product/create',
        name: 'product-create',
        component: ProductCreate,
        meta: { title: '创建产品', roles: [ADMIN, PRODUCT_MGR] },
      },
      {
        path: 'product/:id',
        name: 'product-detail',
        component: ProductDetail,
        meta: { title: '产品详情', roles: [ADMIN, PRODUCT_MGR, ACTUARY] },
        props: true,
      },

      // 费率表
      {
        path: 'rate-table',
        name: 'rate-table-list',
        component: RateTableList,
        meta: { title: '费率表管理', roles: [ADMIN, PRODUCT_MGR, ACTUARY] },
      },
      {
        path: 'rate-table/create',
        name: 'rate-table-create',
        component: RateTableCreate,
        meta: { title: '创建费率表', roles: [ADMIN, ACTUARY] },
      },
      {
        path: 'rate-table/query',
        name: 'rate-table-query',
        component: RateQuery,
        meta: { title: '费率查询', roles: [ADMIN, PRODUCT_MGR, ACTUARY, AGENT] },
      },
      {
        path: 'rate-table/:id',
        name: 'rate-table-detail',
        component: RateTableDetail,
        meta: { title: '费率表详情', roles: [ADMIN, PRODUCT_MGR, ACTUARY] },
        props: true,
      },

      // ====== 承保管理域 ======
      {
        path: 'proposal',
        name: 'proposal-list',
        component: ProposalList,
        meta: { title: '投保单列表', roles: [ADMIN, AGENT, SERVICE_REP, UNDERWRITER, PRODUCT_MGR] },
      },
      {
        path: 'proposal/create',
        name: 'proposal-create',
        component: ProposalCreate,
        meta: { title: '创建投保单', roles: [ADMIN, AGENT, SERVICE_REP] },
      },
      {
        path: 'proposal/:id',
        name: 'proposal-detail',
        component: ProposalDetail,
        meta: { title: '投保单详情', roles: [ADMIN, AGENT, SERVICE_REP, UNDERWRITER] },
        props: true,
      },
      {
        path: 'underwriting',
        name: 'underwriting-list',
        component: UnderwritingList,
        meta: { title: '核保工作台', roles: [ADMIN, UNDERWRITER] },
      },
      {
        path: 'underwriting/:proposalId',
        name: 'underwriting-detail',
        component: UnderwritingDetail,
        meta: { title: '核保详情', roles: [ADMIN, UNDERWRITER] },
        props: true,
      },
      {
        path: 'payment',
        name: 'payment-list',
        component: PaymentManagement,
        meta: { title: '支付管理', roles: [ADMIN, UNDERWRITER, FINANCE] },
      },

      // ====== 保单管理域 ======
      {
        path: 'policy',
        name: 'policy-list',
        component: PolicyList,
        meta: { title: '保单列表', roles: [ADMIN, CONSERVATION, UNDERWRITER, SERVICE_REP] },
      },
      {
        path: 'policy/:id',
        name: 'policy-detail',
        component: PolicyDetail,
        meta: { title: '保单详情', roles: [ADMIN, CONSERVATION, UNDERWRITER, SERVICE_REP] },
        props: true,
      },

      // ====== 理赔管理域 ======
      {
        path: 'claim',
        name: 'claim-list',
        component: ClaimList,
        meta: { title: '理赔工作台', roles: [ADMIN, CLAIM_HANDLER, INVESTIGATOR, CONSERVATION] },
      },
      {
        path: 'claim/create',
        name: 'claim-create',
        component: ClaimCreate,
        meta: { title: '理赔报案', roles: [ADMIN, CLAIM_HANDLER, SERVICE_REP] },
      },
      {
        path: 'claim/:id',
        name: 'claim-detail',
        component: ClaimDetail,
        meta: { title: '理赔详情', roles: [ADMIN, CLAIM_HANDLER, INVESTIGATOR, CONSERVATION] },
        props: true,
      },

      // ====== 佣金管理域 ======
      {
        path: 'commission',
        name: 'commission-list',
        component: CommissionManagement,
        meta: { title: '佣金管理', roles: [ADMIN, CHANNEL_MGR, FINANCE] },
      },

      // ====== 客户管理域 ======
      {
        path: 'customer',
        name: 'customer-list',
        component: CustomerList,
        meta: { title: '客户列表', roles: [ADMIN, AGENT, SERVICE_REP, CLAIM_HANDLER, UNDERWRITER, CONSERVATION] },
      },
      {
        path: 'customer/create',
        name: 'customer-create',
        component: CustomerCreate,
        meta: { title: '创建客户', roles: [ADMIN, AGENT, SERVICE_REP] },
      },
      {
        path: 'customer/:id',
        name: 'customer-detail',
        component: CustomerDetail,
        meta: { title: '客户详情', roles: [ADMIN, AGENT, SERVICE_REP, CLAIM_HANDLER, UNDERWRITER, CONSERVATION] },
        props: true,
      },

      // ====== 渠道管理域 ======
      {
        path: 'channel',
        name: 'channel-list',
        component: ChannelList,
        meta: { title: '渠道商列表', roles: [ADMIN, CHANNEL_MGR] },
      },
      {
        path: 'channel/create',
        name: 'channel-create',
        component: ChannelCreate,
        meta: { title: '创建渠道商', roles: [ADMIN, CHANNEL_MGR] },
      },
      {
        path: 'channel/:id',
        name: 'channel-detail',
        component: ChannelDetail,
        meta: { title: '渠道商详情', roles: [ADMIN, CHANNEL_MGR] },
        props: true,
      },

      // ====== 规则配置中心（开发中） ======
      {
        path: 'rule-center',
        name: 'rule-center',
        component: ComingSoon,
        meta: { title: '规则配置中心', roles: [ADMIN, ACTUARY] },
      },

      // ====== 财务管理（开发中） ======
      {
        path: 'finance/settlement',
        name: 'finance-settlement',
        component: ComingSoon,
        meta: { title: '佣金结算', roles: [ADMIN, FINANCE] },
      },
      {
        path: 'finance/report',
        name: 'finance-report',
        component: ComingSoon,
        meta: { title: '财务报表', roles: [ADMIN, FINANCE] },
      },

      // ====== 合规管理（开发中） ======
      {
        path: 'compliance/review',
        name: 'compliance-review',
        component: ComingSoon,
        meta: { title: '合规审核', roles: [ADMIN, COMPLIANCE] },
      },
      {
        path: 'compliance/regulatory',
        name: 'compliance-regulatory',
        component: ComingSoon,
        meta: { title: '监管报告', roles: [ADMIN, COMPLIANCE] },
      },

      // ====== 系统管理域 ======
      {
        path: 'system/user',
        name: 'system-user',
        component: UserManagement,
        meta: { title: '用户管理', roles: [ADMIN, SUB_ADMIN] },
      },
      {
        path: 'system/role',
        name: 'system-role',
        component: RoleManagement,
        meta: { title: '角色管理', roles: [ADMIN] },
      },
      {
        path: 'system/permission',
        name: 'system-permission',
        component: PermissionManagement,
        meta: { title: '权限管理', roles: [ADMIN] },
      },
      {
        path: 'system/data-scope',
        name: 'system-data-scope',
        component: DataScopeManagement,
        meta: { title: '数据范围', roles: [ADMIN] },
      },
      {
        path: 'system/dict',
        name: 'system-dict',
        component: DictManagement,
        meta: { title: '数据字典', roles: [ADMIN] },
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
  if (!to.meta.requiresAuth && to.path !== '/') {
    return next()
  }

  // 2. 检查 token（主布局的子路由默认需要认证）
  const userStore = useUserStore()
  const needsAuth = to.meta.requiresAuth !== false && to.path !== '/login'
  if (needsAuth && !userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 3. 检查角色权限
  if (to.meta.roles && (to.meta.roles as string[]).length > 0) {
    if (!userStore.hasAnyRole(to.meta.roles as string[])) {
      return next('/403')
    }
  }

  // 4. 兜底放行
  next()
})

export default router
