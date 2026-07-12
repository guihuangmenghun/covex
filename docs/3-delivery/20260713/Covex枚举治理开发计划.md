# 枚举值一致性审计与前端硬编码治理 — 执行计划

> **需求文档**：`docs/1-specs/data-model/枚举值一致性审计与前端硬编码治理.md`
> **核心原则**：前端状态码/类型码的 label 必须通过 dictStore 获取，禁止硬编码映射表

## 三方数据一致性模型

```
ins_dict 表 (MySQL, 唯一权威源)
    → Redis 缓存 (运行时高速查询)
        → 前端 dictStore (API 拉取，存入 Pinia)
```

新增/修改枚举值时：只改 DB → Redis 自动同步 → 前端自动生效。前端文件中不存任何状态码映射。

---

## Task 1：审计三方数据现状

### 1.1 DB 层审计
- [ ] 确认 `ins_dict` 表 39 组 175 条记录完整（已验证，见 `项目枚举值.md`）
- [ ] 确认无脏数据（is_deleted=0 但实际废弃的记录）

### 1.2 Redis 层审计
- [ ] `redis-cli KEYS *dict*` 查看缓存 key 格式
- [ ] 确认 TTL 设置
- [ ] 确认缓存是否存在（可能从未加载过）

### 1.3 API 层审计
- [ ] 找到 dict 相关 Controller（GET /api/dict 或类似）
- [ ] 确认 API 返回数据是否与 DB 一致
- [ ] 确认 API 是否优先查 Redis

### 1.4 前端层审计
- [ ] 读取 `Covex-ui/src/stores/dict.ts`，确认 dictStore 初始化流程
- [ ] 确认 dictStore 调用的 API 路径和数据格式
- [ ] 确认 dictStore 是否有缓存过期机制

### 1.5 后端代码与字典一致性审计（3 层深度审计）

> 按需求文档“原则 2”执行：先确认后端代码实际行为，再决定修复方向
> **禁止 grep 扫描代替全量阅读**：必须逐文件读取完整方法体

**第 1 层：逐 Service 全量读取方法体**

按以下顺序逐个读取 Service 完整代码，检查每个涉及枚举字段的方法：

| 序号 | Service 文件 | 涉及枚举字段 | 重点检查 |
|---|---|---|---|
| 1 | ClaimService.java | status(11值) | 多级审核流转是否覆盖全部状态 |
| 2 | ClaimPaymentService.java | status(11值) | 赔付→结案流程 |
| 3 | PaymentService.java | status(4+1值) | 支付回调/超时/金额校验 |
| 4 | PaymentCompensationService.java | status(5值) | 超时补偿流程 |
| 5 | ProposalService.java | status(8值) | 投保→核保→支付→出单全流程 |
| 6 | ProductService.java | versionStatus(5值) | 审批流转状态机 |
| 7 | CommissionService.java | settleStatus(4值) | 佣金结算流程 |
| 8 | ChannelController/Service | status(6值) | 渠道审批流程 |
| 9 | PolicyService.java | status(3值)+terminationReason | 保单生命周期 |
| 10 | UnderwritingService.java | proposal status | 核保流转 |

每个 Service 检查项：
- `setStatus(N)` 的 N 值是否在 dict 中有对应
- 状态流转是否完整（有没有“能进不能出”的死状态）
- switch/if 分支是否覆盖所有 dict 值
- 注释含义是否与 dict 一致

**第 2 层：对照业务流程框架**

- [ ] 读取 `docs/4-reference/Covex业务流程框架.md`
- [ ] 提取每个域的预期状态机（产品/承保/理赔/保单/渠道/佣金）
- [ ] 对比第 1 层审计结果，识别：
  - 业务流程框架要求的流转未实现
  - 代码自行发明的流转（框架未定义）

**第 3 层：输出分类修复清单**

将审计结果整理为 4 类：
- **A类**：代码与 DB + 业务框架一致 → 无需修改
- **B类**：代码正确，DB 缺失/错误 → 补 ins_dict（成本最低）
- **C类**：DB 正确，代码有 Bug → 改代码
- **D类**：业务框架要求的流转未实现 → 新增开发任务

输出文件：审计结果写入本报告“三、修复结果”章节

- [ ] B类/C类修改后，必须补充 API 测试验证修改正确性
- [ ] D类任务如超出本次治理范围，记录到报告中后续处理

**已知 D类发现（来自需求规格状态机对比）**：
- [ ] 保全申请状态机（9.4）：需求规格定义 5 状态，但无字典/无 Service/无前端 → 记录报告
- [ ] 续期账单状态机（9.5）：需求规格定义 4 状态，但无字典/无 Service/无前端 → 记录报告
- [ ] 理赔状态机（9.3）：DB 多出 5 个状态(7-9未使用)，需求规格未同步补充计划新增的 10/11 → 更新需求规格

### 1.6 后端 DB 修复（基于 1.5 审计结果）

**P0 修复（决策 1-3 已确认）：**
- [ ] payment_status=4：UPDATE ins_dict 将 dict_name 从“支付失败”改为“挂起”
- [ ] payment_status=5：INSERT ins_dict 新增 dict_code=5, dict_name=“超时挂起”
- [ ] claim_status=9：DELETE FROM ins_dict WHERE dict_type='claim_status' AND dict_code='9'（合并到 3）
- [ ] 以上修改后补充 API 测试

**P1 补充缺失字典类型（13 个 + 9 个保全/续期 = 22 个字段）：**
- [ ] INSERT ins_dict: claim_review_type(1自动审核,3调查审核), claim_review_result(1通过,2拒绝,4需调查), commission_type(1首期,2续期)
- [ ] INSERT ins_dict: reporter_relation(1本人,2家属,3代理人,4其他), template_source(1手动创建,2系统模板,3公司模板), scope_type(1全部,2本部门,3自定义)
- [ ] INSERT ins_dict: bank_account_type(1储蓄卡,2信用卡,3对公账户), customer_type(1个人,2团体), customer_source(1线下,2线上,3转介绍,4渠道代理)
- [ ] INSERT ins_dict: education_level(1高中及以下,2大专,3本科,4硕士,5博士), marital_status(1未婚,2已婚,3离异,4丧偶), occupation_risk_level(1-6类)
- [ ] INSERT ins_dict: 保全状态(1待审核,2审核中,3已生效,4已通过,5已驳回), 续期账单状态(1待缴,2已缴,3逾期,4已豁免)
- [ ] gender 字段改用已有 gender_linked 字典
- [ ] 更新 `项目枚举值.md` 同步新增的字典类型
- [ ] 补充 API 测试验证新字典可查询

---

## Task 2：确保 DB→Redis→API 链路通畅

### 2.1 后端 dict 缓存机制
- [ ] 确认是否存在 `DictCacheService` 或类似组件
- [ ] 如不存在：创建启动时全量加载 dict → Redis 的逻辑
- [ ] 如已存在：确认缓存 key 格式、TTL、刷新机制

### 2.2 缓存刷新机制
- [ ] dict 数据变更时（INSERT/UPDATE/DELETE），清除对应 Redis key
- [ ] 确认前端重新请求 dict API 时能拿到最新数据

### 2.3 端到端验证
- [ ] 修改 DB 一条 dict 记录
- [ ] 确认 Redis 缓存同步更新
- [ ] 确认 API 返回新值
- [ ] 恢复原始数据

---

## Task 3：修复 P0 硬编码 Bug（3 处）

> **前置条件**：Task 1.5/1.6 后端审计+修复已完成。禁止跳过审计直接修改前端。

### 3.1 Dashboard.vue — claim_status 严重错位
- **文件**：`Covex-ui/src/views/dashboard/Dashboard.vue` L210-220
- **问题**：硬编码 9 项映射，7 项语义错位 + 缺失 code 10/11
- [ ] 删除硬编码 Record
- [ ] 改用 dictStore
- [ ] 浏览器验证 Dashboard 理赔统计状态标签

### 3.2 ClaimList.vue — claim_status 同样错位
- **文件**：`Covex-ui/src/views/claim/ClaimList.vue` L90-100
- **问题**：硬编码与 Dashboard.vue 完全相同
- [ ] 删除硬编码 statusOptions
- [ ] 改用 dictStore
- [ ] 浏览器验证理赔列表状态标签

### 3.3 ProductList.vue — versionStatus 编码偏移
- **文件**：`Covex-ui/src/views/product/ProductList.vue` L144-175
- **问题**：0-based 编码 vs DB 1-based，5 个状态码中 4 个偏移
- **修复**：
  - versionStatusMap 改用 `dictStore.getDictLabel('version_status', String(status))`
  - 按钮 v-if 条件从 `versionStatus === 0` 改为 `versionStatus === 1`（草稿）
- [ ] 删除硬编码 versionStatusMap
- [ ] 改用 dictStore
- [ ] 修正编辑/发布/冻结按钮的 v-if 条件
- [ ] 浏览器验证产品列表状态标签 + 按钮可见性

---

## Task 4：治理 P1 硬编码（18 处）+ 写入 AGENTS.md

### 4.1 Record 映射表字典化（4 处，除 P0 外）
- [ ] Dashboard.vue L196: proposal_status → dictStore
- [ ] ProductDetail.vue L369: product_type → dictStore
- [ ] ProductDetail.vue L373: version_status → dictStore
- [ ] ProductList.vue L144: product_type → dictStore

### 4.2 statusOptions 数组字典化（3 处，除 P0 外）
- [ ] ProposalList.vue L98: proposal_status → dictStore
- [ ] ProposalDetail.vue L95: proposal_status → dictStore
- [ ] PolicyList.vue L88: policy_status → dictStore

### 4.3 el-option 模板字典化（8 处）
- [ ] ProductDetail.vue L19-25: product_type → el-option v-for dictStore
- [ ] ProductDetail.vue L234-235: product_type 重复硬编码 → 复用
- [ ] CustomerDetail.vue L183-185: address_type → dictStore
- [ ] CustomerDetail.vue L229-231: account_usage_type → dictStore
- [ ] UnderwritingDetail.vue L75-79: underwriting_result → dictStore
- [ ] PaymentManagement.vue L104-107: pay_channel → dictStore
- [ ] ChannelList.vue L12+L77-80: channel_status → 全量字典选项
- [ ] ChannelDetail.vue L141-145: channel_status → 全量字典选项

### 4.4 A 类硬编码字段改 dictStore（6 个字段，含理赔模块，决策 6 纳入本次）
- [ ] CustomerDetail.vue / CustomerList.vue: gender 硬编码三元运算 → `dictStore.getDictLabel('gender_linked', ...)`
- [ ] CustomerCreate.vue / CustomerDetail.vue: customerType 硬编码 el-select/el-display → dictStore
- [ ] CommissionManagement.vue: commissionType 硬编码 el-select/el-tag → `dictStore.getDictLabel('commission_type', ...)`
- [ ] ClaimDetail.vue: reviewType 硬编码 el-select/el-display → `dictStore.getDictLabel('claim_review_type', ...)`
- [ ] ClaimDetail.vue: reviewResult 硬编码 el-tag → `dictStore.getDictLabel('claim_review_result', ...)`
- [ ] DataScopeManagement.vue: scopeType → `dictStore.getDictLabel('scope_type', ...)`

在 AGENTS.md 编码前检查清单中新增：

### 4.5 AGENTS.md 新增强制规范

- [ ] Task 1.5-1.6：后端审计+DB修复完成（22个字典类型），API 测试通过
- [ ] Task 2：三方数据链路通畅（DB→Redis→API→dictStore）
- [ ] Task 3：3 个 P0 Bug 修复（Dashboard + ClaimList claim_status + ProductList versionStatus）
- [ ] Task 4：18 处 P1 硬编码清除 + 6 个 A 类字段 dictStore 化，AGENTS.md 规范生效
- [ ] Task 5：客户信息录入完善（5 个新下拉框 + 详情展示 + 存量兼容）
- [ ] 全局搜索确认：前端代码中无 `Record<number, {label` 硬编码状态映射
- [ ] `项目枚举值.md` 已同步新增的字典类型
- [ ] **后端代码任何改动必须补充对应的 API 测试**（写入测试计划 TS）
