# Covex — 保险核心平台

> **开源保险核心平台**，覆盖产品配置 → 投保 → 核保 → 出单 → 理赔 → 佣金 → 保全全链路。
> 基于 Spring Boot 3 + Vue 3 微服务架构，内置可配置规则引擎。

[![License](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)

---

## 项目简介

Covex 是一个面向保险公司的核心业务系统，旨在提供完整的保险业务处理能力。项目涵盖寿险、车险、财产险、乘务险四大险种，支持从产品创建到理赔结算的端到端业务流程。

**核心特色**：
- **可配置规则引擎**：独立微服务，支持核保/费率/理赔/佣金等多规则域，热更新生效
- **产品模板工厂**：6 个预置模板，产品经理 3 步创建完整产品（选模板 → 填参数 → 生成）
- **13 角色权限体系**：覆盖保险公司完整组织架构（产品→精算→销售→核保→保全→理赔→财务→合规）
- **AI 辅助开发**：全程使用 [Qoder](https://qoder.cn) AI IDE 开发，主力模型 Qwen3.7 Max / Qwen3.7 Plus

---

## 技术栈

### 后端

| 组件 | 版本 | 用途 |
|---|---|---|
| Java | 17 | 运行时 |
| Spring Boot | 3.4.13 | Web 应用基座 |
| Spring Cloud | 2024.0.x | 微服务治理 |
| MyBatis-Plus | — | ORM 持久层 |
| Nacos | 2.4.3 | 服务注册 + 配置中心 |
| RocketMQ | 5.3.3 | 消息队列（异步解耦/延迟消息） |
| Redis + Redisson | 5.0.14 | 缓存 + 分布式锁 |
| LiteFlow | — | 流程编排引擎 |
| Aviator | — | 表达式计算引擎 |
| Log4j2 | — | 日志框架 |
| springdoc-openapi | — | API 文档（Swagger UI） |

### 前端

| 组件 | 版本 | 用途 |
|---|---|---|
| Vue | 3.5 | 前端框架 |
| Element Plus | 2.9 | UI 组件库 |
| Vite | 6.3 | 构建工具 |
| TypeScript | 5.8 | 类型安全 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.5 | 路由管理 |
| Axios | 1.9 | HTTP 客户端 |

### 数据库

| 组件 | 版本 | 用途 |
|---|---|---|
| MySQL | 8.4 | 主数据库（40 张表，含规则引擎 3 张待开发） |

---

## 项目结构

```
covex-parent/
├── covex-common/          # 通用模块（BaseEntity, Result, JwtUtil, Redis 配置）
├── covex-api/             # API DTO 定义（跨模块共享）
├── covex-service/         # 业务 Service 层（Entity, Mapper, LiteFlow 组件）
├── covex-web/             # 业务 Controller（8080 端口）
├── Covex-ui/              # 前端 Vue 3 项目（3000 端口）
└── docs/                  # 项目文档（方案/规格/计划）
```

---

## 业务模块

### 已实现模块

| 模块 | 说明 | 状态 |
|---|---|:---:|
| **产品配置** | 产品 CRUD、保障责任、缴费计划、规则引用、费率表管理、变更历史 | ✅ |
| **客户管理** | 客户 CRUD、投保人/被保人/受益人角色、健康告知 | ✅ |
| **渠道管理** | 渠道商 CRUD、渠道产品授权、佣金比例配置、渠道审批 | ✅ |
| **投保单** | 投保单创建、保障选择、缴费计划选择、提交流转 | ✅ |
| **核保** | 核保列表、核保详情、核保决策（年龄/健康/职业/累计保额） | ✅ |
| **支付** | 支付记录创建、回调处理、缴费计划生成 | ✅ |
| **保单** | 保单列表、保单详情、保单生效/到期管理 | ✅ |
| **理赔** | 理赔报案、理赔审核、理赔计算、赔付通知 | ✅ |
| **佣金** | 佣金计算、月度结算、渠道佣金汇总 | ✅ |
| **数据字典** | 字典类型/值管理、Redis 缓存、全局下拉数据源 | ✅ |
| **用户/角色/权限** | 13 角色 RBAC 权限体系、权限编码统一 edit、角色模板预设 | ✅ |
| **产品模板工厂** | 产品库三层架构 + 6 个预置模板 + 3 步向导创建 | ✅ |
| **MQ Consumer** | 出单→佣金计算、理赔→赔付通知 | ✅ |

### 规划中模块（详见 [补充计划](docs/Covex补充计划20260709.md)）

| 模块 | 说明 | 计划期次 | 状态 |
|---|---|---|:---:|
| **规则引擎服务** | 独立微服务（8081），6 域 42 条规则，版本追溯，热更新 | P2.5 | 🔨 待开发 |

---

## 规则引擎

Covex 内置独立的规则引擎微服务（`covex-rule-engine:8081`），支持：

- **6 个规则域**：投保校验 / 核保决策 / 费率计算 / 理赔审核 / 保全 / 佣金
- **三级层级**：COMMON → PRODUCT_TYPE → PRODUCT（产品级覆盖公共级）
- **统一 API**：`POST /api/rule/evaluate` — 传入保单 JSON，返回结论 + 违反规则提示
- **版本追溯**：version + requirement_no 双定位，逻辑删除保留全量历史，发布快照支持版本对比
- **热更新**：Redis 缓存 + Pub/Sub 通知，规则修改后 < 1 秒生效

---

## 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- MySQL 8.4+
- Redis 5.0+
- Node.js 18+（前端）

### 启动后端

```bash
# 1. 启动 MySQL 和 Redis

# 2. 编译项目
mvn clean package -DskipTests

# 3. 启动业务服务
java -jar covex-web/target/covex-web.jar    # 端口 8080
```

### 启动前端

```bash
cd Covex-ui
npm install
npm run dev                                    # 端口 3000
```

### 访问

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html

---

## AI 辅助开发

本项目全程使用 **[Qoder CN](https://qoder.cn)** AI IDE 进行开发，主要使用的 AI 模型：

- **Qwen3.7 Max** — 复杂架构设计、代码生成、需求分析
- **Qwen3.7 Plus** — 日常编码、文档编写、Bug 修复

Qoder CN 是一款基于 VS Code 架构的独立桌面 IDE，具备完整的 AI Agent 能力，支持 Skill 系统、子 Agent、MCP 工具等。本项目的所有方案文档、代码实现、E2E 测试均由 AI Agent 协同完成。

---

## 文档索引

| 文档 | 路径 | 说明 |
|---|---|---|
| 技术栈总表 | `docs/Covex技术栈总表.md` | 全部组件版本与用途 |
| 数据模型（产品配置域） | `docs/Covex数据模型-产品配置域.md` | 11 张产品配置表 |
| 数据模型（运营域） | `docs/Covex数据模型-运营域.md` | 29 张运营表 |
| 运营域需求规格 | `docs/Covex运营域需求规格.md` | 用户故事 + 状态机 + 端到端流程 |
| 业务流程框架 | `docs/Covex业务流程框架.md` | 6 阶段业务全景 + 13 个角色 |
| 路由权限矩阵 | `docs/Covex路由权限矩阵.md` | 13 角色 × 42 路由权限矩阵 |
| 规则引擎服务方案 | `docs/Covex规则引擎服务方案.md` | 独立微服务架构 + 版本追溯 |
| 规则引擎需求规格 | `docs/Covex规则引擎需求规格.md` | 6 域 42 条规则 + 统一 API |
| 产品模板工厂方案 | `docs/Covex产品模板工厂方案.md` | 产品库三层 + 6 预置模板 |
| 补充计划 | `docs/Covex补充计划20260709.md` | P0~P3 补充任务 + 规则引擎 |
| ER 图 | `docs/Covex_ER_Diagram.mermaid` | 40 张表关系图（含规则引擎待开发） |

---

## 数据模型概览

项目共 **40 张数据库表**（37 张已实现 + 3 张规则引擎待开发），分为两大域：

- **产品配置域**（11 张表）：产品、保障责任、缴费计划、规则引用、费率表、字典等
- **运营域**（26 张表）：客户、投保单、保单、理赔、佣金、渠道、权限等
- **规则引擎域**（3 张表，待开发）：规则表、规则行、发布快照

所有表遵循统一规范：
- 表名 `ins_` 前缀 + snake_case
- 通用字段：`id`, `tenant_id`, `is_deleted`, `deleted_at`, `created_by`, `updated_by`, `created_at`, `updated_at`
- 布尔字段 `TINYINT(1)`，枚举全数字编码，金额 `DECIMAL(16,2)`，费率 `DECIMAL(16,6)`

---

## 贡献

本项目为开源项目，欢迎贡献！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 许可证

本项目基于 [GNU AGPL v3.0](LICENSE) 开源许可证发布。

---

## 联系方式

- GitHub Issues：[提交问题或建议](https://github.com/guihuangmenghun/covex/issues)
