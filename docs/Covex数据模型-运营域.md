# Covex 数据模型 — 运营域

> **版本**：v1.0（拆分版 - 纯表结构定义）
> **内容**：运营域 29 张表的字段定义、类型、索引、实体关系
> **配套文档**：`Covex运营域需求规格.md`（用户故事、流程、状态机）
> **约定**：所有表均包含通用字段（id/tenant_id/is_deleted/审计字段），下表仅列出业务字段

---


### 通用表字段（所有运营域表均包含）

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

> 以下表结构中省略通用字段，仅列出业务字段。


## 渠道域


#### `ins_channel` — 渠道商

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `channel_code` | VARCHAR(20) | 渠道商编码 | M |
| `channel_name` | VARCHAR(100) | 渠道商名称 | M |
| `channel_type` | TINYINT | 类型：1-个人代理 2-直销 3-专业代理 4-银保 5-经纪 6-互联网 | M |
| `license_no` | VARCHAR(50) | 许可证号 | M |
| `license_expiry` | DATE | 许可证到期日 | M |
| `contact_name` | VARCHAR(50) | 联系人 | M |
| `contact_phone` | VARCHAR(20) | 联系电话 | M |
| `contact_email` | VARCHAR(100) | 联系邮箱 | S |
| `region_code` | VARCHAR(20) | 所在区域编码 | M |
| `status` | TINYINT | 状态：1-待审核 2-已签约 3-已暂停 4-已终止 | M |
| `contract_no` | VARCHAR(50) | 合同编号 | S |
| `contract_start` | DATE | 合同起始日 | S |
| `contract_end` | DATE | 合同到期日 | S |
| `attributes` | JSON | 扩展属性 | C |

#### `ins_channel_product` — 渠道商-产品授权

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `channel_id` | BIGINT | 关联渠道商 | M |
| `product_id` | BIGINT | 关联产品 | M |
| `first_year_rate` | DECIMAL(5,2) | 首年佣金比例(%) | M |
| `renewal_rate` | DECIMAL(5,2) | 续期佣金比例(%) | M |
| `sale_region` | VARCHAR(100) | 销售区域 | S |
| `is_active` | TINYINT(1) | 是否启用 | M |

> **唯一索引**：`UNIQUE(tenant_id, channel_id, product_id)`

#### `ins_channel_user` — 渠道商账号

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `channel_id` | BIGINT | 关联渠道商 | M |
| `username` | VARCHAR(50) | 登录用户名 | M |
| `password_hash` | VARCHAR(200) | 密码哈希 | M |
| `real_name` | VARCHAR(50) | 真实姓名 | M |
| `agent_license_no` | VARCHAR(50) | 代理人资格证号 | S |
| `phone` | VARCHAR(20) | 手机号 | M |
| `status` | TINYINT | 状态：1-正常 2-锁定 3-停用 | M |
| `last_login_at` | DATETIME | 最后登录时间 | S |

#### `ins_commission` — 佣金记录

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `channel_id` | BIGINT | 渠道商 | M |
| `channel_user_id` | BIGINT | 具体销售人员 | S |
| `policy_id` | BIGINT | 关联保单 | M |
| `commission_type` | TINYINT | 类型：1-首年佣金 2-续期佣金 3-奖金 | M |
| `premium_amount` | DECIMAL(16,2) | 对应保费金额 | M |
| `commission_rate` | DECIMAL(5,2) | 佣金比例(%) | M |
| `commission_amount` | DECIMAL(16,2) | 佣金金额 | M |
| `settle_month` | VARCHAR(7) | 结算月份(YYYY-MM) | M |
| `settle_status` | TINYINT | 结算状态：1-待结算 2-已确认 3-已支付 | M |
| `settled_at` | DATETIME | 结算时间 | S |
| `operator` | VARCHAR(50) | 操作人：SYSTEM（自动生成）/ 财务管理员账号（确认结算） | M |

---


## 客户域


#### `ins_customer` — 客户主表（自然人）

存储所有角色的共用基本信息，一个自然人只有一条记录。

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `customer_code` | VARCHAR(20) | 客户编码（系统生成） | M |
| `customer_name` | VARCHAR(50) | 姓名 | M |
| `id_type` | TINYINT | 证件类型：1-身份证 2-护照 3-军官证 4-港澳通行证 5-统一社会信用代码 6-其他 | M |
| `id_no` | VARCHAR(30) | 证件号码（加密存储） | M |
| `id_expiry` | DATE | 证件有效期 | M |
| `gender` | TINYINT | 性别：1-男 2-女 0-未知 | M |
| `birth_date` | DATE | 出生日期 | M |
| `nationality` | VARCHAR(20) | 国籍 | S |
| `phone` | VARCHAR(20) | 手机号（加密存储） | M |
| `email` | VARCHAR(100) | 邮箱 | S |
| `customer_type` | TINYINT | 类型：1-个人 2-团体（企业/机构） | M |
| `role_flags` | JSON | 已扮演角色标记：{"applicant":true,"insured":true,"beneficiary":false} | M |
| `source` | TINYINT | 来源：1-自主注册 2-代理人录入 3-渠道导入 4-第三方平台 | S |
| `attributes` | JSON | 扩展属性（团险时存企业名称/注册号/法人等） | S |

> **唯一索引**：`UNIQUE(tenant_id, id_type, id_no)`

---

#### `ins_customer_applicant` — 投保人扩展属性

投保人特有的业务属性，一个客户最多一条。

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `customer_id` | BIGINT | 关联 ins_customer.id | M |
| `annual_income` | DECIMAL(16,2) | 年收入 | S |
| `income_source` | VARCHAR(50) | 收入来源 | C |
| `education_level` | TINYINT | 学历：1-高中及以下 2-大专 3-本科 4-硕士及以上 | C |
| `marital_status` | TINYINT | 婚姻状况：1-未婚 2-已婚 3-离异 4-丧偶 | S |
| `has_social_security` | TINYINT(1) | 是否有社保 | S |
| `has_other_insurance` | TINYINT(1) | 是否有其他保险 | S |
| `other_insurance_desc` | VARCHAR(200) | 其他保险说明 | C |

> **唯一索引**：`UNIQUE(tenant_id, customer_id)`

---

#### `ins_customer_insured` — 被保人扩展属性（含健康档案）

被保人特有的业务属性和健康信息，一个客户最多一条。

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `customer_id` | BIGINT | 关联 ins_customer.id | M |
| `occupation` | VARCHAR(50) | 职业 | M |
| `occupation_code` | VARCHAR(10) | 职业类别编码（对应职业分类表） | M |
| `occupation_risk_level` | TINYINT | 职业风险等级：1-极低 2-低 3-中 4-高 5-极高 6-拒保 | M |
| `smoking_status` | TINYINT | 吸烟状态：0-不吸烟 1-已戒烟 2-吸烟 | M |
| `drinking_status` | TINYINT | 饮酒状态：0-不饮酒 1-已戒酒 2-偶尔 3-经常 | S |
| `bmi` | DECIMAL(4,1) | BMI 指数 | S |
| `blood_type` | VARCHAR(5) | 血型 | C |
| `medical_history` | JSON | 既往病史（疾病编码+确诊日期+治疗状态列表） | M |
| `family_history` | JSON | 家族病史（亲属关系+疾病编码列表） | S |
| `current_medications` | JSON | 当前用药（药品名称+用量列表） | S |
| `last_health_update` | DATETIME | 健康信息最后更新时间 | S |

> **唯一索引**：`UNIQUE(tenant_id, customer_id)`

**medical_history JSON 示例**：
```json
[
  {"disease_code": "I10", "disease_name": "高血压", "diagnosed_date": "2020-03-15", "status": "ongoing", "treatment": "药物控制"},
  {"disease_code": "K21", "disease_name": "胃食管反流", "diagnosed_date": "2022-08-01", "status": "resolved", "treatment": "已治愈"}
]
```

---

#### `ins_customer_bank_account` — 银行账户

一个客户可以有多个银行账户，分别用于缴费和收款。

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `customer_id` | BIGINT | 关联 ins_customer.id | M |
| `account_holder` | VARCHAR(50) | 账户户名 | M |
| `bank_name` | VARCHAR(100) | 开户行名称 | M |
| `bank_code` | VARCHAR(20) | 银行编码 | S |
| `branch_name` | VARCHAR(100) | 支行名称 | S |
| `account_no` | VARCHAR(30) | 银行账号（加密存储） | M |
| `account_type` | TINYINT | 账户类型：1-储蓄卡 2-信用卡 3-对公账户 | M |
| `usage_type` | TINYINT | 用途：1-缴费扣款 2-理赔收款 3-两者皆可 | M |
| `is_default` | TINYINT(1) | 是否默认账户 | M |
| `agreement_no` | VARCHAR(50) | 代扣协议编号（如有自动扣款） | S |
| `agreement_expiry` | DATE | 代扣协议到期日 | S |
| `status` | TINYINT | 状态：1-正常 2-已冻结 3-已注销 | M |

> **索引**：`INDEX(tenant_id, customer_id)`

---

#### `ins_customer_address` — 联系地址

一个客户可以有多个不同类型的地址。

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `customer_id` | BIGINT | 关联 ins_customer.id | M |
| `address_type` | TINYINT | 地址类型：1-户籍地址 2-常住地址 3-工作地址 4-通讯地址 | M |
| `province` | VARCHAR(20) | 省/直辖市 | M |
| `city` | VARCHAR(20) | 市 | M |
| `district` | VARCHAR(20) | 区/县 | M |
| `detail` | VARCHAR(200) | 详细地址 | M |
| `postal_code` | VARCHAR(10) | 邮编 | S |
| `is_default` | TINYINT(1) | 是否该类型默认地址 | M |

> **唯一索引**：`UNIQUE(tenant_id, customer_id, address_type)`

---


## 承保域


#### `ins_proposal` — 投保单

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `proposal_no` | VARCHAR(30) | 投保单号 | M |
| `product_id` | BIGINT | 关联产品 | M |
| `channel_id` | BIGINT | 渠道商（可空=直销） | M |
| `channel_user_id` | BIGINT | 销售人员（可空） | S |
| `applicant_id` | BIGINT | 投保人（关联客户） | M |
| `insured_id` | BIGINT | 被保人（关联客户） | M |
| `product_snapshot` | JSON | 产品快照（名称/类型/版本） | M |
| `selected_coverages` | JSON | 选中的保障责任（编码+保额） | M |
| `selected_premium_plan` | JSON | 选中的缴费计划 | M |
| `health_declaration` | JSON | 健康告知（问题+答案） | S |
| `total_premium` | DECIMAL(16,2) | 总保费 | M |
| `total_sum_insured` | DECIMAL(16,2) | 总保额 | M |
| `status` | TINYINT | 状态：1-待校验 2-待核保 3-核保中 4-待支付 5-已支付 6-已出单 7-已拒保 8-已撤销 | M |
| `submit_at` | DATETIME | 提交时间 | M |
| `operator` | VARCHAR(50) | 投保录入人：代理人账号 / 投保人账号（自助投保） | M |

#### `ins_underwriting_record` — 核保记录

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `proposal_id` | BIGINT | 关联投保单 | M |
| `uw_type` | TINYINT | 核保类型：1-自动核保 2-人工核保 | M |
| `uw_result` | TINYINT | 核保结论：1-标准体 2-加费 3-除外 4-延期 5-拒保 6-转人工 | M |
| `loading_amount` | DECIMAL(16,2) | 加费金额（加费时） | S |
| `exclusion_desc` | VARCHAR(500) | 除外责任描述（除外时） | S |
| `uw_comment` | VARCHAR(500) | 核保备注 | S |
| `uw_operator` | VARCHAR(50) | 核保操作员 | S |
| `uw_at` | DATETIME | 核保时间 | M |

#### `ins_policy` — 保单主表

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `policy_no` | VARCHAR(30) | 保单号（全局唯一） | M |
| `proposal_id` | BIGINT | 来源投保单 | M |
| `product_id` | BIGINT | 关联产品 | M |
| `channel_id` | BIGINT | 渠道商 | M |
| `applicant_id` | BIGINT | 投保人 | M |
| `insured_id` | BIGINT | 被保人 | M |
| `product_snapshot` | JSON | 产品快照 | M |
| `effective_date` | DATE | 生效日 | M |
| `expiry_date` | DATE | 到期日（终身为null） | M |
| `total_premium` | DECIMAL(16,2) | 总保费 | M |
| `total_sum_insured` | DECIMAL(16,2) | 总保额 | M |
| `payment_mode` | TINYINT | 缴费方式：1-趸交 2-期交 | M |
| `status` | TINYINT | 状态：1-有效 2-中止 3-终止 4-退保 | M |
| `termination_reason` | TINYINT | 终止原因：1-满期 2-退保 3-犹豫期退保 4-理赔终止 5-身故 6-复效超期 7-拒保终止 | S |
| `terminated_at` | DATETIME | 终止时间 | S |
| `beneficiaries` | JSON | 受益人列表（姓名/关系/比例/证件号） | M |
| `attributes` | JSON | 扩展属性 | S |

> **唯一索引**：`UNIQUE(tenant_id, policy_no)`

#### `ins_policy_coverage` — 保单险种明细

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `policy_id` | BIGINT | 关联保单 | M |
| `coverage_code` | VARCHAR(20) | 责任编码 | M |
| `coverage_name` | VARCHAR(100) | 责任名称 | M |
| `sum_insured` | DECIMAL(16,2) | 保额 | M |
| `premium` | DECIMAL(16,2) | 保费 | M |
| `deductible` | DECIMAL(16,2) | 免赔额 | S |
| `coverage_detail` | JSON | 保障详情快照 | M |
| `status` | TINYINT | 状态：1-有效 2-已终止 | M |

#### `ins_policy_premium` — 保单缴费计划

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `policy_id` | BIGINT | 关联保单 | M |
| `premium_plan_code` | VARCHAR(20) | 缴费计划编码 | M |
| `payment_frequency` | TINYINT | 缴费频率 | M |
| `payment_term` | SMALLINT | 缴费期间 | M |
| `payment_term_unit` | TINYINT | 期间单位 | M |
| `period_premium` | DECIMAL(16,2) | 每期保费 | M |
| `total_periods` | SMALLINT | 总期数 | M |
| `paid_periods` | SMALLINT | 已缴期数 | M |
| `next_due_date` | DATE | 下期应缴日 | M |
| `grace_period` | SMALLINT | 宽限天数 | M |

#### `ins_payment` — 支付记录

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `payment_no` | VARCHAR(30) | 支付流水号 | M |
| `policy_id` | BIGINT | 关联保单 | M |
| `proposal_id` | BIGINT | 关联投保单（首期支付时） | S |
| `payment_type` | TINYINT | 类型：1-首期保费 2-续期保费 3-退保金 4-理赔金 5-保单借款 | M |
| `amount` | DECIMAL(16,2) | 金额 | M |
| `pay_channel` | TINYINT | 支付通道：1-微信 2-支付宝 3-银行转账 4-线下 | M |
| `pay_channel_no` | VARCHAR(50) | 第三方支付流水号 | S |
| `status` | TINYINT | 状态：1-待支付 2-已支付 3-已退款 4-支付失败 | M |
| `paid_at` | DATETIME | 支付时间 | M |
| `operator` | VARCHAR(50) | 支付确认人：SYSTEM（回调自动处理）/ 财务账号（人工退款） | M |

---


## 保单服务域


#### `ins_renewal_bill` — 续期账单

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `bill_no` | VARCHAR(30) | 账单编号 | M |
| `policy_id` | BIGINT | 关联保单 | M |
| `period_no` | SMALLINT | 期数（第N期） | M |
| `due_date` | DATE | 应缴日期 | M |
| `amount` | DECIMAL(16,2) | 应缴金额 | M |
| `grace_end_date` | DATE | 宽限期截止日 | M |
| `status` | TINYINT | 状态：1-待缴 2-已缴 3-逾期 4-已豁免 | M |
| `paid_at` | DATETIME | 实际缴费时间 | S |
| `payment_id` | BIGINT | 关联支付记录 | S |
| `remind_count` | SMALLINT | 提醒次数 | S |
| `last_remind_at` | DATETIME | 最后提醒时间 | S |
| `operator` | VARCHAR(50) | 续期处理人：SYSTEM（定时任务生成）/ 运营人员账号（手动处理） | M |

#### `ins_endorsement` — 保全申请

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `endorsement_no` | VARCHAR(30) | 保全单号 | M |
| `policy_id` | BIGINT | 关联保单 | M |
| `endorsement_type` | TINYINT | 类型：1-信息变更 2-受益人变更 3-保额变更 4-缴费方式变更 5-退保 6-复效 7-借款 | M |
| `apply_reason` | VARCHAR(200) | 申请原因 | S |
| `apply_at` | DATETIME | 申请时间 | M |
| `applicant_name` | VARCHAR(50) | 申请人 | M |
| `status` | TINYINT | 状态：1-待审核 2-审核中 3-已通过 4-已驳回 5-已生效 | M |
| `reviewer` | VARCHAR(50) | 审核人 | S |
| `review_comment` | VARCHAR(200) | 审核意见 | S |
| `reviewed_at` | DATETIME | 审核时间 | S |
| `effective_at` | DATETIME | 生效时间 | S |
| `refund_amount` | DECIMAL(16,2) | 退保金额（退保时） | S |
| `operator` | VARCHAR(50) | 保全执行人：保全人员账号（审核通过后执行变更） | M |

#### `ins_endorsement_change` — 保全变更明细

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `endorsement_id` | BIGINT | 关联保全申请 | M |
| `change_field` | VARCHAR(50) | 变更字段名 | M |
| `old_value` | TEXT | 变更前值 | M |
| `new_value` | TEXT | 变更后值 | M |

#### `ins_policy_loan` — 保单借款

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `loan_no` | VARCHAR(30) | 借款单号 | M |
| `policy_id` | BIGINT | 关联保单 | M |
| `loan_amount` | DECIMAL(16,2) | 借款金额 | M |
| `interest_rate` | DECIMAL(5,4) | 借款利率(年化) | M |
| `loan_date` | DATE | 借款日期 | M |
| `due_date` | DATE | 还款到期日 | M |
| `repaid_amount` | DECIMAL(16,2) | 已还金额 | M |
| `status` | TINYINT | 状态：1-借款中 2-已还清 3-逾期 | M |
| `operator` | VARCHAR(50) | 借款处理人：保全人员账号 | M |

---


## 理赔域


#### `ins_claim` — 理赔案件

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `claim_no` | VARCHAR(30) | 理赔案件号 | M |
| `policy_id` | BIGINT | 关联保单 | M |
| `coverage_id` | BIGINT | 关联保单险种明细 | M |
| `reporter_id` | BIGINT | 报案人（关联客户） | M |
| `reporter_relation` | TINYINT | 报案人与被保人关系：1-本人 2-投保人 3-代理人 4-其他 | M |
| `accident_date` | DATE | 出险日期 | M |
| `accident_type` | VARCHAR(50) | 事故类型 | M |
| `accident_desc` | VARCHAR(500) | 事故描述 | M |
| `accident_location` | VARCHAR(200) | 出险地点 | S |
| `claim_amount` | DECIMAL(16,2) | 申请赔付金额 | M |
| `approved_amount` | DECIMAL(16,2) | 核准赔付金额 | S |
| `status` | TINYINT | 状态：1-已报案 2-审核中 3-需调查 4-已赔付 5-已拒赔 6-已结案 | M |
| `claim_handler` | VARCHAR(50) | 理赔员 | S |
| `reported_at` | DATETIME | 报案时间 | M |
| `closed_at` | DATETIME | 结案时间 | S |

#### `ins_claim_document` — 理赔材料

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `claim_id` | BIGINT | 关联理赔案件 | M |
| `document_type` | TINYINT | 材料类型：1-身份证明 2-医疗单据 3-事故证明 4-死亡证明 5-伤残鉴定 6-其他 | M |
| `file_url` | VARCHAR(500) | 文件URL | M |
| `file_name` | VARCHAR(100) | 文件名 | M |
| `uploaded_at` | DATETIME | 上传时间 | M |
| `uploaded_by` | VARCHAR(50) | 上传人 | M |

#### `ins_claim_review` — 理赔审核记录

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `claim_id` | BIGINT | 关联理赔案件 | M |
| `review_type` | TINYINT | 审核类型：1-自动审核 2-人工审核 3-调查审核 | M |
| `review_result` | TINYINT | 结论：1-正常赔付 2-部分赔付 3-拒赔 4-需调查 | M |
| `approved_amount` | DECIMAL(16,2) | 核准金额 | S |
| `reject_reason` | VARCHAR(500) | 拒赔原因（拒赔时） | S |
| `review_comment` | VARCHAR(500) | 审核意见 | S |
| `reviewer` | VARCHAR(50) | 审核人 | M |
| `reviewed_at` | DATETIME | 审核时间 | M |

#### `ins_claim_payment` — 赔付记录

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `claim_id` | BIGINT | 关联理赔案件 | M |
| `payment_id` | BIGINT | 关联支付记录 | M |
| `beneficiary_id` | BIGINT | 收款人（关联客户） | M |
| `beneficiary_name` | VARCHAR(50) | 收款人姓名 | M |
| `amount` | DECIMAL(16,2) | 赔付金额 | M |
| `paid_at` | DATETIME | 支付时间 | M |
| `operator` | VARCHAR(50) | 赔付处理人：SYSTEM（自动打款）/ 理赔财务账号（人工处理） | M |

---


## 基础服务层


#### `ins_user` — 系统用户

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `username` | VARCHAR(50) | 登录用户名 | M |
| `password_hash` | VARCHAR(200) | 密码哈希 | M |
| `real_name` | VARCHAR(50) | 真实姓名 | M |
| `phone` | VARCHAR(20) | 手机号 | M |
| `email` | VARCHAR(100) | 邮箱 | S |
| `user_type` | TINYINT | 类型：1-内部员工 2-渠道商 3-客户 | M |
| `status` | TINYINT | 状态：1-正常 2-锁定 3-停用 | M |
| `last_login_at` | DATETIME | 最后登录时间 | S |

#### `ins_role` — 角色

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `role_code` | VARCHAR(30) | 角色编码 | M |
| `role_name` | VARCHAR(50) | 角色名称 | M |
| `description` | VARCHAR(200) | 角色描述 | S |
| `is_system` | TINYINT(1) | 是否系统内置角色（不可删除） | M |

#### `ins_permission` — 权限

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `permission_code` | VARCHAR(50) | 权限编码（如 proposal:view, uw:execute） | M |
| `permission_name` | VARCHAR(100) | 权限名称 | M |
| `module` | VARCHAR(30) | 所属模块（channel/policy/claim/uw/endorsement） | M |
| `action` | VARCHAR(20) | 操作类型（view/create/edit/delete/approve/execute） | M |

#### `ins_user_role` — 用户-角色关联

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `user_id` | BIGINT | 用户 | M |
| `role_id` | BIGINT | 角色 | M |

> **唯一索引**：`UNIQUE(user_id, role_id)`

#### `ins_role_permission` — 角色-权限关联

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `role_id` | BIGINT | 角色 | M |
| `permission_id` | BIGINT | 权限 | M |

> **唯一索引**：`UNIQUE(role_id, permission_id)`

#### `ins_data_scope` — 数据权限范围

| 字段名 | 类型 | 说明 | 优先级 |
|---|---|---|---|
| `role_id` | BIGINT | 角色 | M |
| `scope_type` | TINYINT | 范围类型：1-区域 2-渠道 3-产品 4-全部 | M |
| `scope_value` | VARCHAR(100) | 范围值（区域编码/渠道ID/产品ID，逗号分隔） | M |

---

## 七、实体关系总览

```
ins_channel ──1:N── ins_channel_product ──N:1── ins_product (配置域)
    │
    └──1:N── ins_channel_user
                │
ins_customer ──1:1── ins_customer_applicant (投保人扩展)
    │
    ├──1:1── ins_customer_insured (被保人扩展+健康档案)
    ├──1:N── ins_customer_bank_account (银行账户)
    ├──1:N── ins_customer_address (联系地址)
    │
    └──1:N── ins_proposal (投保人/被保人)
                │
                └──1:N── ins_underwriting_record
                │
                └──1:1── ins_policy
                            │
                            ├──1:N── ins_policy_coverage
                            ├──1:N── ins_policy_premium
                            ├──1:N── ins_renewal_bill
                            ├──1:N── ins_endorsement ──1:N── ins_endorsement_change
                            ├──1:N── ins_policy_loan
                            ├──1:N── ins_claim
                            │         ├──1:N── ins_claim_document
                            │         ├──1:N── ins_claim_review
                            │         └──1:N── ins_claim_payment
                            └──1:N── ins_payment

ins_user ──M:N── ins_role ──M:N── ins_permission
                    │
                    └──1:N── ins_data_scope

ins_commission ──N:1── ins_channel + ins_policy + ins_payment
```

---

## 八、设计注释

### 1. 产品配置域与运营域的数据流
- 投保时：产品配置域的数据**快照**到投保单和保单（product_snapshot / coverage_detail）
- 保单引用产品 ID 但同时保存快照，产品下架/修改不影响已有保单
- 费率表通过 Redis 缓存查询，不直接存储在运营域

### 2. LiteFlow 链在运营域的应用
- **投保链**：validate → underwrite → calculate → issue → notify
- **保全链**：validate → calculate → approve → execute → notify
- **理赔链**：validate → match_coverage → calculate → review → pay → notify
- **续期链**：generate_bill → remind → collect → confirm → renew

### 3. 后续可扩展
- 再保域（分出/分入管理）
- 团险批量投保
- 互联网保险对接（第三方平台 API）
- 监管报送（保监会数据上报）
- BI 数据分析（运营报表/精算报表）

### 4. 表统计
| 域 | 表数 | 核心表 |
|---|---|---|
| 渠道域 | 4 | channel, channel_product, channel_user, commission |
| 客户域 | 5 | customer, customer_applicant, customer_insured, customer_bank_account, customer_address |
| 承保域 | 6 | proposal, underwriting_record, policy, policy_coverage, policy_premium, payment |
| 保单服务域 | 4 | renewal_bill, endorsement, endorsement_change, policy_loan |
| 理赔域 | 4 | claim, claim_document, claim_review, claim_payment |
| 基础服务层 | 6 | user, role, permission, user_role, role_permission, data_scope |
| **合计** | **29** | |

---

