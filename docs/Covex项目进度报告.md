# Covex 保险核心平台 — 项目进度报告

> 报告日期：2026-07-03
> 分支：qoder
> 最后一次提交：a9fa53b

---

## 一、项目概况

Covex 是一个多险种保险核心平台，覆盖寿险、车险、财产险、乘务险四大险种，采用 Spring Cloud 微服务架构，集成 LiteFlow 流程编排 + Aviator 表达式引擎 + Redis 缓存 + RocketMQ 消息队列。

| 指标 | 数值 |
|---|---|
| 数据库表数 | **36 张** |
| Java 文件数 | **166 个** |
| Java 代码行数 | **9,739 行** |
| SQL 迁移脚本 | **22 个** |
| API 端点数 | **133 个** |
| LiteFlow 组件 | **17 个** |
| Git 提交数 | **19 个** |
| 项目总文件数 | **242 个** |

---

## 二、开发进度总览

### 已完成 Story（12/14）

| Story | 标题 | 表数 | 文件数 | 状态 | Commit |
|---|---|---|---|---|---|
| S1 | 项目骨架 + 基础设施 | 0 | 13 | ✅ | 564c418 |
| S2 | 数据字典服务 | 1 | 9 | ✅ | 2293e81 |
| S3 | 用户角色权限服务 | 6 | 29 | ✅ | 08e4e5f |
| S4 | 客户主表 + 投保人/被保人扩展 | 3 | — | ✅ | 48b4f9c |
| S5 | 客户银行账户 + 联系地址 | 2 | 19 | ✅ | ↑ |
| S6 | 渠道商管理 + 产品授权 | 3 | — | ✅ | eecb65f |
| S7 | 佣金计算与结算 | 1 | 17 | ✅ | ↑ |
| S8 | 产品配置域 | 10 | 38 | ✅ | 908c5d3 |
| S9 | 投保单 + 核保 + LiteFlow 链 | 2 | — | ✅ | 0b21b6b |
| S10 | 保费计算 + 支付 + 出单 | 4 | 43 | ✅ | ↑ |
| S13 | 理赔报案 + 审核 + 理赔链 | 3 | — | ✅ | e4e533d |
| S14 | 理赔赔付支付 + 结案 | 1 | 29 | ✅ | ↑ |
| **合计** | | **34** | **~200** | **100 项[x]** | |

### 待开发 Story（2/14）

| Story | 标题 | 表数 | 状态 |
|---|---|---|---|
| S11 | 续期账单 + 缴费提醒 | 1 | ⏸️ 暂缓 |
| S12 | 保全变更 + 保单借款 + 退保 | 3+3 | ⏸️ 暂缓 |

---

## 三、技术架构

### 技术栈

| 组件 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 3.4.13 | 基础框架 |
| Spring Cloud | 2024.0.1 | 微服务基础设施 |
| Nacos | 2.4.3（独立客户端） | 注册中心 + 配置中心 |
| RocketMQ | 5.3.3 | 异步消息 |
| Redis + Redisson | 5.0.14 | 缓存 + 分布式锁 |
| MySQL | 8.4.9 | 持久化存储 |
| LiteFlow | 2.12.4 | 流程编排（17 组件） |
| Aviator | 5.4.3 | 表达式计算（保费/赔付） |
| Sentinel | 1.8.8 | 限流熔断 |
| MyBatis-Plus | 3.5.9 | ORM + 多租户 + 逻辑删除 |
| Druid | 1.2.25 | 连接池 + SQL 监控 |
| springdoc-openapi | 2.8.6 | API 文档（OpenAPI 3 + Swagger UI） |
| Log4j2 | — | 异步日志 + JSON 结构化 |
| Micrometer Tracing | — | 全链路追踪 |
| jjwt | 0.12.6 | JWT 认证 |
| MapStruct | 1.6.3 | DTO 映射 |
| Apache Commons + Guava | 最新 | 工具库 |

### 模块结构

```
covex-parent (pom)
├── covex-common    — 通用基类/工具/异常/安全
├── covex-api       — DTO/VO/契约
├── covex-service   — Entity/Mapper/Service/LiteFlow组件
└── covex-web       — Controller/配置/启动类
```

### LiteFlow 流程链

| 链名称 | 组件数 | 组件列表 |
|---|---|---|
| validateChain | 5 | Age → Id → Amount → Rider → Channel |
| underwriteChain | 3 | Health ∥ Occupation ∥ CumulativeAmount |
| issueChain | 4 | CreatePolicy → CreateCoverage → CreatePremium → Notify |
| claimValidateChain | 4 | Policy → Coverage → WaitingPeriod → CoverageMatch |
| claimCalculationChain | 1 | ClaimCalculation (Aviator) |

---

## 四、数据库表清单（36 张）

### 基础服务层（7 张）
| 表名 | 说明 |
|---|---|
| ins_dict | 数据字典（169 条预置数据，40 类型） |
| ins_user | 系统用户 |
| ins_role | 角色 |
| ins_permission | 权限 |
| ins_user_role | 用户-角色关联 |
| ins_role_permission | 角色-权限关联 |
| ins_data_scope | 数据权限范围 |

### 客户域（5 张）
| 表名 | 说明 |
|---|---|
| ins_customer | 客户主表（AES 加密证件号/手机号） |
| ins_customer_applicant | 投保人扩展属性 |
| ins_customer_insured | 被保人扩展 + 健康档案（JSON） |
| ins_customer_bank_account | 银行账户（AES 加密账号） |
| ins_customer_address | 联系地址 |

### 渠道域（4 张）
| 表名 | 说明 |
|---|---|
| ins_channel | 渠道商（状态机：待审核→已签约→已暂停→已终止） |
| ins_channel_product | 渠道-产品授权 + 佣金比例 |
| ins_channel_user | 渠道商账号 |
| ins_commission | 佣金记录（幂等计算 + 月度结算） |

### 产品配置域（10 张）
| 表名 | 说明 |
|---|---|
| ins_product | 产品主表（版本状态机 + capabilities JSON） |
| ins_product_coverage | 保障定义（coverage_detail JSON） |
| ins_product_premium | 缴费规则（premium_detail JSON） |
| ins_coverage_premium_rel | 责任-缴费关联 |
| ins_product_rule | 规则引用（liteflow/aviator/java） |
| ins_product_rider_rel | 主附险关联 |
| ins_product_document | 条款文档 |
| ins_product_changelog | 变更日志（AOP 自动记录） |
| ins_rate_table | 费率表元数据（table_schema JSON） |
| ins_rate_table_row | 费率行数据（Redis Hash 缓存） |

### 承保域（6 张）
| 表名 | 说明 |
|---|---|
| ins_proposal | 投保单（8 状态 + 产品快照 JSON） |
| ins_underwriting_record | 核保记录（6 种结论） |
| ins_payment | 支付记录（幂等 + 金额校验） |
| ins_policy | 保单主表（7 种终止原因） |
| ins_policy_coverage | 保单险种明细 |
| ins_policy_premium | 保单缴费计划 |

### 理赔域（4 张）
| 表名 | 说明 |
|---|---|
| ins_claim | 理赔案件（6 状态 + 调查/争议流） |
| ins_claim_document | 理赔材料 |
| ins_claim_review | 理赔审核记录（3 种审核类型） |
| ins_claim_payment | 赔付记录（累计赔付 + 保额终止） |

---

## 五、API 端点汇总（133 个）

| 模块 | 路径前缀 | 端点数 | 关键功能 |
|---|---|---|---|
| 健康检查 | /api/health | 1 | 应用状态 |
| 数据字典 | /api/dict | 6 | CRUD + 层级查询 + 缓存清理 |
| 用户管理 | /api/user | 9 | 注册/登录/角色分配/权限查询 |
| 角色管理 | /api/role | 6 | CRUD + 权限分配 |
| 权限管理 | /api/permission | 3 | CRUD + 模块分组 |
| 数据权限 | /api/data-scope | 2 | 设置/查询数据范围 |
| 客户管理 | /api/customer | 7 | CRUD + 角色扩展 + 健康档案 |
| 银行账户 | /api/customer/{id}/bank-account | 5 | CRUD + 删除保护 + 默认设置 |
| 联系地址 | /api/customer/{id}/address | 5 | CRUD + 默认设置 |
| 渠道商 | /api/channel | 7 | CRUD + 状态流转 + 产品授权 |
| 渠道账号 | /api/channel/{id}/user | 4 | CRUD + 状态切换 |
| 佣金 | /api/commission | 4 | 查询/结算/汇总/确认 |
| 产品管理 | /api/product | 8 | CRUD + 克隆/发布/冻结/变更历史 |
| 产品责任 | /api/product/{id}/coverage | 6 | CRUD + 关联缴费 |
| 产品缴费 | /api/product/{id}/premium | 4 | CRUD |
| 产品规则 | /api/product/{id}/rule | 4 | CRUD |
| 主附险 | /api/product/{id}/rider | 3 | 创建/查询/删除 |
| 条款文档 | /api/product/{id}/document | 4 | CRUD |
| 费率表 | /api/rate-table | 5 | 创建/导入/查询/Redis加载/清缓存 |
| 投保单 | /api/proposal | 4 | 创建/查询/提交 |
| 核保 | /api/underwriting | 3 | 自动/人工核保 + 记录查询 |
| 支付 | /api/payment | 5 | 创建/回调/查询/超时扫描/保费计算 |
| 保单 | /api/policy | 3 | 出单/详情/列表 |
| 理赔 | /api/claim | 8 | 报案/分配/审核/计算/调查/争议 |
| 理赔材料 | /api/claim/{id}/document | 2 | 上传/查询 |
| 理赔支付 | /api/claim/{id}/payment | 4 | 赔付/回调/结案/争议 |

---

## 六、质量保障

### 已完成的质量验证
- 每个 Story 完成后执行端到端 API 测试（共 100+ 测试用例）
- 两次独立 Agent 覆盖审核（14/14 条修复建议全部闭合）
- 所有编译零错误（mvn clean install -DskipTests BUILD SUCCESS）
- 应用启动验证（端口 8080，8 秒内启动）

### 安全特性
- JWT 认证（jjwt 0.12.6，24h 过期）
- BCrypt 密码加密
- AES 敏感数据加密（证件号/手机号/银行账号）
- Spring Security 权限拦截（登录/健康/Swagger 公开，其余需 JWT）
- 数据权限隔离（DataPermissionInterceptor）

---

## 七、待办事项

### 待开发（暂缓）
| 项目 | 说明 | 表数 |
|---|---|---|
| S11 续期账单 | 账单自动生成 + 缴费提醒 + 代扣 + 宽限期 | 1 |
| S12 保全变更 | 信息变更/受益人变更/退保/借款/复效 | 6 |

### 待补充（低优先级）
| 项目 | 说明 |
|---|---|
| 操作人字段 | 业务表加 operator 字段（开发后统一补充） |
| RocketMQ NameServer | NameServer 未启动，当前 MQ 通知为 Mock |
| Spring AI Alibaba | DashScope API Key 已配置但未集成业务逻辑 |
| 前端 | 无前端代码，当前纯后端 API |
| 生产部署 | Docker/K8s 部署方案 |

---

## 八、Git 提交历史

```
a9fa53b  docs: S13+S14[x]
e4e533d  feat(S13+S14): Claims domain (4 tables, 22 files)
e23b170  docs: S9+S10[x]
0b21b6b  feat(S9+S10): Underwriting domain (6 tables, 32 files)
bfd3fe9  docs: S8[x]
908c5d3  feat(S8): Product config domain (10 tables, 36 files)
9eb87a5  docs: S6+S7[x]
eecb65f  feat(S6+S7): Channel domain (4 tables, 17 files)
161baf2  docs: S4+S5[x]
48b4f9c  feat(S4+S5): Customer domain (5 tables, 19 files)
21152bb  docs: S1/S2/S3[x]
613a9b2  fix: 业务流程框架实体枚举补充
08e4e5f  feat(S3): User role permission (6 tables, 29 files)
2293e81  feat(S2): Data dictionary (169 entries)
564c418  feat(S1): Project skeleton verified
50bf624  feat(S1): Maven skeleton (18 components)
7a7c777  fix: 覆盖审核14项修复
9b3ceaf  fix: 交叉验证修复
01a888d  init: 项目设计文档
```
