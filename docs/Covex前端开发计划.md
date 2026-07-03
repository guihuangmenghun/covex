# Covex 前端开发计划

> 15 个阶段（F1 ~ F15），按依赖顺序排列。完成后将 `[ ]` 改为 `[x]`。
> 前端技术栈：Vue 3.5 + Element Plus 2.9.7 + Vite 6 + TypeScript 5 + Pinia 3 + Vue Router 4 + ESLint 9
> 后端 API：133 个端点（26 个 Controller），基础路径 `/api`
> 适配角色：仅管理人员（保险业务提供方），不含 C 端用户页面

---

## 后端 API 端点总览

### 一、产品配置域（34 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 1 | ProductController | POST | /api/product | 创建产品 |
| 2 | ProductController | GET | /api/product/{id} | 查询产品详情（含责任/缴费/规则） |
| 3 | ProductController | GET | /api/product | 分页查询产品列表 |
| 4 | ProductController | PUT | /api/product/{id} | 更新产品（仅草稿/驳回） |
| 5 | ProductController | POST | /api/product/{id}/clone | 克隆产品 |
| 6 | ProductController | PUT | /api/product/{id}/publish | 发布产品 |
| 7 | ProductController | PUT | /api/product/{id}/freeze | 冻结产品 |
| 8 | ProductController | GET | /api/product/{id}/changelog | 查询变更历史 |
| 9 | ProductCoverageController | POST | /api/product/{productId}/coverage | 创建保障 |
| 10 | ProductCoverageController | GET | /api/product/{productId}/coverage | 查询产品下所有保障 |
| 11 | ProductCoverageController | GET | /api/product/{productId}/coverage/{coverageId} | 查询保障详情 |
| 12 | ProductCoverageController | PUT | /api/product/{productId}/coverage/{coverageId} | 更新保障 |
| 13 | ProductCoverageController | DELETE | /api/product/{productId}/coverage/{coverageId} | 删除保障 |
| 14 | ProductCoverageController | POST | /api/product/{productId}/coverage/{coverageId}/link-premium | 关联缴费计划 |
| 15 | ProductCoverageController | DELETE | /api/product/{productId}/coverage/{coverageId}/unlink-premium/{premiumId} | 取消关联缴费计划 |
| 16 | ProductCoverageController | GET | /api/product/{productId}/coverage/{coverageId}/premiums | 查询保障关联的缴费计划 |
| 17 | ProductDocumentController | POST | /api/product/{productId}/document | 创建文档 |
| 18 | ProductDocumentController | GET | /api/product/{productId}/document | 查询产品下所有文档 |
| 19 | ProductDocumentController | GET | /api/product/{productId}/document/{documentId} | 查询文档详情 |
| 20 | ProductDocumentController | PUT | /api/product/{productId}/document/{documentId} | 更新文档 |
| 21 | ProductDocumentController | DELETE | /api/product/{productId}/document/{documentId} | 删除文档 |
| 22 | ProductPremiumController | POST | /api/product/{productId}/premium | 创建缴费计划 |
| 23 | ProductPremiumController | GET | /api/product/{productId}/premium | 查询产品下所有缴费计划 |
| 24 | ProductPremiumController | GET | /api/product/{productId}/premium/{premiumId} | 查询缴费计划详情 |
| 25 | ProductPremiumController | PUT | /api/product/{productId}/premium/{premiumId} | 更新缴费计划 |
| 26 | ProductPremiumController | DELETE | /api/product/{productId}/premium/{premiumId} | 删除缴费计划 |
| 27 | ProductRiderRelController | POST | /api/product/{productId}/rider | 创建主附险关联 |
| 28 | ProductRiderRelController | GET | /api/product/{productId}/rider | 查询主险下的附加险关联 |
| 29 | ProductRiderRelController | DELETE | /api/product/{productId}/rider/{relId} | 删除主附险关联 |
| 30 | ProductRuleController | POST | /api/product/{productId}/rule | 创建规则引用 |
| 31 | ProductRuleController | GET | /api/product/{productId}/rule | 查询产品下所有规则 |
| 32 | ProductRuleController | GET | /api/product/{productId}/rule/{ruleId} | 查询规则详情 |
| 33 | ProductRuleController | PUT | /api/product/{productId}/rule/{ruleId} | 更新规则 |
| 34 | ProductRuleController | DELETE | /api/product/{productId}/rule/{ruleId} | 删除规则 |

### 二、费率表域（8 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 35 | RateTableController | POST | /api/rate-table | 创建费率表 |
| 36 | RateTableController | GET | /api/rate-table/{id} | 查询费率表详情 |
| 37 | RateTableController | GET | /api/rate-table | 按产品查询费率表列表 |
| 38 | RateTableController | GET | /api/rate-table/{id}/rows | 查询费率表行数据 |
| 39 | RateTableController | POST | /api/rate-table/{id}/import | 批量导入费率表行数据 |
| 40 | RateTableController | GET | /api/rate-table/query | 查询费率（Redis -> DB） |
| 41 | RateTableController | POST | /api/rate-table/load | 加载费率表到 Redis |
| 42 | RateTableController | POST | /api/rate-table/evict | 清除费率表 Redis 缓存 |

### 三、投保单域（4 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 43 | ProposalController | POST | /api/proposal | 创建投保单 |
| 44 | ProposalController | GET | /api/proposal/{id} | 查询投保单详情 |
| 45 | ProposalController | GET | /api/proposal | 分页查询投保单列表 |
| 46 | ProposalController | PUT | /api/proposal/{id}/submit | 提交投保单（触发校验+核保链） |

### 四、核保域（3 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 47 | UnderwritingController | POST | /api/underwriting/auto/{proposalId} | 自动核保（手动触发） |
| 48 | UnderwritingController | POST | /api/underwriting/manual/{proposalId} | 人工核保 |
| 49 | UnderwritingController | GET | /api/underwriting/records/{proposalId} | 查询核保记录 |

### 五、支付域（5 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 50 | PaymentController | POST | /api/payment/calculate/{proposalId} | 计算保费 |
| 51 | PaymentController | POST | /api/payment/create | 创建支付记录 |
| 52 | PaymentController | POST | /api/payment/callback | 支付回调 |
| 53 | PaymentController | GET | /api/payment/query/{proposalId} | 查询投保单支付记录 |
| 54 | PaymentController | POST | /api/payment/timeout-scan | 扫描超时投保单并撤销 |

### 六、保单域（3 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 55 | PolicyController | POST | /api/policy/issue/{proposalId} | 出单（生成保单） |
| 56 | PolicyController | GET | /api/policy/{id} | 查询保单详情（含险种明细+缴费计划） |
| 57 | PolicyController | GET | /api/policy | 分页查询保单列表 |

### 七、理赔域（12 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 58 | ClaimController | POST | /api/claim | 报案 |
| 59 | ClaimController | GET | /api/claim/{id} | 查询理赔详情 |
| 60 | ClaimController | GET | /api/claim | 分页查询理赔列表 |
| 61 | ClaimController | POST | /api/claim/{id}/assign | 分配理赔员 |
| 62 | ClaimController | POST | /api/claim/{id}/review | 提交审核 |
| 63 | ClaimController | POST | /api/claim/{id}/calculate | 赔付计算 |
| 64 | ClaimController | POST | /api/claim/{id}/investigate | 启动调查 |
| 65 | ClaimController | POST | /api/claim/{id}/investigation-result | 提交调查结论 |
| 66 | ClaimDocumentController | POST | /api/claim/{claimId}/document | 上传理赔材料 |
| 67 | ClaimDocumentController | GET | /api/claim/{claimId}/document | 查询理赔材料列表 |
| 68 | ClaimPaymentController | POST | /api/claim/{claimId}/payment/process | 触发赔付 |
| 69 | ClaimPaymentController | POST | /api/claim/{claimId}/payment/callback | 支付回调 |
| 70 | ClaimPaymentController | POST | /api/claim/{claimId}/payment/close | 结案 |
| 71 | ClaimPaymentController | POST | /api/claim/{claimId}/payment/dispute | 拒赔申诉 |

### 八、佣金域（5 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 72 | CommissionController | GET | /api/commission | 查询佣金列表 |
| 73 | CommissionController | POST | /api/commission/calculate | 计算佣金 |
| 74 | CommissionController | POST | /api/commission/settle | 触发月度结算 |
| 75 | CommissionController | GET | /api/commission/summary | 月度汇总统计 |
| 76 | CommissionController | PUT | /api/commission/confirm | 确认支付 |

### 九、客户域（17 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 77 | CustomerController | POST | /api/customer | 创建客户 |
| 78 | CustomerController | GET | /api/customer/{id} | 查询客户详情 |
| 79 | CustomerController | GET | /api/customer | 分页查询客户 |
| 80 | CustomerController | PUT | /api/customer/{id} | 更新客户 |
| 81 | CustomerController | POST | /api/customer/{id}/ensure-applicant | 确保投保人扩展存在 |
| 82 | CustomerController | POST | /api/customer/{id}/ensure-insured | 确保被保人扩展存在 |
| 83 | CustomerController | PUT | /api/customer/{id}/health | 更新健康档案 |
| 84 | CustomerAddressController | POST | /api/customer/{customerId}/address | 创建地址 |
| 85 | CustomerAddressController | GET | /api/customer/{customerId}/address | 查询地址列表 |
| 86 | CustomerAddressController | PUT | /api/customer/{customerId}/address/{id} | 更新地址 |
| 87 | CustomerAddressController | DELETE | /api/customer/{customerId}/address/{id} | 删除地址 |
| 88 | CustomerAddressController | PUT | /api/customer/{customerId}/address/{id}/default | 设置默认地址 |
| 89 | CustomerBankAccountController | POST | /api/customer/{customerId}/bank-account | 创建银行账户 |
| 90 | CustomerBankAccountController | GET | /api/customer/{customerId}/bank-account | 查询银行账户列表 |
| 91 | CustomerBankAccountController | PUT | /api/customer/{customerId}/bank-account/{id} | 更新银行账户 |
| 92 | CustomerBankAccountController | DELETE | /api/customer/{customerId}/bank-account/{id} | 删除银行账户 |
| 93 | CustomerBankAccountController | PUT | /api/customer/{customerId}/bank-account/{id}/default | 设置默认银行账户 |

### 十、渠道域（12 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 94 | ChannelController | POST | /api/channel | 创建渠道商 |
| 95 | ChannelController | GET | /api/channel/{id} | 查询渠道商详情 |
| 96 | ChannelController | GET | /api/channel | 分页查询渠道商 |
| 97 | ChannelController | PUT | /api/channel/{id} | 更新渠道商 |
| 98 | ChannelController | PUT | /api/channel/{id}/status | 更新渠道商状态 |
| 99 | ChannelController | POST | /api/channel/{channelId}/authorize | 授权产品 |
| 100 | ChannelController | DELETE | /api/channel/{channelId}/authorize/{productId} | 撤销产品授权 |
| 101 | ChannelController | GET | /api/channel/{channelId}/products | 查询授权产品列表 |
| 102 | ChannelUserController | POST | /api/channel/{channelId}/user | 创建渠道商账号 |
| 103 | ChannelUserController | GET | /api/channel/{channelId}/user | 查询渠道商下的账号列表 |
| 104 | ChannelUserController | PUT | /api/channel/{channelId}/user/{id} | 更新渠道商账号 |
| 105 | ChannelUserController | PUT | /api/channel/{channelId}/user/{id}/status | 切换账号状态 |

### 十一、系统管理域（28 个端点）

| # | Controller | 方法 | 路径 | 功能说明 |
|---|---|---|---|---|
| 106 | UserController | POST | /api/user | 创建用户 |
| 107 | UserController | POST | /api/user/login | 用户登录（返回 JWT Token） |
| 108 | UserController | GET | /api/user/{id} | 根据 ID 查询用户 |
| 109 | UserController | GET | /api/user | 分页查询用户列表 |
| 110 | UserController | PUT | /api/user/{id} | 更新用户 |
| 111 | UserController | PUT | /api/user/{id}/status | 切换用户启用/停用状态 |
| 112 | UserController | POST | /api/user/{id}/roles | 分配角色 |
| 113 | UserController | GET | /api/user/{id}/roles | 查询用户角色 |
| 114 | UserController | GET | /api/user/{id}/permissions | 查询用户权限 |
| 115 | RoleController | POST | /api/role | 创建角色 |
| 116 | RoleController | GET | /api/role | 查询角色列表 |
| 117 | RoleController | PUT | /api/role/{id} | 更新角色 |
| 118 | RoleController | DELETE | /api/role/{id} | 删除角色 |
| 119 | RoleController | POST | /api/role/{id}/permissions | 分配权限 |
| 120 | RoleController | GET | /api/role/{id}/permissions | 查询角色权限 |
| 121 | PermissionController | POST | /api/permission | 创建权限 |
| 122 | PermissionController | GET | /api/permission | 查询权限列表 |
| 123 | PermissionController | GET | /api/permission/modules | 按模块分组查询权限 |
| 124 | DataScopeController | POST | /api/data-scope/{roleId} | 设置角色数据范围 |
| 125 | DataScopeController | GET | /api/data-scope/{roleId} | 查询角色数据范围 |
| 126 | DictController | GET | /api/dict/{dictType} | 按类型查询字典 |
| 127 | DictController | GET | /api/dict/{dictType}/children | 按类型+父编码查询（层级字典） |
| 128 | DictController | GET | /api/dict | 查询所有字典（按类型分组） |
| 129 | DictController | POST | /api/dict | 新增字典项 |
| 130 | DictController | PUT | /api/dict/{id} | 更新字典项 |
| 131 | DictController | DELETE | /api/dict/{id} | 删除字典项 |
| 132 | DictController | POST | /api/dict/cache/evict | 清空字典缓存 |
| 133 | HealthController | GET | /api/health | 应用健康检查 |

---

## 页面清单（API -> 页面映射）

### 页面总览

| # | 页面名称 | 路由路径 | 对应 API | 功能说明 |
|---|---|---|---|---|
| 1 | 登录页 | /login | POST /api/user/login | 管理员登录，JWT 认证 |
| 2 | 工作台首页 | /dashboard | - | 数据概览仪表盘（聚合数据） |
| 3 | 产品列表 | /product | GET /api/product | 产品分页查询 + 筛选 |
| 4 | 产品创建 | /product/create | POST /api/product | 产品新建表单 |
| 5 | 产品详情 | /product/:id | GET /api/product/:id | 产品详情 + Tab 切换（保障/缴费/规则/文档/附险/变更历史） |
| 6 | 费率表列表 | /rate-table | GET /api/rate-table | 费率表列表 + 按产品筛选 |
| 7 | 费率表详情 | /rate-table/:id | GET /api/rate-table/:id + GET /:id/rows | 费率表详情 + 行数据管理 + 导入 |
| 8 | 费率查询工具 | /rate-table/query | GET /api/rate-table/query | 费率试算（Redis -> DB 二级查询） |
| 9 | 投保单列表 | /proposal | GET /api/proposal | 投保单分页查询 + 状态筛选 |
| 10 | 投保单创建 | /proposal/create | POST /api/proposal | 投保单录入表单 |
| 11 | 投保单详情 | /proposal/:id | GET /api/proposal/:id | 投保单详情 + 提交操作 |
| 12 | 核保工作台 | /underwriting | GET /api/proposal?status=待核保 | 待核保任务列表 + 核保操作面板 |
| 13 | 核保详情 | /underwriting/:proposalId | GET /api/underwriting/records/:proposalId | 核保记录查看 + 人工核保表单 |
| 14 | 支付管理 | /payment | GET /api/payment/query/:proposalId | 支付记录查询 + 保费计算 |
| 15 | 保单列表 | /policy | GET /api/policy | 保单分页查询 |
| 16 | 保单详情 | /policy/:id | GET /api/policy/:id | 保单详情（含险种明细+缴费计划） |
| 17 | 理赔工作台 | /claim | GET /api/claim | 理赔列表 + 状态筛选 + 分配操作 |
| 18 | 理赔报案 | /claim/create | POST /api/claim | 理赔报案录入 |
| 19 | 理赔详情 | /claim/:id | GET /api/claim/:id | 理赔详情 + 审核/调查/赔付/结案全流程操作 |
| 20 | 佣金管理 | /commission | GET /api/commission | 佣金列表 + 月度结算 + 统计汇总 |
| 21 | 客户列表 | /customer | GET /api/customer | 客户分页查询 |
| 22 | 客户创建 | /customer/create | POST /api/customer | 客户录入表单 |
| 23 | 客户详情 | /customer/:id | GET /api/customer/:id | 客户详情 + Tab（地址/银行账户/健康档案） |
| 24 | 渠道商列表 | /channel | GET /api/channel | 渠道商分页查询 |
| 25 | 渠道商创建 | /channel/create | POST /api/channel | 渠道商新建 |
| 26 | 渠道商详情 | /channel/:id | GET /api/channel/:id | 渠道商详情 + 账号管理 + 产品授权 |
| 27 | 用户管理 | /system/user | GET /api/user | 用户列表 + CRUD + 角色分配 |
| 28 | 角色管理 | /system/role | GET /api/role | 角色列表 + CRUD + 权限分配 |
| 29 | 权限管理 | /system/permission | GET /api/permission | 权限列表 + 创建 + 模块分组查看 |
| 30 | 数据范围管理 | /system/data-scope | GET /api/data-scope/:roleId | 角色数据范围配置 |
| 31 | 数据字典管理 | /system/dict | GET /api/dict | 字典项管理（按类型分组 + 层级） |

### 菜单结构

```
Covex 管理平台
├── 工作台 (/dashboard)
├── 产品配置
│   ├── 产品管理 (/product)
│   ├── 费率表管理 (/rate-table)
│   └── 费率查询 (/rate-table/query)
├── 承保管理
│   ├── 投保单 (/proposal)
│   ├── 核保工作台 (/underwriting)
│   └── 支付管理 (/payment)
├── 保单管理
│   └── 保单列表 (/policy)
├── 理赔管理
│   ├── 理赔工作台 (/claim)
│   └── 理赔报案 (/claim/create)
├── 佣金管理 (/commission)
├── 客户管理
│   └── 客户列表 (/customer)
├── 渠道管理
│   └── 渠道商 (/channel)
└── 系统管理
    ├── 用户管理 (/system/user)
    ├── 角色管理 (/system/role)
    ├── 权限管理 (/system/permission)
    ├── 数据范围 (/system/data-scope)
    └── 数据字典 (/system/dict)
```

---

## 开发阶段

### F1 - 项目初始化 + 工程脚手架

> 依赖：无 | 验收：`npm run dev` 启动成功，ESLint 无报错，TypeScript 编译通过

- [ ] 使用 Vite 6 创建 Vue 3 + TypeScript 项目
- [ ] 安装核心依赖：element-plus 2.9.7、vue-router 4、pinia 3、axios
- [ ] 安装开发依赖：eslint 9、@vue/eslint-config-typescript、prettier、sass
- [ ] 配置 ESLint + Prettier（统一代码风格）
- [ ] 配置 Vite 代理（开发环境 /api -> http://localhost:8080）
- [ ] 配置 TypeScript 严格模式
- [ ] 配置路径别名（@/ -> src/）
- [ ] 建立目录结构：api/、components/、views/、stores/、router/、utils/、types/
- [ ] 封装 axios 实例（baseURL、拦截器、JWT Token 自动注入、统一错误处理）
- [ ] 封装 Result\<T\> 类型定义（与后端 Result 对齐）
- [ ] 配置环境变量（.env.development / .env.production）

---

### F2 - 登录页 + 布局框架

> 依赖：F1 | 验收：管理员登录成功 -> 跳转到工作台 -> 侧边栏菜单完整展示 -> 退出登录清除 Token

- [ ] 实现登录页（/login）：用户名+密码表单，调用 POST /api/user/login
- [ ] 登录成功后存储 JWT Token（localStorage / Pinia store）
- [ ] 实现全局布局组件：左侧边栏 + 顶部导航 + 内容区
- [ ] 实现侧边栏菜单（按菜单结构渲染，支持折叠/展开）
- [ ] 实现顶部导航（用户信息 + 退出登录按钮）
- [ ] 配置 Vue Router 路由守卫（未登录跳转 /login）
- [ ] 实现 Pinia 用户状态管理（token / userInfo / permissions）
- [ ] 实现面包屑导航组件
- [ ] 实现 403 / 404 异常页面

---

### F3 - 系统管理 - 数据字典

> 依赖：F2 | 验收：字典列表按类型分组展示 + 新增/编辑/删除字典项 + 清空缓存操作

**涉及 API：**
- GET /api/dict（查询所有字典，按类型分组）
- GET /api/dict/{dictType}（按类型查询）
- GET /api/dict/{dictType}/children（层级字典）
- POST /api/dict（新增）
- PUT /api/dict/{id}（更新）
- DELETE /api/dict/{id}（删除）
- POST /api/dict/cache/evict（清空缓存）

- [ ] 创建字典管理 API 层（src/api/dict.ts）
- [ ] 实现字典列表页：左侧字典类型树 + 右侧字典项表格
- [ ] 实现字典项新增/编辑弹窗（dict_type / dict_code / dict_label / sort_order / parent_code）
- [ ] 实现字典项删除（二次确认）
- [ ] 实现层级字典展示（树形结构）
- [ ] 实现清空缓存按钮
- [ ] 配置路由：/system/dict

---

### F4 - 系统管理 - 用户/角色/权限/数据范围

> 依赖：F3 | 验收：创建用户 -> 分配角色 -> 角色分配权限 -> 数据范围配置完成

**涉及 API：**
- 用户管理：POST/GET/PUT /api/user、PUT /api/user/{id}/status、POST /api/user/{id}/roles、GET /api/user/{id}/roles、GET /api/user/{id}/permissions
- 角色管理：POST/GET/PUT/DELETE /api/role、POST /api/role/{id}/permissions、GET /api/role/{id}/permissions
- 权限管理：POST/GET /api/permission、GET /api/permission/modules
- 数据范围：POST/GET /api/data-scope/{roleId}

- [ ] 创建用户/角色/权限 API 层
- [ ] 实现用户列表页（分页 + 关键字搜索 + 状态标签）
- [ ] 实现用户创建/编辑表单（用户名/姓名/手机号/邮箱/用户类型）
- [ ] 实现用户状态切换（启用/停用）
- [ ] 实现用户角色分配弹窗（穿梭框/多选）
- [ ] 实现角色列表页（卡片/表格模式）
- [ ] 实现角色创建/编辑表单
- [ ] 实现角色权限分配页（树形权限选择，按模块分组）
- [ ] 实现权限列表页（按模块分组展示）
- [ ] 实现数据范围配置页（选择角色 -> 配置数据范围规则）
- [ ] 配置路由：/system/user、/system/role、/system/permission、/system/data-scope

---

### F5 - 客户管理

> 依赖：F2 | 验收：创建客户 -> 维护地址和银行账户 -> 更新健康档案 -> 客户详情完整展示

**涉及 API：**
- 客户：POST/GET/PUT /api/customer、POST /api/customer/{id}/ensure-applicant、POST /api/customer/{id}/ensure-insured、PUT /api/customer/{id}/health
- 地址：POST/GET/PUT/DELETE /api/customer/{customerId}/address、PUT .../default
- 银行账户：POST/GET/PUT/DELETE /api/customer/{customerId}/bank-account、PUT .../default

- [ ] 创建客户 API 层（src/api/customer.ts）
- [ ] 实现客户列表页（分页 + 关键字搜索：姓名/证件号/手机号）
- [ ] 实现客户创建表单（姓名/证件类型/证件号/性别/出生日期/手机号/邮箱）
- [ ] 实现客户详情页，包含 Tab 页签：
  - 基本信息 Tab：客户详情 + 编辑功能
  - 联系地址 Tab：地址列表 + CRUD + 设置默认地址
  - 银行账户 Tab：账户列表 + CRUD + 设置默认 + 代扣协议保护提示
  - 健康档案 Tab：既往病史/家族病史/当前用药 JSON 表单
- [ ] 实现投保人/被保人角色扩展操作按钮
- [ ] 证件号、手机号脱敏展示
- [ ] 配置路由：/customer、/customer/create、/customer/:id

---

### F6 - 产品管理

> 依赖：F2 | 验收：创建产品 -> 配置保障/缴费/规则/文档/附险 -> 发布 -> 冻结 -> 克隆全流程

**涉及 API：**
- 产品：POST/GET/PUT /api/product、POST /api/product/{id}/clone、PUT /api/product/{id}/publish、PUT /api/product/{id}/freeze、GET /api/product/{id}/changelog
- 保障：POST/GET/PUT/DELETE /api/product/{productId}/coverage、POST .../link-premium、DELETE .../unlink-premium、GET .../premiums
- 缴费：POST/GET/PUT/DELETE /api/product/{productId}/premium
- 规则：POST/GET/PUT/DELETE /api/product/{productId}/rule
- 文档：POST/GET/PUT/DELETE /api/product/{productId}/document
- 附险：POST/GET/DELETE /api/product/{productId}/rider

- [ ] 创建产品 API 层（src/api/product.ts）
- [ ] 实现产品列表页（分页 + 产品类型筛选 + 版本状态筛选 + 关键字搜索）
- [ ] 实现产品创建表单（产品编码/名称/类型/险种类别/版本/描述）
- [ ] 实现产品详情页，包含 Tab 页签：
  - 基本信息 Tab：产品详情 + 编辑（仅草稿/驳回状态可编辑）
  - 保障定义 Tab：保障列表 CRUD + 关联缴费计划
  - 缴费计划 Tab：缴费计划列表 CRUD
  - 规则引用 Tab：规则列表 CRUD（引擎类型选择：liteflow/aviator/java）
  - 条款文档 Tab：文档列表 CRUD
  - 附加险 Tab：主附险关联管理
  - 变更历史 Tab：时间线展示变更记录
- [ ] 实现产品操作按钮：发布、冻结、克隆
- [ ] 产品状态标签（草稿/已发布/已冻结/已驳回）
- [ ] 配置路由：/product、/product/create、/product/:id

---

### F7 - 费率表管理

> 依赖：F6 | 验收：创建费率表 -> 导入行数据 -> 加载到 Redis -> 费率查询验证

**涉及 API：**
- POST/GET /api/rate-table
- GET /api/rate-table/{id}
- GET /api/rate-table/{id}/rows
- POST /api/rate-table/{id}/import
- GET /api/rate-table/query
- POST /api/rate-table/load
- POST /api/rate-table/evict

- [ ] 创建费率表 API 层（src/api/rateTable.ts）
- [ ] 实现费率表列表页（按产品筛选）
- [ ] 实现费率表创建表单（表编码/名称/产品关联/版本号/描述）
- [ ] 实现费率表详情页：表头信息 + 行数据表格
- [ ] 实现费率行数据批量导入（JSON 格式输入或表格编辑）
- [ ] 实现费率查询工具页（输入 tableCode + version + dimensionKey -> 展示费率值）
- [ ] 实现加载到 Redis / 清除缓存操作按钮
- [ ] 配置路由：/rate-table、/rate-table/:id、/rate-table/query

---

### F8 - 投保单管理

> 依赖：F5、F6 | 验收：创建投保单 -> 提交 -> 触发核保链 -> 查看详情

**涉及 API：**
- POST /api/proposal
- GET /api/proposal/{id}
- GET /api/proposal
- PUT /api/proposal/{id}/submit

- [ ] 创建投保单 API 层（src/api/proposal.ts）
- [ ] 实现投保单列表页（分页 + 状态筛选 + 渠道筛选 + 关键字搜索）
- [ ] 实现投保单创建表单（产品选择/投保人/被保人/保额/起保日期/渠道/渠道用户）
- [ ] 实现投保单详情页（完整信息展示 + 状态流转标签）
- [ ] 实现提交投保单操作（二次确认 -> 触发后端校验+核保链）
- [ ] 投保单状态标签（草稿/待核保/核保通过/核保拒绝/待支付/已支付/已出单/已撤销）
- [ ] 配置路由：/proposal、/proposal/create、/proposal/:id

---

### F9 - 核保工作台

> 依赖：F8 | 验收：核保员看到待核保列表 -> 执行自动核保 -> 人工核保填写结论 -> 记录查看

**涉及 API：**
- POST /api/underwriting/auto/{proposalId}
- POST /api/underwriting/manual/{proposalId}
- GET /api/underwriting/records/{proposalId}

- [ ] 创建核保 API 层（src/api/underwriting.ts）
- [ ] 实现核保工作台页面：
  - 待核保任务列表（调用投保单列表 API，status=待核保）
  - 核保操作面板（自动核保按钮 + 人工核保表单）
- [ ] 实现人工核保表单（核保结论：通过/加费/除外/拒保 + 备注）
- [ ] 实现核保记录查看（时间线展示每次核保结果）
- [ ] 配置路由：/underwriting、/underwriting/:proposalId

---

### F10 - 支付管理

> 依赖：F8 | 验收：保费计算 -> 创建支付记录 -> 查询支付状态 -> 超时扫描触发

**涉及 API：**
- POST /api/payment/calculate/{proposalId}
- POST /api/payment/create
- POST /api/payment/callback
- GET /api/payment/query/{proposalId}
- POST /api/payment/timeout-scan

- [ ] 创建支付 API 层（src/api/payment.ts）
- [ ] 实现支付管理页面：
  - 按投保单号查询支付记录
  - 支付记录列表（金额/状态/时间/流水号）
- [ ] 实现保费计算操作（输入投保单 ID -> 展示计算结果）
- [ ] 实现创建支付记录操作
- [ ] 实现超时扫描按钮（手动触发）
- [ ] 配置路由：/payment

---

### F11 - 保单管理

> 依赖：F10 | 验收：出单操作 -> 保单列表查询 -> 保单详情展示（含险种明细+缴费计划）

**涉及 API：**
- POST /api/policy/issue/{proposalId}
- GET /api/policy/{id}
- GET /api/policy

- [ ] 创建保单 API 层（src/api/policy.ts）
- [ ] 实现保单列表页（分页 + 保单号搜索 + 状态筛选 + 投保人筛选）
- [ ] 实现出单操作（从已支付投保单生成保单，二次确认）
- [ ] 实现保单详情页：
  - 基本信息区（保单号/产品名/投保人/被保人/保额/保费/起止日期/状态）
  - 险种明细 Tab（保障列表：险种名/保额/费率/保费）
  - 缴费计划 Tab（缴费期次/应缴日期/应缴金额/实缴状态）
- [ ] 保单状态标签（有效/宽限期/中止/终止/退保）
- [ ] 配置路由：/policy、/policy/:id

---

### F12 - 理赔管理

> 依赖：F11 | 验收：报案 -> 分配理赔员 -> 审核/调查/计算/赔付/结案全流程

**涉及 API：**
- 理赔：POST/GET /api/claim、POST .../assign、.../review、.../calculate、.../investigate、.../investigation-result
- 材料：POST/GET /api/claim/{claimId}/document
- 赔付：POST /api/claim/{claimId}/payment/process、.../callback、.../close、.../dispute

- [ ] 创建理赔 API 层（src/api/claim.ts）
- [ ] 实现理赔工作台页面：
  - 理赔列表（分页 + 保单号搜索 + 状态筛选 + 处理人筛选）
  - 快捷操作（分配理赔员）
- [ ] 实现理赔报案表单（保单号/出险时间/出险原因/报案描述/预估金额）
- [ ] 实现理赔详情页，包含操作区域：
  - 基本信息区（理赔号/保单号/报案信息/状态/处理人）
  - 操作按钮组（分配理赔员/提交审核/启动调查/赔付计算/触发赔付/结案/拒赔申诉）
  - 理赔材料 Tab（材料列表 + 上传功能）
  - 审核记录 Tab（审核结论/审核人/时间）
  - 调查记录 Tab（调查结论/调查人/时间）
  - 赔付信息 Tab（赔付金额/支付状态/支付流水）
- [ ] 实现审核表单（审核结论：通过/拒绝 + 审核意见）
- [ ] 实现调查结论表单（调查结论：正常/欺诈/部分欺诈 + 调查报告）
- [ ] 理赔状态标签（已报案/已分配/审核中/调查中/待赔付/已赔付/已结案/已拒赔/申诉中）
- [ ] 配置路由：/claim、/claim/create、/claim/:id

---

### F13 - 佣金管理

> 依赖：F11 | 验收：佣金列表查询 -> 计算佣金 -> 月度结算 -> 确认支付 -> 统计查看

**涉及 API：**
- GET /api/commission
- POST /api/commission/calculate
- POST /api/commission/settle
- GET /api/commission/summary
- PUT /api/commission/confirm

- [ ] 创建佣金 API 层（src/api/commission.ts）
- [ ] 实现佣金列表页（渠道筛选 + 月份筛选 + 状态筛选）
- [ ] 实现计算佣金表单（保单ID/渠道ID/渠道用户ID/保费金额/佣金类型/费率）
- [ ] 实现月度结算操作（输入 yearMonth -> 触发结算 -> 展示结果）
- [ ] 实现确认支付操作
- [ ] 实现月度汇总统计卡片（选择渠道+月份 -> 展示统计）
- [ ] 佣金状态标签（待结算/已结算/已支付）
- [ ] 配置路由：/commission

---

### F14 - 渠道管理

> 依赖：F2、F6 | 验收：创建渠道商 -> 管理账号 -> 授权产品 -> 状态切换

**涉及 API：**
- 渠道商：POST/GET/PUT /api/channel、PUT .../status、POST .../authorize、DELETE .../authorize/{productId}、GET .../products
- 渠道账号：POST/GET/PUT /api/channel/{channelId}/user、PUT .../status

- [ ] 创建渠道 API 层（src/api/channel.ts）
- [ ] 实现渠道商列表页（分页 + 关键字搜索 + 状态筛选）
- [ ] 实现渠道商创建表单（名称/编码/联系人/电话/地址/类型）
- [ ] 实现渠道商详情页，包含 Tab 页签：
  - 基本信息 Tab：渠道商详情 + 编辑 + 状态切换
  - 账号管理 Tab：账号列表 CRUD + 状态切换
  - 产品授权 Tab：已授权产品列表 + 授权操作（选择产品 + 首年费率 + 续期费率） + 撤销授权
- [ ] 渠道商状态标签（启用/停用/冻结）
- [ ] 配置路由：/channel、/channel/create、/channel/:id

---

### F15 - 工作台 + 全局优化

> 依赖：F3 ~ F14 | 验收：仪表盘数据正确展示 + 所有页面权限控制生效 + 响应式布局正常

- [ ] 实现工作台首页仪表盘：
  - 统计卡片（产品数/投保单数/保单数/理赔数）
  - 待办事项（待核保/待理赔/待支付数量）
  - 最近操作记录
- [ ] 实现前端权限控制：
  - 基于 JWT 解析角色/权限
  - 路由级别权限守卫
  - 按钮级别权限指令（v-permission）
- [ ] 全局 loading 状态优化
- [ ] 表格通用功能（列排序/列筛选/导出）
- [ ] 表单通用功能（必填校验/格式校验/提交防抖）
- [ ] 消息通知优化（成功/失败/警告统一提示）
- [ ] 响应式布局适配（侧边栏折叠 + 表格横向滚动）
- [ ] 字典数据全局缓存（Pinia store 初始化时加载）

---

## 阶段依赖关系

```
F1（脚手架）
 └─ F2（登录+布局）
     ├─ F3（数据字典）
     │   └─ F4（用户/角色/权限/数据范围）
     ├─ F5（客户管理）
     ├─ F6（产品管理）
     │   └─ F7（费率表管理）
     ├─ F14（渠道管理）
     ├─ F8（投保单）─── 依赖 F5 + F6
     │   ├─ F9（核保工作台）
     │   └─ F10（支付管理）
     │       └─ F11（保单管理）
     │           ├─ F12（理赔管理）
     │           └─ F13（佣金管理）
     └─ F15（工作台+全局优化）─── 依赖 F3~F14 全部完成
```

## 预估工期

| 阶段 | 预估工时 | 累计 |
|---|---|---|
| F1 - 项目初始化 | 1 天 | 1 天 |
| F2 - 登录+布局 | 2 天 | 3 天 |
| F3 - 数据字典 | 1 天 | 4 天 |
| F4 - 用户/角色/权限 | 3 天 | 7 天 |
| F5 - 客户管理 | 2 天 | 9 天 |
| F6 - 产品管理 | 4 天 | 13 天 |
| F7 - 费率表管理 | 2 天 | 15 天 |
| F8 - 投保单管理 | 2 天 | 17 天 |
| F9 - 核保工作台 | 2 天 | 19 天 |
| F10 - 支付管理 | 1 天 | 20 天 |
| F11 - 保单管理 | 2 天 | 22 天 |
| F12 - 理赔管理 | 4 天 | 26 天 |
| F13 - 佣金管理 | 2 天 | 28 天 |
| F14 - 渠道管理 | 2 天 | 30 天 |
| F15 - 工作台+优化 | 3 天 | 33 天 |
| **总计** | **33 个工作日** | **约 6.5 周** |
