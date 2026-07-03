# Covex 操作人字段补充计划

> **状态**：待执行（开发完成后统一补充）
> **创建日期**：2026-07-03
> **执行时机**：全部 14 个 Story 开发完成后
> **规范来源**：covex-context-lock skill → 三、数据模型约定 → 操作人字段

---

## 背景

保险业务中"谁执行了这笔操作"是审计追溯的核心字段。当前通用字段 `created_by`/`updated_by` 只能记录数据库行的创建/修改人，无法区分以下场景：

- 代理人帮投保人录入投保单 → `created_by` = 代理人，但投保发起方是投保人
- 系统自动核保 → 没有人在操作，`created_by` 可能是系统账号
- 支付回调由系统自动处理 → 执行人是 SYSTEM，不是某个具体的人
- 佣金结算由定时任务生成 → 生成人是 SYSTEM，财务确认人是另一个角色

因此需要为业务操作表统一新增 `operator` 字段（VARCHAR(50)），填写规则：人工操作填操作员账号，系统自动操作填 `SYSTEM`。

---

## 需要新增 operator 字段的表

### A 类：资金操作表（必须）

| # | 表名 | 所属域 | operator 含义 | 典型值 |
|---|---|---|---|---|
| 1 | `ins_payment` | 承保域 | 支付确认/退款处理人 | SYSTEM（回调自动处理）/ 财务账号（人工退款） |
| 2 | `ins_claim_payment` | 理赔域 | 赔付打款处理人 | SYSTEM / 理赔财务账号 |
| 3 | `ins_commission` | 渠道域 | 佣金结算确认人 | SYSTEM（自动生成）/ 财务管理员（确认结算） |

### B 类：业务执行表（必须）

| # | 表名 | 所属域 | operator 含义 | 典型值 |
|---|---|---|---|---|
| 4 | `ins_endorsement` | 保单服务域 | 保全变更执行人 | 保全人员账号（审核通过后执行变更） |
| 5 | `ins_renewal_bill` | 保单服务域 | 续期账单处理人 | SYSTEM（定时任务生成）/ 运营人员（手动处理） |
| 6 | `ins_policy_loan` | 保单服务域 | 借款处理人 | 保全人员账号 |
| 7 | `ins_proposal` | 承保域 | 投保录入人 | 代理人账号 / 投保人账号（自助投保） |

### C 类：已有专用操作人字段（不需要新增）

| 表名 | 已有字段 | 说明 |
|---|---|---|
| `ins_underwriting_record` | `uw_operator` | 核保操作员 |
| `ins_claim` | `claim_handler` | 理赔员 |
| `ins_claim_review` | `reviewer` | 审核人 |
| `ins_claim_document` | `uploaded_by` | 上传人 |
| `ins_endorsement` | `applicant_name` + `reviewer` | 申请人 + 审核人（但缺执行人，见 B 类） |
| `ins_product_changelog` | `operator` | 产品变更操作人 |

### D 类：纯数据维护表（不需要新增）

ins_dict / ins_product 及子表 / ins_customer 及子表 / ins_channel 及子表 / ins_user / ins_role / ins_permission / ins_user_role / ins_role_permission / ins_data_scope

这些表用通用字段 `created_by`/`updated_by` 即可，操作人 = 数据录入人。

---

## 执行清单

开发完成后，逐条执行以下修改：

### 1. 数据模型文档修改

- [ ] `Covex数据模型-运营域.md`：为 A 类 + B 类共 7 张表新增 `operator VARCHAR(50)` 字段定义
- [ ] `Covex数据模型-运营域.md`：在通用字段说明中补充 operator 字段的适用范围和填写规则

### 2. 需求规格文档修改

- [ ] `Covex运营域需求规格.md`：在 §11 异常与边界处理中补充操作人追溯要求
- [ ] `Covex运营域需求规格.md`：在相关 Story 的 AC 中补充 operator 字段的验收标准（如 Story 3.3 支付回调 operator=SYSTEM）

### 3. 开发计划修改

- [ ] `Covex开发计划.md`：新增一个 Story（如 S15 - 操作人字段补充），包含 7 张表的 DDL ALTER + 代码适配任务

### 4. 测试计划修改

- [ ] `Covex测试计划.md`：新增对应测试 Story，验证 operator 字段的填写规则（人工操作填账号、自动操作填 SYSTEM）

### 5. 代码修改

- [ ] 7 张表执行 `ALTER TABLE ADD COLUMN operator VARCHAR(50)`
- [ ] 对应的 Entity 类新增 `operator` 字段
- [ ] 对应的 Service 层在业务操作时设置 operator 值
- [ ] 自动操作场景（支付回调、定时任务、MQ 消费）统一填写 "SYSTEM"
