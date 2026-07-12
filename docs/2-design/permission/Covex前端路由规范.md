# Covex 前端路由规范

> 防止 AI 生成代码时路由访问失败的强制规范。
> 此文件应放入前端项目的 `docs/` 目录，作为 AI 开发时的参考。

---

## 1. 路由配置原则

### 禁止懒加载

```js
// ❌ 禁止：AI 经常写错路径
const routes = [
  { path: '/product', component: () => import('@/views/ProductList.vue') }
]

// ✅ 正确：显式 import，路径错误在编译时就能发现
import ProductList from '@/views/product/ProductList.vue'
const routes = [
  { path: '/product', component: ProductList }
]
```

### 路由 name 必须唯一且带域前缀

```js
// ❌ 禁止：name 重复
{ path: '/product', name: 'List', component: ProductList }
{ path: '/claim', name: 'List', component: ClaimList }

// ✅ 正确：域前缀
{ path: '/product', name: 'product-list', component: ProductList }
{ path: '/claim', name: 'claim-list', component: ClaimList }
```

### 命名规范

| 页面类型 | 路由 path | name | 文件路径 |
|---|---|---|---|
| 列表页 | `/domain` | `domain-list` | `views/domain/XxxList.vue` |
| 详情页 | `/domain/:id` | `domain-detail` | `views/domain/XxxDetail.vue` |
| 创建页 | `/domain/create` | `domain-create` | `views/domain/XxxForm.vue` |
| 编辑页 | `/domain/:id/edit` | `domain-edit` | `views/domain/XxxForm.vue` |

## 2. JWT Token 处理

### 登录响应结构

后端登录接口 `POST /api/user/login` 返回：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sZXMiOlsiQURNSU4iXSwidGVuYW50SWQiOjAsImlhdCI6MTc4MzExMjc0MiwiZXhwIjoxNzgzMTk5MTQyfQ.xxx"
  }
}
```

**注意**：响应只有 `token`，没有单独的 `username` 和 `roles` 字段。用户名和角色必须从 JWT payload 中解析。

### JWT Payload 结构

```json
{
  "sub": "admin",          // 用户名
  "userId": 1,             // 用户ID
  "roles": ["ADMIN"],      // 角色列表（大写）
  "tenantId": 0,           // 租户ID
  "iat": 1783112742,       // 签发时间
  "exp": 1783199142        // 过期时间
}
```

### JWT 解析函数

```ts
// src/utils/jwt.ts
export function decodeJwt(token: string): Record<string, any> {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return {}
  }
}
```

### User Store 中的 Token 处理

```ts
// src/stores/user.ts
function setLogin(data: { token: string }) {
  token.value = data.token

  // 从 JWT 解析 username 和 roles
  const payload = decodeJwt(data.token)
  username.value = payload.sub || ''
  // 角色转小写：ADMIN -> admin，匹配路由守卫的 roles 检查
  roles.value = (payload.roles || []).map((r: string) => r.toLowerCase())

  localStorage.setItem('covex_token', data.token)
  localStorage.setItem('covex_username', username.value)
  localStorage.setItem('covex_roles', JSON.stringify(roles.value))
}
```

**关键**：后端返回的 roles 是大写（如 `ADMIN`），路由守卫检查的是小写（如 `admin`），必须在存储前做 `.toLowerCase()` 转换，否则导航守卫永远拦截。

### 请求拦截器

```ts
// src/utils/request.ts
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('covex_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 响应拦截器

```ts
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res  // 注意：返回的是 res（包含 code/data/message），不是 res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // token 过期或无效 → 清除登录态 → 跳转登录页
      localStorage.removeItem('covex_token')
      localStorage.removeItem('covex_username')
      localStorage.removeItem('covex_roles')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

## 3. 导航守卫规范

### 必须有明确的 next() 出口

```js
// ❌ 禁止：漏了 next()
router.beforeEach((to, from) => {
  if (!token) router.push('/login')
})

// ✅ 正确：每个分支都有 next()
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !getToken()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
```

### 守卫逻辑顺序

```js
router.beforeEach((to, from, next) => {
  // 1. 白名单直接放行
  if (['/login', '/404', '/403'].includes(to.path)) {
    return next()
  }

  // 2. 无 token → 登录页
  if (!getToken()) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 3. 有 token 但无权限 → 403
  if (to.meta.roles && !hasRole(to.meta.roles)) {
    return next('/403')
  }

  // 4. 兜底放行
  next()
})
```

## 3. 错误处理

### 404 兜底路由

```js
const routes = [
  // ... 正常路由
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFound
  }
]
```

### 动态参数校验

```vue
<!-- views/claim/ClaimDetail.vue -->
<script setup>
import { useRoute, useRouter } from 'vue-router'
import { onMounted } from 'vue'

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const id = route.params.id
  const res = await getClaimDetail(id)
  if (!res.data) {
    router.replace('/404')
  }
})
</script>
```

## 4. Vite 开发配置

```js
// vite.config.ts
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 5. 生产 Nginx 配置

```nginx
server {
    listen 80;
    server_name covex.example.com;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;  # ← History 模式必须
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 6. 路由定义模板

```js
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 显式 import 所有页面组件
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import ProductList from '@/views/product/ProductList.vue'
import ProductDetail from '@/views/product/ProductDetail.vue'
import ProductForm from '@/views/product/ProductForm.vue'
// ... 其他组件

const routes = [
  { path: '/login', name: 'login', component: Login, meta: { requiresAuth: false } },
  { path: '/403', name: 'forbidden', component: () => import('@/views/error/403.vue'), meta: { requiresAuth: false } },
  { path: '/404', name: 'not-found', component: () => import('@/views/error/404.vue'), meta: { requiresAuth: false } },

  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: Dashboard, meta: { requiresAuth: true } },

  // 产品配置域
  { path: '/product', name: 'product-list', component: ProductList, meta: { requiresAuth: true, roles: ['admin', 'product_mgr'] } },
  { path: '/product/create', name: 'product-create', component: ProductForm, meta: { requiresAuth: true, roles: ['admin', 'product_mgr'] } },
  { path: '/product/:id', name: 'product-detail', component: ProductDetail, meta: { requiresAuth: true } },
  { path: '/product/:id/edit', name: 'product-edit', component: ProductForm, meta: { requiresAuth: true, roles: ['admin', 'product_mgr'] } },

  // ... 其他域路由

  // 兜底
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  if (!to.meta.requiresAuth) return next()
  const token = localStorage.getItem('token')
  if (!token) return next({ path: '/login', query: { redirect: to.fullPath } })
  if (to.meta.roles && !hasAnyRole(to.meta.roles)) return next('/403')
  next()
})

export default router
```

## 7. AI 开发时的检查清单

AI 每次生成路由相关代码后，必须检查：

```
□ 所有路由组件是否已显式 import（不用懒加载）
□ 路由 name 是否唯一且带域前缀
□ 导航守卫是否每个分支都有 next()
□ 是否有 404 兜底路由
□ 动态参数页面是否有 onMounted 校验
□ Vite proxy 是否配置了 /api 代理
□ 路由 meta.requiresAuth 是否正确设置
□ 路由 meta.roles 是否与后端权限编码一致
```
