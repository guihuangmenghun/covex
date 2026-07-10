# Covex 前端需求规格

> 本文档定义 Covex 保险核心平台前端的功能需求，与后端 133 个 API 端点一一对应。
> 前端技术栈：Vue 3 + Element Plus + Vite + TypeScript
> 用户群体：仅管理人员（保险业务提供方），不含 C 端用户页面

---

## 一、总体需求

### 1.1 系统入口

- 系统仅面向保险公司管理人员，无 C 端用户页面
- 用户通过登录页进入系统，使用 JWT Token 进行身份认证
- 登录成功后根据角色权限展示对应菜单和功能

### 1.2 认证与授权

| 需求项 | 说明 |
|---|---|
| 认证方式 | JWT Token，登录后存入 localStorage |
| Token 注入 | axios 请求拦截器自动添加 `Authorization: Bearer <token>` |
| Token 过期 | 响应拦截器捕获 401，自动跳转登录页并清除 Token |
| 路由守卫 | 未登录用户访问任何页面自动重定向至 /login |
| 权限控制 | 基于角色的菜单可见性 + 按钮级权限指令（v-permission） |

### 1.3 通用交互规范

| 规范 | 说明 |
|---|---|
| 列表页 | 统一使用 Element Plus el-table 组件，支持分页、排序、列筛选 |
| 表单页 | 统一使用 Element Plus el-form 组件，支持必填校验、格式校验、提交防抖 |
| 删除操作 | 所有删除操作弹出二次确认对话框（ElMessageBox.confirm） |
| 状态标签 | 使用 Element Plus el-tag / el-badge 组件，不同状态对应不同颜色 |
| 成功/失败提示 | 使用 Element Plus ElMessage 组件，成功绿色、失败红色、警告橙色 |
| 加载状态 | 请求期间展示 loading 动画，按钮置灰防止重复提交 |
| 日期格式 | 统一展示格式：YYYY-MM-DD HH:mm:ss |
| 金额格式 | 统一展示格式：￥X,XXX.XX（千位分隔，保留两位小数） |
| 脱敏展示 | 证件号、手机号、银行账号在列表中脱敏，详情页可点击显示 |

---

## 二、页面功能需求

### 2.1 登录页（/login）

**功能描述：** 系统唯一入口，管理员通过用户名和密码登录。

**对应 API：** POST /api/user/login

**页面元素：**
- 系统 Logo + 标题「Covex 保险核心平台」
- 用户名输入框（必填）
- 密码输入框（必填，支持密码显示/隐藏切换）
- 登录按钮
- 表单验证提示

**交互规则：**
1. 点击登录按钮，校验必填项后调用 POST /api/user/login
2. 登录成功：存储 Token -> 获取用户信息 -> 跳转 /dashboard
3. 登录失败：展示错误提示（用户名或密码错误 / 账号已停用）
4. 登录过程中按钮展示 loading 状态，防止重复提交
5. 按 Enter 键可触发登录

---

### 2.2 工作台首页（/dashboard）

**功能描述：** 系统首页仪表盘，展示核心业务数据和待办事项。

**对应 API：** 复用各域列表 API 获取统计数据

**页面元素：**
- 统计卡片区域（4 张卡片）：
  - 产品总数（调用 GET /api/product，取 total）
  - 待核保投保单数（调用 GET /api/proposal?status=待核保，取 total）
  - 有效保单数（调用 GET /api/policy?status=有效，取 total）
  - 待处理理赔数（调用 GET /api/claim?status=已分配，取 total）
- 待办事项列表：
  - 待核保任务（最近 5 条）
  - 待理赔任务（最近 5 条）
  - 待支付投保单（最近 5 条）
- 最近操作记录（最近 10 条系统操作）

**交互规则：**
1. 页面加载时并行请求各统计数据
2. 统计卡片支持点击跳转到对应列表页
3. 待办事项列表条目支持点击跳转到详情页
4. 支持手动刷新

---

### 2.3 产品列表（/product）

**功能描述：** 展示所有保险产品，支持筛选和快捷操作。

**对应 API：** GET /api/product

**页面元素：**
- 搜索栏：关键字输入框 + 产品类型下拉框 + 版本状态下拉框 + 搜索按钮 + 重置按钮
- 操作栏：「新建产品」按钮
- 数据表格：
  - 列：产品编码、产品名称、产品类型、险种类别、版本号、版本状态、创建时间、操作
  - 操作列：查看详情、编辑（仅草稿/驳回）、克隆、发布（仅草稿）、冻结（仅已发布）
- 分页器

**交互规则：**
1. 页面加载时默认查询第 1 页
2. 产品类型筛选调用字典 API（dict_type = product_type）
3. 版本状态筛选：草稿(0)/已发布(1)/已冻结(2)/已驳回(3)
4. 点击「查看详情」跳转到 /product/:id
5. 点击「克隆」弹出确认框，确认后调用 POST /api/product/{id}/clone
6. 点击「发布」弹出确认框，确认后调用 PUT /api/product/{id}/publish
7. 点击「冻结」弹出确认框，确认后调用 PUT /api/product/{id}/freeze

---

### 2.4 产品创建（/product/create）

**功能描述：** 新建保险产品的基本信息。

**对应 API：** POST /api/product

**表单字段：**
- 产品编码（必填，唯一标识）
- 产品名称（必填）
- 产品类型（必填，下拉选择，来源字典 product_type）
- 险种类别（必填，下拉选择）
- 版本号（默认 1.0.0）
- 产品描述（文本域）

**交互规则：**
1. 产品编码输入后失焦时做唯一性校验（可调用查询接口验证）
2. 提交成功后跳转到产品详情页
3. 提交失败展示后端返回的错误信息

---

### 2.5 产品详情（/product/:id）

**功能描述：** 展示产品完整信息，包含 7 个子模块的 Tab 页签管理。

**对应 API：**
- GET /api/product/:id（基本信息 + 关联数据）
- PUT /api/product/:id（编辑基本信息）
- 保障：/api/product/{productId}/coverage（8 个端点）
- 缴费：/api/product/{productId}/premium（5 个端点）
- 规则：/api/product/{productId}/rule（5 个端点）
- 文档：/api/product/{productId}/document（5 个端点）
- 附险：/api/product/{productId}/rider（3 个端点）
- 历史：/api/product/{id}/changelog

**页面结构：**

**基本信息 Tab：**
- 展示产品基础字段（编码/名称/类型/版本/状态/描述）
- 编辑按钮（仅 version_status = 0 草稿 或 3 驳回 时可见）
- 点击编辑切换为表单模式，保存调用 PUT /api/product/:id

**保障定义 Tab：**
- 保障列表表格（保障编码/名称/类型/保额/免赔额/等待期）
- 「新增保障」按钮 -> 弹窗表单
- 每行操作：编辑、删除、关联缴费计划、查看关联
- 关联缴费计划：弹窗选择缴费计划 -> 调用 link-premium
- 取消关联：调用 unlink-premium

**缴费计划 Tab：**
- 缴费计划列表表格（计划编码/名称/缴费方式/缴费频率/缴费期限/总期数）
- 「新增缴费计划」按钮 -> 弹窗表单
- 每行操作：编辑、删除

**规则引用 Tab：**
- 规则列表表格（规则编码/名称/引擎类型/规则阶段/优先级）
- 「新增规则」按钮 -> 弹窗表单
- 引擎类型下拉：liteflow / aviator / java
- 规则阶段下拉：underwriting / pricing / claim / commission
- 每行操作：编辑、删除

**条款文档 Tab：**
- 文档列表表格（文档名/文档类型/版本号/文件路径）
- 「新增文档」按钮 -> 弹窗表单
- 每行操作：编辑、删除

**附加险 Tab：**
- 附加险关联列表（附加险编码/附加险名称/最大份数/是否必选）
- 「新增关联」按钮 -> 弹窗选择产品
- 每行操作：删除

**变更历史 Tab：**
- 时间线组件展示变更历史（时间/操作类型/操作人/变更描述）
- 调用 GET /api/product/{id}/changelog

---

### 2.6 费率表列表（/rate-table）

**功能描述：** 管理费率表，支持按产品筛选。

**对应 API：** GET /api/rate-table

**页面元素：**
- 筛选栏：产品选择下拉框
- 「新建费率表」按钮
- 数据表格：
  - 列：表编码、表名称、关联产品、版本号、创建时间、操作
  - 操作列：查看详情、加载到 Redis、清除缓存
- 操作列的「加载到 Redis」调用 POST /api/rate-table/load
- 操作列的「清除缓存」调用 POST /api/rate-table/evict

---

### 2.7 费率表详情（/rate-table/:id）

**功能描述：** 查看和管理费率表行数据。

**对应 API：**
- GET /api/rate-table/:id
- GET /api/rate-table/:id/rows
- POST /api/rate-table/:id/import

**页面元素：**
- 表头信息区：表编码、表名称、关联产品、版本号、描述
- 行数据表格：
  - 列：维度键（dimension_key）、费率值（rate_value）、最小值、最大值、备注
  - 支持排序
- 「批量导入」按钮 -> 弹窗：JSON 格式输入框或动态表格行编辑
- 导入后自动刷新行数据

---

### 2.8 费率查询工具（/rate-table/query）

**功能描述：** 手动查询费率值，验证 Redis -> DB 二级查询逻辑。

**对应 API：** GET /api/rate-table/query

**页面元素：**
- 查询表单：表编码（必填）、版本号（必填）、维度键（必填）、查询按钮
- 结果展示区：表格展示查询结果（表编码/版本/维度键/费率值）
- 数据来源标识（Redis 命中 / DB 回源）

---

### 2.9 投保单列表（/proposal）

**功能描述：** 管理所有投保单，支持多维度筛选。

**对应 API：** GET /api/proposal

**页面元素：**
- 搜索栏：状态下拉框 + 渠道下拉框 + 关键字输入框 + 搜索 + 重置
- 「新建投保单」按钮
- 数据表格：
  - 列：投保单号、产品名称、投保人、总保费、状态、渠道、创建时间、操作
  - 操作列：查看详情、提交（仅草稿状态）
- 分页器

**状态标签颜色映射：**
- 草稿：灰色
- 待核保：蓝色
- 核保通过：绿色
- 核保拒绝：红色
- 待支付：橙色
- 已支付：绿色
- 已出单：青色
- 已撤销：灰色（虚线边框）

---

### 2.10 投保单创建（/proposal/create）

**功能描述：** 录入新投保单信息。

**对应 API：** POST /api/proposal

**表单字段：**
- 产品选择（必填，下拉搜索，调用 GET /api/product?versionStatus=1）
- 投保人（必填，下拉搜索客户，调用 GET /api/customer）
- 被保人（必填，下拉搜索客户）
- 保额（必填，数字输入）
- 起保日期（必填，日期选择器）
- 保险期间（必填，数字 + 单位选择）
- 渠道选择（下拉，调用 GET /api/channel）
- 渠道用户（下拉，联动渠道选择）
- 受益人信息（文本域）

**交互规则：**
1. 选择产品后自动加载该产品的保障列表供勾选
2. 选择渠道后联动加载渠道用户
3. 提交成功后跳转到投保单详情页

---

### 2.11 投保单详情（/proposal/:id）

**功能描述：** 展示投保单完整信息和操作按钮。

**对应 API：**
- GET /api/proposal/:id
- PUT /api/proposal/:id/submit

**页面元素：**
- 基本信息区：投保单号、产品名、投保人、被保人、保额、保费、状态、渠道、创建时间
- 操作按钮区（根据状态动态展示）：
  - 「提交投保单」按钮（仅草稿状态可见）-> 调用 PUT /api/proposal/:id/submit
  - 「计算保费」按钮（调用 POST /api/payment/calculate/:id）
  - 「出单」按钮（仅已支付状态可见）-> 调用 POST /api/policy/issue/:id
- 关联信息 Tab：
  - 产品快照 Tab：投保时快照的产品信息
  - 支付记录 Tab：调用 GET /api/payment/query/:id

---

### 2.12 核保工作台（/underwriting）

**功能描述：** 核保员工作台，展示待核保任务列表。

**对应 API：**
- GET /api/proposal?status=待核保（获取待核保列表）
- POST /api/underwriting/auto/:proposalId
- POST /api/underwriting/manual/:proposalId
- GET /api/underwriting/records/:proposalId

**页面元素：**
- 待核保任务列表表格：
  - 列：投保单号、产品名称、投保人、保额、提交时间、操作
  - 操作列：查看详情、自动核保、人工核保
- 「自动核保」按钮 -> 调用 POST /api/underwriting/auto/:proposalId -> 展示结果
- 「人工核保」按钮 -> 弹出人工核保表单弹窗

**人工核保表单：**
- 核保结论（必填，单选：通过/加费/除外/拒保）
- 加费金额（结论为「加费」时展示）
- 除外责任描述（结论为「除外」时展示）
- 核保备注（文本域）

---

### 2.13 核保详情（/underwriting/:proposalId）

**功能描述：** 查看某个投保单的完整核保记录。

**对应 API：** GET /api/underwriting/records/:proposalId

**页面元素：**
- 投保单基本信息（投保单号/产品/投保人/保额/状态）
- 核保记录时间线：
  - 每条记录：时间、核保类型（自动/人工）、结论、操作人、备注
- 操作区域：手动触发自动核保、人工核保（复用核保工作台操作）

---

### 2.14 支付管理（/payment）

**功能描述：** 查询和管理支付记录。

**对应 API：**
- POST /api/payment/calculate/:proposalId
- POST /api/payment/create
- GET /api/payment/query/:proposalId
- POST /api/payment/timeout-scan

**页面元素：**
- 查询区域：投保单号输入框 + 查询按钮
- 操作区域：
  - 「计算保费」按钮（输入投保单 ID -> 调用 calculate -> 展示结果）
  - 「创建支付记录」按钮 -> 弹窗表单
  - 「超时扫描」按钮 -> 调用 timeout-scan -> 展示处理数量
- 支付记录表格：
  - 列：支付流水号、投保单号、支付金额、支付方式、支付状态、创建时间、完成时间
- 支付状态标签：待支付(灰)/支付中(蓝)/支付成功(绿)/支付失败(红)/已超时(橙)

---

### 2.15 保单列表（/policy）

**功能描述：** 管理所有保单。

**对应 API：** GET /api/policy

**页面元素：**
- 搜索栏：保单号输入框 + 状态下拉框 + 投保人下拉框 + 搜索 + 重置
- 数据表格：
  - 列：保单号、产品名称、投保人、总保费、保额、起保日期、止保日期、状态、操作
  - 操作列：查看详情
- 分页器

**状态标签颜色映射：**
- 有效：绿色
- 宽限期：橙色
- 中止：红色
- 终止：灰色
- 退保：紫色

---

### 2.16 保单详情（/policy/:id）

**功能描述：** 展示保单完整信息，含险种明细和缴费计划。

**对应 API：** GET /api/policy/:id

**页面元素：**
- 基本信息区：
  - 保单号、产品名称、投保人姓名、被保人姓名
  - 总保费、总保额、起保日期、止保日期
  - 缴费方式、缴费频率、保单状态
- Tab 页签：
  - 险种明细 Tab：表格展示（保障名称/保额/费率/保费/免赔额/等待期）
  - 缴费计划 Tab：表格展示（期次/应缴日期/应缴金额/实缴金额/实缴日期/状态）
  - 理赔记录 Tab：关联的理赔案件列表（如有）

---

### 2.17 理赔工作台（/claim）

**功能描述：** 理赔管理主页面，展示所有理赔案件。

**对应 API：** GET /api/claim

**页面元素：**
- 搜索栏：保单号输入框 + 状态下拉框 + 处理人下拉框 + 搜索 + 重置
- 「理赔报案」按钮（跳转 /claim/create）
- 数据表格：
  - 列：理赔号、保单号、出险时间、报案时间、预估金额、实际赔付、状态、处理人、操作
  - 操作列：查看详情、分配理赔员（仅已报案状态）
- 分页器

**状态标签颜色映射：**
- 已报案：蓝色
- 已分配：青色
- 审核中：橙色
- 调查中：紫色
- 待赔付：橙色
- 已赔付：绿色
- 已结案：灰色
- 已拒赔：红色
- 申诉中：黄色

---

### 2.18 理赔报案（/claim/create）

**功能描述：** 录入理赔报案信息。

**对应 API：** POST /api/claim

**表单字段：**
- 保单号（必填，输入或下拉搜索，调用 GET /api/policy）
- 出险时间（必填，日期时间选择器）
- 出险原因（必填，下拉选择，来源字典 claim_reason）
- 出险地点（文本输入）
- 报案描述（必填，文本域）
- 预估金额（数字输入）
- 报案人联系方式（手机号输入）

**交互规则：**
1. 输入保单号后自动查询保单信息（调用 GET /api/policy 验证保单有效性）
2. 保单无效（已终止/已退保）时提示不可报案
3. 提交成功后跳转到理赔详情页

---

### 2.19 理赔详情（/claim/:id）

**功能描述：** 理赔案件完整信息和全流程操作面板。

**对应 API：**
- 理赔：GET /api/claim/:id、POST .../assign、.../review、.../calculate、.../investigate、.../investigation-result
- 材料：POST/GET /api/claim/:id/document
- 赔付：POST /api/claim/:id/payment/process、.../callback、.../close、.../dispute

**页面结构：**

**基本信息区：**
- 理赔号、保单号、出险时间、出险原因、报案描述
- 预估金额、实际赔付金额、状态、处理人、创建时间

**操作按钮组（根据状态动态展示）：**
- 「分配理赔员」-> 调用 POST /api/claim/:id/assign（自动分配当前登录用户）
- 「提交审核」-> 弹窗表单（审核结论：通过/拒绝 + 审核意见）-> 调用 POST /api/claim/:id/review
- 「赔付计算」-> 调用 POST /api/claim/:id/calculate -> 展示计算结果
- 「启动调查」-> 调用 POST /api/claim/:id/investigate
- 「提交调查结论」-> 弹窗表单（结论：正常/欺诈/部分欺诈 + 报告）-> 调用 POST /api/claim/:id/investigation-result
- 「触发赔付」-> 调用 POST /api/claim/:id/payment/process
- 「结案」-> 调用 POST /api/claim/:id/payment/close
- 「拒赔申诉」-> 调用 POST /api/claim/:id/payment/dispute

**Tab 页签：**

**理赔材料 Tab：**
- 材料列表表格（文件名/文件类型/上传时间/审核状态）
- 「上传材料」按钮 -> 弹窗表单（文件名/文件类型/文件路径/描述）
- 调用 GET /api/claim/:id/document 获取列表
- 调用 POST /api/claim/:id/document 上传

**审核记录 Tab：**
- 时间线展示审核记录（审核人/结论/意见/时间）

**调查记录 Tab：**
- 时间线展示调查记录（调查人/结论/报告/时间）

**赔付信息 Tab：**
- 赔付记录（赔付金额/支付方式/支付状态/流水号/创建时间/完成时间）

---

### 2.20 佣金管理（/commission）

**功能描述：** 管理渠道佣金，包含计算、结算、统计功能。

**对应 API：**
- GET /api/commission
- POST /api/commission/calculate
- POST /api/commission/settle
- GET /api/commission/summary
- PUT /api/commission/confirm

**页面元素：**
- 搜索栏：渠道下拉框 + 月份选择器（YYYY-MM）+ 状态下拉框 + 搜索 + 重置
- 操作按钮区：
  - 「计算佣金」按钮 -> 弹窗表单（保单ID/渠道ID/渠道用户ID/保费金额/佣金类型/费率）
  - 「月度结算」按钮 -> 弹窗输入年月 -> 调用 settle -> 展示结算结果
  - 「月度统计」按钮 -> 弹窗选择渠道+月份 -> 调用 summary -> 展示统计卡片
- 佣金列表表格：
  - 列：佣金编号、渠道名称、渠道用户、保单号、保费金额、佣金类型、佣金金额、佣金费率、结算月份、状态、操作
  - 操作列：确认支付（仅待结算状态）
- 佣金状态标签：待结算(橙)/已结算(蓝)/已支付(绿)

---

### 2.21 客户列表（/customer）

**功能描述：** 管理所有客户信息。

**对应 API：** GET /api/customer

**页面元素：**
- 搜索栏：关键字输入框（姓名/证件号/手机号）+ 搜索 + 重置
- 「新建客户」按钮
- 数据表格：
  - 列：客户姓名、证件类型、证件号（脱敏）、性别、手机号（脱敏）、创建时间、操作
  - 操作列：查看详情、编辑
- 分页器

---

### 2.22 客户创建（/customer/create）

**功能描述：** 录入新客户信息。

**对应 API：** POST /api/customer

**表单字段：**
- 客户姓名（必填）
- 证件类型（必填，下拉选择：身份证/护照/军官证/其他）
- 证件号码（必填，根据证件类型做格式校验）
- 性别（单选：男/女）
- 出生日期（日期选择器）
- 手机号（必填，手机号格式校验）
- 邮箱（邮箱格式校验）
- 国籍（下拉选择）
- 职业（文本输入）

**交互规则：**
1. 证件号实时校验是否已存在（调用查询接口）
2. 提交成功后跳转到客户详情页
3. 证件号格式校验规则：
   - 身份证：18 位数字 + 校验位
   - 护照：字母 + 数字，5-20 位

---

### 2.23 客户详情（/customer/:id）

**功能描述：** 展示客户完整信息，包含地址、银行账户、健康档案。

**对应 API：**
- 客户：GET /api/customer/:id、PUT /api/customer/:id、POST .../ensure-applicant、POST .../ensure-insured、PUT .../health
- 地址：POST/GET/PUT/DELETE /api/customer/:id/address、PUT .../default
- 银行账户：POST/GET/PUT/DELETE /api/customer/:id/bank-account、PUT .../default

**页面结构：**

**基本信息 Tab：**
- 客户基础字段展示 + 编辑按钮
- 操作按钮：「设为投保人」（调用 ensure-applicant）、「设为被保人」（调用 ensure-insured）

**联系地址 Tab：**
- 地址列表表格（地址类型/省/市/区/详细地址/是否默认/操作）
- 「新增地址」按钮 -> 弹窗表单（地址类型/省市区级联/详细地址/邮编）
- 每行操作：编辑、删除、设为默认
- 地址类型：通讯地址/账单地址/居住地址

**银行账户 Tab：**
- 银行账户列表（银行名称/账号(脱敏)/开户行/用途类型/是否默认/操作）
- 「新增银行账户」按钮 -> 弹窗表单（银行名称/账号/开户行/用途类型/户名）
- 每行操作：编辑、删除、设为默认
- 删除保护：有代扣协议关联的账户不允许删除（后端校验，前端展示错误提示）
- 用途类型：保费扣款/理赔收款/佣金收款

**健康档案 Tab：**
- 三个子区域（JSON 表单编辑）：
  - 既往病史（medical_history）：动态列表，每条包含疾病名称/诊断时间/治疗状态
  - 家族病史（family_history）：动态列表，每条包含亲属关系/疾病名称/发病年龄
  - 当前用药（current_medications）：动态列表，每条包含药品名称/剂量/频率/开始时间
- 「保存健康档案」按钮 -> 调用 PUT /api/customer/:id/health

---

### 2.24 渠道商列表（/channel）

**功能描述：** 管理所有渠道商。

**对应 API：** GET /api/channel

**页面元素：**
- 搜索栏：关键字输入框 + 状态下拉框 + 搜索 + 重置
- 「新建渠道商」按钮
- 数据表格：
  - 列：渠道编码、渠道名称、联系人、联系电话、渠道类型、状态、创建时间、操作
  - 操作列：查看详情、编辑、状态切换
- 分页器

---

### 2.25 渠道商创建（/channel/create）

**功能描述：** 新建渠道商。

**对应 API：** POST /api/channel

**表单字段：**
- 渠道编码（必填，唯一标识）
- 渠道名称（必填）
- 联系人（必填）
- 联系电话（必填）
- 联系邮箱
- 渠道地址
- 渠道类型（下拉选择：代理人/经纪人/银保/互联网/其他）
- 描述

---

### 2.26 渠道商详情（/channel/:id）

**功能描述：** 渠道商完整信息管理，含账号和产品授权。

**对应 API：**
- 渠道商：GET /api/channel/:id、PUT /api/channel/:id、PUT .../status、POST .../authorize、DELETE .../authorize/:productId、GET .../products
- 账号：POST/GET/PUT /api/channel/:id/user、PUT .../status

**页面结构：**

**基本信息 Tab：**
- 渠道商基础字段展示 + 编辑按钮
- 状态切换按钮（启用/停用/冻结）

**账号管理 Tab：**
- 账号列表表格（用户名/真实姓名/手机号/状态/创建时间/操作）
- 「新增账号」按钮 -> 弹窗表单（用户名/密码/真实姓名/手机号/邮箱）
- 每行操作：编辑、状态切换（启用/停用）
- 调用 GET /api/channel/:id/user 获取列表
- 调用 POST /api/channel/:id/user 创建
- 调用 PUT /api/channel/:id/user/:userId 编辑
- 调用 PUT /api/channel/:id/user/:userId/status 切换状态

**产品授权 Tab：**
- 已授权产品表格（产品编码/产品名称/首年费率/续期费率/授权时间/操作）
- 「授权产品」按钮 -> 弹窗表单（产品选择/首年费率/续期费率）
- 每行操作：撤销授权（二次确认）
- 调用 GET /api/channel/:id/products 获取列表
- 调用 POST /api/channel/:id/authorize 授权
- 调用 DELETE /api/channel/:id/authorize/:productId 撤销

---

### 2.27 用户管理（/system/user）

**功能描述：** 系统用户管理，含 CRUD 和角色分配。

**对应 API：**
- POST /api/user、GET /api/user、GET /api/user/:id、PUT /api/user/:id、PUT /api/user/:id/status
- POST /api/user/:id/roles、GET /api/user/:id/roles、GET /api/user/:id/permissions

**页面元素：**
- 搜索栏：关键字输入框 + 搜索 + 重置
- 「新建用户」按钮
- 数据表格：
  - 列：用户名、真实姓名、手机号、邮箱、用户类型、状态、创建时间、操作
  - 操作列：编辑、状态切换、分配角色、查看权限
- 分页器

**弹窗表单：**
- 新建用户：用户名(必填)/密码(必填)/真实姓名/手机号/邮箱/用户类型
- 编辑用户：真实姓名/手机号/邮箱/用户类型（不可改用户名）
- 分配角色：穿梭框/多选列表，展示所有角色，标记已分配角色
- 查看权限：只读列表，按模块分组展示用户的所有权限

---

### 2.28 角色管理（/system/role）

**功能描述：** 系统角色管理，含权限分配。

**对应 API：**
- POST/GET/PUT/DELETE /api/role
- POST /api/role/:id/permissions、GET /api/role/:id/permissions

**页面元素：**
- 「新建角色」按钮
- 角色列表表格：
  - 列：角色编码、角色名称、描述、创建时间、操作
  - 操作列：编辑、删除、分配权限、配置数据范围
- 弹窗表单：
  - 新建/编辑角色：角色编码/角色名称/描述
  - 分配权限：树形权限选择（按模块分组，GET /api/permission/modules），支持全选/反选/父子联动

---

### 2.29 权限管理（/system/permission）

**功能描述：** 系统权限项管理。

**对应 API：** POST/GET /api/permission、GET /api/permission/modules

**页面元素：**
- 「新建权限」按钮
- 权限展示模式切换：列表视图 / 模块分组视图
- 列表视图：权限编码/权限名称/模块/操作类型
- 模块分组视图：按模块折叠分组展示
- 新建权限弹窗：权限编码/权限名称/模块下拉/操作类型下拉（view/create/edit/delete/approve）

---

### 2.30 数据范围管理（/system/data-scope）

**功能描述：** 配置角色的数据访问范围。

**对应 API：** POST/GET /api/data-scope/:roleId

**页面元素：**
- 角色选择下拉框（调用 GET /api/role）
- 选择角色后加载当前数据范围（调用 GET /api/data-scope/:roleId）
- 数据范围配置表单：
  - 范围类型（单选：全部数据/本部门数据/自定义数据/仅本人数据）
  - 自定义数据时展示可选范围列表
- 「保存」按钮 -> 调用 POST /api/data-scope/:roleId

---

### 2.31 数据字典管理（/system/dict）

**功能描述：** 管理系统字典数据。

**对应 API：** GET /api/dict、GET /api/dict/:dictType、GET /api/dict/:dictType/children、POST/PUT/DELETE /api/dict、POST /api/dict/cache/evict

**页面元素：**
- 左侧面板：字典类型列表（从 GET /api/dict 提取所有 dict_type），支持搜索
- 右侧面板：选中类型后展示字典项列表
- 字典项表格：
  - 列：字典编码、字典标签、排序号、父编码、状态、操作
  - 操作列：编辑、删除
- 「新增字典项」按钮 -> 弹窗表单（dict_type/dict_code/dict_label/sort_order/parent_code/remark）
- 「清空缓存」按钮 -> 调用 POST /api/dict/cache/evict
- 层级字典：树形展示，支持展开/折叠

---

## 三、权限控制矩阵

### 3.1 角色与菜单映射

| 菜单/功能 | 管理员 | 产品经理 | 核保员 | 理赔员 | 渠道管理员 |
|---|:---:|:---:|:---:|:---:|:---:|
| 工作台 | Y | Y | Y | Y | Y |
| 产品管理 | Y | Y | - | - | - |
| 费率表管理 | Y | Y | - | - | - |
| 投保单管理 | Y | Y | Y | - | - |
| 核保工作台 | Y | - | Y | - | - |
| 支付管理 | Y | Y | - | - | - |
| 保单管理 | Y | Y | Y | Y | - |
| 理赔管理 | Y | - | - | Y | - |
| 佣金管理 | Y | - | - | - | Y |
| 客户管理 | Y | Y | Y | Y | Y |
| 渠道管理 | Y | - | - | - | Y |
| 系统管理 | Y | - | - | - | - |

### 3.2 权限控制实现

| 层级 | 控制方式 | 说明 |
|---|---|---|
| 路由级 | beforeEach 路由守卫 | 根据角色过滤可访问路由，无权限跳转 403 页面 |
| 菜单级 | 动态菜单渲染 | 根据用户角色动态生成侧边栏菜单项 |
| 按钮级 | v-permission 指令 | 控制操作按钮的可见性，如 `<el-button v-permission="'product:publish'">发布</el-button>` |
| 接口级 | 后端 Spring Security | 前端仅做展示控制，实际权限校验在后端完成 |

### 3.3 预置权限编码

| 权限编码 | 模块 | 操作 | 说明 |
|---|---|---|---|
| product:view | 产品管理 | 查看 | 查看产品列表和详情 |
| product:create | 产品管理 | 创建 | 新建产品 |
| product:update | 产品管理 | 编辑 | 编辑产品信息 |
| product:publish | 产品管理 | 发布 | 发布产品 |
| product:freeze | 产品管理 | 冻结 | 冻结产品 |
| rate_table:view | 费率表 | 查看 | 查看费率表 |
| rate_table:update | 费率表 | 编辑 | 管理费率表数据 |
| rate_table:cache | 费率表 | 缓存 | 操作 Redis 缓存 |
| proposal:view | 投保单 | 查看 | 查看投保单 |
| proposal:create | 投保单 | 创建 | 新建投保单 |
| proposal:submit | 投保单 | 提交 | 提交投保单 |
| underwriting:view | 核保 | 查看 | 查看核保记录 |
| underwriting:auto | 核保 | 自动 | 触发自动核保 |
| underwriting:manual | 核保 | 人工 | 人工核保 |
| payment:view | 支付 | 查看 | 查看支付记录 |
| payment:operate | 支付 | 操作 | 创建支付/计算保费 |
| policy:view | 保单 | 查看 | 查看保单 |
| policy:issue | 保单 | 出单 | 生成保单 |
| claim:view | 理赔 | 查看 | 查看理赔案件 |
| claim:create | 理赔 | 报案 | 创建理赔报案 |
| claim:assign | 理赔 | 分配 | 分配理赔员 |
| claim:review | 理赔 | 审核 | 提交审核结论 |
| claim:investigate | 理赔 | 调查 | 启动调查/提交结论 |
| claim:payment | 理赔 | 赔付 | 触发赔付/结案 |
| commission:view | 佣金 | 查看 | 查看佣金列表 |
| commission:settle | 佣金 | 结算 | 月度结算/确认支付 |
| customer:view | 客户 | 查看 | 查看客户信息 |
| customer:update | 客户 | 编辑 | 编辑客户信息 |
| channel:view | 渠道 | 查看 | 查看渠道商 |
| channel:update | 渠道 | 编辑 | 管理渠道商 |
| channel:authorize | 渠道 | 授权 | 产品授权/撤销 |
| system:user | 系统 | 用户 | 用户管理 |
| system:role | 系统 | 角色 | 角色管理 |
| system:permission | 系统 | 权限 | 权限管理 |
| system:dict | 系统 | 字典 | 字典管理 |

---

## 四、数据字典依赖

前端页面中需要使用的字典类型（调用 GET /api/dict/{dictType}）：

| 字典类型 | 使用场景 |
|---|---|
| product_type | 产品类型筛选（寿险/车险/财产险/乘务险等） |
| insurance_category | 险种类别 |
| version_status | 产品版本状态（草稿/已发布/已冻结/已驳回） |
| proposal_status | 投保单状态 |
| policy_status | 保单状态 |
| claim_status | 理赔状态 |
| claim_reason | 出险原因 |
| payment_status | 支付状态 |
| payment_method | 支付方式 |
| commission_type | 佣金类型（首年/续期） |
| commission_status | 佣金状态 |
| channel_type | 渠道类型 |
| channel_status | 渠道商状态 |
| user_type | 用户类型 |
| id_type | 证件类型 |
| gender | 性别 |
| address_type | 地址类型 |
| account_usage_type | 银行账户用途 |
| rule_engine | 规则引擎类型（liteflow/aviator/java） |
| rule_stage | 规则阶段（underwriting/pricing/claim/commission） |
| uw_conclusion | 核保结论（通过/加费/除外/拒保） |
| review_conclusion | 审核结论（通过/拒绝） |
| investigation_conclusion | 调查结论（正常/欺诈/部分欺诈） |
| document_type | 文档类型 |
| coverage_type | 保障类型 |
| premium_frequency | 缴费频率 |
| premium_method | 缴费方式 |

---

## 五、非功能需求

| 需求项 | 要求 |
|---|---|
| 首屏加载 | < 3 秒（生产环境 gzip 后） |
| 列表页加载 | < 1 秒 |
| 表单提交响应 | < 2 秒 |
| 浏览器兼容 | Chrome 90+、Edge 90+、Firefox 90+ |
| 响应式布局 | 最小支持 1280x768 分辨率 |
| 代码规范 | ESLint + Prettier 统一风格，TypeScript 严格模式 |
| 国际化 | 暂不支持，仅中文 |
| 无障碍 | 基本键盘导航支持（Tab 切换、Enter 确认） |
