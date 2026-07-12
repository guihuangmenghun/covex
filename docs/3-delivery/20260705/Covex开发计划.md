# Covex 开发计划

> 14 个 Story，按依赖顺序排列。**每完成一项立即将 `[ ]` 改为 `[x]`，不得批量补标。**
> 配套测试：`Covex测试计划.md`（由独立测试 Agent 执行）

---

## S1 - 项目骨架搭建 + 基础设施

> 依赖：无 | 验收：Spring Boot 启动 + Nacos 注册 + Swagger UI 可访问 + 日志含 traceId

- [x] 使用 Spring Initializr 创建 Maven 多模块项目：covex-parent / covex-common / covex-api / covex-service / covex-web
- [x] 集成 Spring Cloud 2024.0.x（Gateway + LoadBalancer），Nacos 独立客户端接入
- [x] 配置 MySQL 8.4.9 + Druid 1.2.25 + MyBatis-Plus（多租户 + 逻辑删除 + 自动填充）
- [x] 配置 Redis 5.0.14 + Redisson 分布式锁
- [x] 集成 springdoc-openapi 2.8.6 + Swagger UI
- [x] 排除 Logback，集成 Log4j2（异步 + JSON + traceId）
- [x] 集成 Micrometer Tracing 全链路追踪
- [x] 创建 BaseEntity / BaseController / Result\<T\> 通用基类
- [x] 创建 GlobalExceptionHandler + BizException
- [x] 配置 RocketMQ 5.3.3 Producer/Consumer
- [x] 集成 Sentinel 1.8.8（规则存 Nacos）
- [x] 集成 LiteFlow + Nacos 规则源
- [x] 集成 Aviator + 注册 rateLookup 函数
- [x] 配置 MapStruct 1.6.3
- [x] 引入 Apache Commons + Guava
- [x] 配置 Spring AI Alibaba 1.1.2.0
- [x] 配置 Spring Cloud Gateway 路由规则（服务路由 + 全局过滤器 + Sentinel 限流集成）
- [x] 配置定时任务基础设施（@EnableScheduling + 自定义线程池 + 任务异常全局处理）

---

## S2 - 数据字典服务（ins_dict）

> 依赖：S1 | 验收：Swagger UI 查询 product_type 返回 7 个险种

- [x] 创建 ins_dict 表 DDL（含 parent_code 层级）
- [x] 创建 DictEntity + DictMapper + DictService + DictController
- [x] 实现 CRUD API：GET/POST/PUT/DELETE /api/dict
- [x] 实现按 dict_type 批量查询 + Redis 缓存
- [x] 实现层级字典查询（parent_code 递归）
- [x] 导入 39 组预置字典数据
- [x] Swagger UI 文档自动生成

---

## S3 - 用户角色权限服务（6张表）

> 依赖：S2 | 验收：核保员登录后只看到自己区域的投保单

- [x] 创建 6 张表 DDL：ins_user / ins_role / ins_permission / ins_user_role / ins_role_permission / ins_data_scope
- [x] 创建 Entity/Mapper/Service/Controller 全套 CRUD
- [x] 实现用户管理 API：注册/登录/查询/启停用
- [x] 实现角色管理 API：创建/分配权限/查询权限
- [x] 实现数据权限 API：设置数据范围
- [x] 实现 JWT Token 认证 + Spring Security 拦截
- [x] 实现 @RequiresPermission 注解 + AOP 校验
- [x] 实现 DataPermissionInterceptor 数据权限
- [x] 预置系统角色：管理员/产品经理/核保员/理赔员/渠道管理员

---

## S4 - 客户主表 + 投保人/被保人扩展（3张表）

> 依赖：S3 | 验收：客户A作为投保人→自动创建applicant，再作为被保人→自动创建insured

- [x] 创建 3 张表 DDL：ins_customer / ins_customer_applicant / ins_customer_insured
- [x] 创建 Entity/Mapper/Service/Controller 全套 CRUD
- [x] 实现客户录入 API（证件号查重 + 自动创建/引用）
- [x] 实现证件号格式校验（身份证/护照/军官证）
- [x] 实现证件号 + 手机号 + 银行账号 AES 加密存储
- [x] 实现角色扩展自动创建（首次投保人→applicant，首次被保人→insured）
- [x] 实现健康档案 JSON 读写（medical_history/family_history/current_medications）
- [x] 实现客户查询 API（证件号/姓名/手机号）

---

## S5 - 客户银行账户 + 联系地址（2张表）

> 依赖：S4 | 验收：删除有代扣协议的账户被拦截

- [x] 创建 2 张表 DDL：ins_customer_bank_account / ins_customer_address
- [x] 实现银行账户 CRUD + 设置默认 + 户名校验
- [x] 实现代扣协议管理
- [x] 实现联系地址 CRUD + 按类型设默认
- [x] 实现账户删除保护

---

## S6 - 渠道商管理 + 产品授权（3张表）

> 依赖：S3 | 验收：渠道商端只看到被授权的3个产品

- [x] 创建 3 张表 DDL：ins_channel / ins_channel_product / ins_channel_user
- [x] 实现渠道商 CRUD + 状态流转
- [x] 实现资质审核 API
- [x] 实现产品授权 + 佣金比例设置
- [x] 实现渠道商可售产品查询
- [x] 实现渠道商账号管理

---

## S7 - 佣金计算与结算

> 依赖：S6 | 验收：出单5笔→月底生成结算单→财务确认

- [x] 创建 ins_commission 表 DDL
- [x] 实现佣金自动计算（RocketMQ 触发）
- [x] 实现佣金计算逻辑（区分首年/续期）
- [x] 实现月度佣金结算定时任务
- [x] 实现佣金确认/驳回 API
- [x] 实现佣金查询 API

---

## S8 - 产品配置域（11张表）

> 依赖：S2 | 验收：创建重疾险→4个责任→关联费率表→Redis可查缓存

- [x] 创建 11 张表 DDL
- [x] 实现产品 CRUD + 版本管理 + version_status 状态机
- [x] 实现保障责任 CRUD + 责任-缴费关联
- [x] 实现缴费计划 CRUD
- [x] 实现规则引用管理（liteflow/aviator/java）
- [x] 实现主附险关联 + 条款文档管理
- [x] 实现费率表管理 + Redis 缓存加载
- [x] 实现产品变更日志（AOP）
- [x] 实现产品查询 API

---

## S9 - 投保单 + 核保 + LiteFlow 链

> 依赖：S4, S6, S8 | 验收：投保→校验通过→核保通过（标准体）→待支付

- [x] 创建 2 张表 DDL：ins_proposal / ins_underwriting_record
- [x] 实现投保单 CRUD + 产品快照 JSON
- [x] 实现投保单状态机
- [x] 实现 LiteFlow validate 链（5个校验组件）
- [x] 实现 LiteFlow underwrite 链
- [x] 实现核保记录管理 + 人工核保工作台
- [x] 实现核保结论处理（6种分支）
- [x] 实现累计风险保额查询降级（查询失败→标记风控标记→转人工核实）
- [x] 实现投保单查询 API

---

## S10 - 保费计算 + 支付 + 出单

> 依赖：S9 | 验收：核保通过→保费3680→支付→保单生成

- [x] 创建表 DDL：ins_payment / ins_policy / ins_policy_coverage / ins_policy_premium
- [x] 实现 Aviator 保费计算引擎
- [x] 实现保费计算 API
- [x] 实现支付 API + Mock 支付通道
- [x] 实现支付回调处理（幂等）
- [x] 实现支付回调金额校验（金额不匹配→status=4 挂起→人工介入）
- [x] 实现支付回调丢失补偿（24h 定时查询支付通道→确认已支付→触发补单）
- [x] 实现支付超时自动撤销
- [x] 实现出单链（LiteFlow issue）
- [x] 实现异步通知（RocketMQ）
- [x] 实现保单查询 API

---

## S11 - 续期账单 + 缴费提醒 `⏸️ 待开发`

> 依赖：S10 | 验收：T-30天→生成账单→提醒→扣款成功 | **状态：暂缓开发**

- [ ] 创建 ins_renewal_bill 表 DDL
- [ ] 实现续期账单自动生成
- [ ] 实现缴费提醒发送
- [ ] 实现代扣处理
- [ ] 实现手动缴费 API
- [ ] 实现宽限期处理
- [ ] 实现账单查询 API

---

## S12 - 保全变更 + 保单借款 + 退保 `⏸️ 待开发`

> 依赖：S10 | 验收：申请退保→计算退保金15680→保单终止 | **状态：暂缓开发**

- [ ] 创建 3 张表 DDL：ins_endorsement / ins_endorsement_change / ins_policy_loan
- [ ] 实现保全申请 CRUD（7种类型）
- [ ] 实现保全状态机
- [ ] 实现信息变更处理
- [ ] 实现退保金计算（犹豫期 + 正常）
- [ ] 实现退保审批 + 退款 + 保单终止
- [ ] 实现保单借款 API（≤80%现金价值）
- [ ] 实现复效流程
- [ ] 实现保全查询 API

---

## S13 - 理赔报案 + 审核 + LiteFlow 理赔链

> 依赖：S10 | 验收：报案→校验通过→分配理赔员→审核赔付8500

- [x] 创建 3 张表 DDL：ins_claim / ins_claim_document / ins_claim_review
- [x] 实现理赔报案 API + 自动校验
- [x] 实现 LiteFlow claim 链前置校验
- [x] 实现理赔员自动分配
- [x] 实现理赔审核工作台 API
- [x] 实现自动赔付计算（Aviator）
- [x] 实现调查流程
- [x] 实现理赔查询 API

---

## S14 - 理赔赔付支付 + 结案

> 依赖：S13 | 验收：赔付8500成功→累计更新→结案→通知

- [x] 创建 ins_claim_payment 表 DDL
- [x] 实现赔付支付流程
- [x] 实现支付回调 + 结案
- [x] 实现累计赔付更新
- [x] 实现保额终止判断
- [x] 实现理赔争议处理
- [x] 实现赔付通知（MQ 异步）

---

## S15 - 操作人字段补充

> 依赖：S1-S14 | 验收：7 张表均有 operator 字段，人工操作填账号，自动操作填 SYSTEM

- [ ] 7 张表执行 ALTER TABLE ADD COLUMN operator VARCHAR(50)：ins_proposal / ins_payment / ins_commission / ins_endorsement / ins_renewal_bill / ins_policy_loan / ins_claim_payment
- [ ] 7 个 Entity 类新增 operator 字段
- [ ] 7 个 Service 层适配：创建/更新记录时设置 operator 值
- [ ] 自动操作场景统一填 SYSTEM：支付回调（PaymentService）、定时任务生成账单、MQ 消费触发佣金
- [ ] 人工操作场景填操作员账号：投保录入（ProposalService）、保全执行（EndorsementService）、人工退款、人工赔付
