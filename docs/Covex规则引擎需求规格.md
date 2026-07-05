# Covex 规则引擎需求规格

> **版本**：v1.1
> **日期**：2026-07-06
> **来源**：17 个 LiteFlow 硬编码组件审计 + 业务流程框架 + 运营域需求规格
> **目标**：定义规则引擎需要覆盖的全部规则域，为统一评估 API 提供需求依据
> **核心 API**：传入保单 JSON → 返回结论 + 违反规则提示
> **版本追溯**：version + requirement_no 双定位，逻辑删除保留全量历史，发布快照支持版本对比

---

## 一、统一评估 API 设计

### 1.1 核心接口

```
POST /api/rule/evaluate
```

**设计理念**：一个接口覆盖所有规则域。通过 `rule_domain` 参数区分场景，引擎自动加载对应规则表并评估。

### 1.2 请求格式

```json
{
  "rule_domain": "underwriting",
  "product_id": 13,
  "channel_id": 2,
  "facts": {
    "applicant": {
      "name": "张三",
      "id_type": 1,
      "id_number": "110101199001011234",
      "age": 35,
      "gender": 1,
      "phone": "13800138000",
      "occupation_type": 2,
      "income": 200000
    },
    "insured": {
      "name": "张三",
      "age": 35,
      "gender": 1,
      "health_status": {
        "bmi": 23.5,
        "smoking_status": 0,
        "medical_history": "",
        "current_diseases": ""
      }
    },
    "policy": {
      "sum_insured": 500000,
      "term": 20,
      "payment_term": 20,
      "payment_frequency": 12,
      "coverages": ["COV_DEATH", "COV_DISABILITY"],
      "premium_plan": "PP_YEARLY_20"
    },
    "existing_policies": {
      "total_risk_amount": 300000,
      "policy_count": 1
    }
  }
}
```

### 1.3 响应格式

```json
{
  "code": 200,
  "data": {
    "rule_domain": "underwriting",
    "conclusion": "REJECT",
    "conclusion_label": "拒保",
    "confidence": "HIGH",
    "matched_rules": [
      {
        "rule_code": "UW_AGE_001",
        "rule_name": "投保年龄校验",
        "result": "PASS",
        "message": "年龄35岁，在承保范围(18-60)内"
      },
      {
        "rule_code": "UW_HEALTH_003",
        "rule_name": "BMI异常校验",
        "result": "FAIL",
        "message": "BMI值28.5超出正常范围(15-28)，属于肥胖，需加费承保",
        "suggestion": "建议加费15%或转人工核保"
      },
      {
        "rule_code": "UW_CUMUL_001",
        "rule_name": "累计风险保额校验",
        "result": "FAIL",
        "message": "累计风险保额80万超过上限(50万)",
        "suggestion": "建议降低保额至50万或提供财务证明"
      }
    ],
    "failed_count": 2,
    "passed_count": 5,
    "total_count": 7,
    "actions": [
      {
        "action_type": "REJECT",
        "action_detail": "累计保额超限且BMI异常，建议拒保",
        "overrideable": true,
        "override_role": "underwriter"
      }
    ]
  }
}
```

### 1.4 结论编码（通用）

| conclusion | 含义 | 适用规则域 |
|---|---|---|
| `PASS` | 全部通过 | validation, underwriting, claim, endorsement |
| `FAIL` | 校验不通过 | validation, claim |
| `STANDARD` | 标准体承保 | underwriting |
| `LOADING` | 加费承保 | underwriting |
| `EXCLUSION` | 除外责任 | underwriting |
| `POSTPONE` | 延期 | underwriting |
| `REJECT` | 拒保/拒赔 | underwriting, claim |
| `MANUAL` | 转人工 | underwriting, claim |
| `APPROVE` | 审批通过 | endorsement, commission |
| `CALCULATED` | 计算完成 | premium, commission |

---

## 二、规则域清单（6 个域，42 条规则）

### 2.1 规则域总览

| rule_domain | 名称 | 规则数 | 类型 | 优先级 | 当前状态 |
|---|---|---|---|---|---|
| `validation` | 投保校验 | 8 条 | 校验类 | **MUST** | 已有 5 个硬编码组件 |
| `underwriting` | 核保决策 | 8 条 | 决策类 | **MUST** | 已有 3 个硬编码组件 |
| `premium` | 费率计算 | 5 条 | 计算类 | **MUST** | 已有 Aviator 表达式 |
| `claim` | 理赔审核 | 8 条 | 混合类 | **MUST** | 已有 5 个硬编码组件 |
| `endorsement` | 保全校验 | 6 条 | 混合类 | **SHOULD** | 尚未实现 |
| `commission` | 佣金计算 | 7 条 | 计算类 | **SHOULD** | 已有硬编码逻辑 |

---

### 2.2 规则域 1：`validation` — 投保校验（MUST）

> **类型**：校验类（通过/不通过）
> **触发时机**：投保单提交前
> **当前对应组件**：validateAge, validateId, validateAmount, validateRider, validateChannel

| # | rule_code | rule_name | 输入 facts | 判断逻辑 | 失败提示 | 优先级 |
|---|---|---|---|---|---|---|
| V1 | `VAL_AGE_001` | 投保年龄校验 | insured.age, product.attributes.min/max_insured_age | 年龄 ∈ [min, max] | "被保人年龄{age}不在承保范围[{min}-{max}]内" | MUST |
| V2 | `VAL_AGE_002` | 投保人年龄校验 | applicant.age | 投保人年龄 ≥ 18 且 ≤ 65 | "投保人年龄需18-65岁，当前{age}岁" | MUST |
| V3 | `VAL_ID_001` | 证件号格式校验 | applicant.id_type, applicant.id_number | 身份证18位校验/护照格式 | "证件号格式不正确" | MUST |
| V4 | `VAL_ID_002` | 证件号重复校验 | applicant.id_number, existing_customers | 同一证件号不能重复投保 | "该证件号已存在投保记录" | MUST |
| V5 | `VAL_AMT_001` | 保额范围校验 | policy.sum_insured, product.attributes.min/max_sum_insured | 保额 ∈ [min, max] 且为整数倍 | "保额需在{min}-{max}之间，且为{unit}的整数倍" | MUST |
| V6 | `VAL_RIDER_001` | 主附险关联校验 | policy.coverages, product_rider_rel | 附加险必须在主险的可挂列表中 | "附加险{rider}不能挂在主险{main}下" | SHOULD |
| V7 | `VAL_CHANNEL_001` | 渠道授权校验 | channel_id, product.sale_channel | 渠道必须被授权销售该产品 | "渠道{channel}未获授权销售产品{product}" | MUST |
| V8 | `VAL_REL_001` | 投保关系校验 | applicant_insured_relation | 投保人与被保人关系必须合法（本人/配偶/父母/子女） | "投保人与被保人关系'{relation}'不在允许范围内" | SHOULD |

**示例规则数据**：

```json
{
  "rule_table": {
    "rule_domain": "validation",
    "scope_type": "COMMON",
    "rule_table_name": "投保基础校验"
  },
  "rows": [
    {
      "rule_code": "VAL_AGE_001",
      "conditions": [
        {"field": "insured.age", "operator": "<", "value": "$product.attributes.min_insured_age"},
        {"field": "insured.age", "operator": ">", "value": "$product.attributes.max_insured_age"}
      ],
      "logic_operator": "OR",
      "actions": [
        {"type": "FAIL", "message": "被保人年龄不在承保范围内"}
      ],
      "priority": 1
    }
  ]
}
```

---

### 2.3 规则域 2：`underwriting` — 核保决策（MUST）

> **类型**：决策类（多结论选择：STANDARD/LOADING/EXCLUSION/POSTPONE/REJECT/MANUAL）
> **触发时机**：投保校验通过后
> **当前对应组件**：uwHealth, uwOccupation, UwCumulativeAmount

| # | rule_code | rule_name | 输入 facts | 判断逻辑 | 结论映射 | 优先级 |
|---|---|---|---|---|---|---|
| UW1 | `UW_AGE_001` | 核保年龄评估 | insured.age, product | 按产品配置的核保年龄范围 | PASS/REJECT | MUST |
| UW2 | `UW_HEALTH_001` | BMI异常评估 | insured.health_status.bmi | BMI < 15 → REJECT; BMI > 35 → REJECT; BMI > 28 → LOADING | STANDARD/LOADING/REJECT | MUST |
| UW3 | `UW_HEALTH_002` | 吸烟加费评估 | insured.health_status.smoking_status | smoking = 1 → LOADING(+15%) | LOADING | MUST |
| UW4 | `UW_HEALTH_003` | 既往病史评估 | insured.health_status.medical_history | 非空 → 按疾病列表匹配 | STANDARD/LOADING/EXCLUSION/REJECT/MANUAL | MUST |
| UW5 | `UW_OCC_001` | 职业类别评估 | insured.occupation_type | 1-2 → STANDARD; 3-4 → LOADING; ≥5 → EXCLUSION/REJECT | STANDARD/LOADING/REJECT | MUST |
| UW6 | `UW_CUMUL_001` | 累计风险保额评估 | existing_policies.total_risk_amount + policy.sum_insured | 累计 > 产品上限 → REJECT; 累计 > 免体检上限 → MANUAL | STANDARD/MANUAL/REJECT | MUST |
| UW7 | `UW_FIN_001` | 财务核保评估 | applicant.income, policy.sum_insured | 保额 > 年收入×20 → MANUAL（高保额财务核保） | STANDARD/MANUAL | SHOULD |
| UW8 | `UW_CHANNEL_001` | 渠道特殊规则 | channel_id, product | 特定渠道有专属核保规则 | 按渠道配置 | COULD |

**示例规则数据**：

```json
{
  "rule_table": {
    "rule_domain": "underwriting",
    "scope_type": "COMMON",
    "rule_table_name": "通用核保决策表"
  },
  "rows": [
    {
      "rule_code": "UW_HEALTH_001",
      "conditions": [
        {"field": "insured.health_status.bmi", "operator": ">", "value": 35}
      ],
      "actions": [
        {"type": "REJECT", "message": "BMI>{35}，严重肥胖，不予承保"}
      ],
      "priority": 1
    },
    {
      "rule_code": "UW_HEALTH_001",
      "conditions": [
        {"field": "insured.health_status.bmi", "operator": ">", "value": 28}
      ],
      "actions": [
        {"type": "LOADING", "message": "BMI>{28}，肥胖，加费15%", "loading_rate": 0.15}
      ],
      "priority": 2
    },
    {
      "rule_code": "UW_HEALTH_001",
      "conditions": [
        {"field": "insured.health_status.bmi", "operator": "<", "value": 15}
      ],
      "actions": [
        {"type": "REJECT", "message": "BMI<{15}，体重过轻，不予承保"}
      ],
      "priority": 3
    }
  ]
}
```

---

### 2.4 规则域 3：`premium` — 费率计算（MUST）

> **类型**：计算类（输出保费金额）
> **触发时机**：核保通过后
> **当前对应**：Aviator 表达式 + Redis 费率表

| # | rule_code | rule_name | 输入 facts | 计算逻辑 | 输出 | 优先级 |
|---|---|---|---|---|---|---|
| P1 | `PREM_BASE_001` | 基础保费计算 | sum_insured, age, gender, term | premium = sum_insured × rate(age,gender,term) | base_premium | MUST |
| P2 | `PREM_LOAD_001` | 加费保费计算 | base_premium, loading_rate | extra = base_premium × loading_rate | loading_premium | MUST |
| P3 | `PREM_DISC_001` | 折扣计算 | base_premium, discount_type | 按渠道/团单折扣 | discount_amount | SHOULD |
| P4 | `PREM_TOTAL_001` | 总保费计算 | base_premium, loading_premium, discount | total = base + loading - discount | total_premium | MUST |
| P5 | `PREM_ROUND_001` | 保费取整 | total_premium, product.rounding_mode | 按产品配置的取整方式 | final_premium | MUST |

**示例**：

```json
{
  "rule_code": "PREM_BASE_001",
  "conditions": [
    {"field": "product.product_type", "operator": "==", "value": 1}
  ],
  "actions": [
    {"type": "CALCULATE", "expression": "sum_insured * rateTable.get(age, gender, term)", "output_field": "base_premium"}
  ]
}
```

---

### 2.5 规则域 4：`claim` — 理赔审核（MUST）

> **类型**：混合类（校验 + 决策 + 计算）
> **触发时机**：理赔报案提交后
> **当前对应组件**：claimValidatePolicy, claimValidateCoverage, claimValidateWaitingPeriod, claimValidateCoverageMatch, claimCalculation

| # | rule_code | rule_name | 输入 facts | 判断逻辑 | 结论/输出 | 优先级 |
|---|---|---|---|---|---|---|
| C1 | `CLM_VAL_001` | 保单有效性校验 | policy.status, policy.effective_date, policy.expiry_date | 保单必须为有效状态 | PASS/FAIL: "保单已失效/已过期" | MUST |
| C2 | `CLM_VAL_002` | 保障责任匹配 | claim.incident_type, policy.coverages | 出险类型必须在承保责任范围内 | PASS/FAIL: "该事故类型不在保障范围内" | MUST |
| C3 | `CLM_VAL_003` | 等待期校验 | policy.effective_date, claim.incident_date, product.observation_period | 出险日期 > 生效日 + 等待期 | PASS/FAIL: "尚在等待期内，不予赔付" | MUST |
| C4 | `CLM_VAL_004` | 免赔额计算 | claim.amount, product.deductible | deductible 扣除 | deductible_amount | MUST |
| C5 | `CLM_CALC_001` | 赔付金额计算 | claim.amount, deductible, sum_insured, claim_ratio, already_paid | claim = (amount - deductible) × ratio; min(claim, limit - paid) | claim_amount | MUST |
| C6 | `CLM_VAL_005` | 累计赔付限额校验 | already_paid + claim_amount, product.max_claim_limit | 累计不超过总限额 | PASS/FAIL: "累计赔付已达上限" | MUST |
| C7 | `CLM_FRAUD_001` | 反欺诈规则 | claim 特征（频率/金额/时间） | 短期多次理赔、金额异常等 | PASS/MANUAL: "疑似欺诈，转调查" | SHOULD |
| C8 | `CLM_AMT_001` | 理赔金额权限 | claim_amount | <1万自动通过; 1-10万理赔员审批; >10万主管审批 | approve_level | SHOULD |

---

### 2.6 规则域 5：`endorsement` — 保全校验（SHOULD）

> **类型**：混合类
> **触发时机**：保全申请提交时
> **当前状态**：尚未实现，纯新增

| # | rule_code | rule_name | 输入 facts | 判断逻辑 | 结论 | 优先级 |
|---|---|---|---|---|---|---|
| E1 | `END_VAL_001` | 保单状态校验 | policy.status | 只有有效/中止状态可申请保全 | PASS/FAIL: "保单当前状态不允许保全操作" | MUST |
| E2 | `END_VAL_002` | 犹豫期退保校验 | policy.issue_date, endorsement.type | 犹豫期内退保 → 全额退 | PASS + refund_type | SHOULD |
| E3 | `END_CALC_001` | 退保金计算 | policy.paid_premiums, policy.duration, product.surrender_rate | surrender = paid × rate(duration) | surrender_value | SHOULD |
| E4 | `END_VAL_003` | 加保核保 | endorsement.new_sum_insured, existing_risk | 加保后累计保额 → 触发核保规则 | PASS/MANUAL: "加保需重新核保" | SHOULD |
| E5 | `END_CALC_002` | 补退保费计算 | old_sum_insured, new_sum_insured, rate | 加保补交/减保退还 | additional_premium | SHOULD |
| E6 | `END_VAL_004` | 保单借款限额 | policy.cash_value, product.loan_ratio | 借款 ≤ 现金价值 × 比例 | PASS/FAIL: "借款金额超过限额" | SHOULD |

---

### 2.7 规则域 6：`commission` — 佣金计算（SHOULD）

> **类型**：计算类
> **触发时机**：出单后 / 月度结算
> **当前状态**：已有硬编码逻辑

| # | rule_code | rule_name | 输入 facts | 计算逻辑 | 输出 | 优先级 |
|---|---|---|---|---|---|---|
| COM1 | `COM_FIRST_001` | 首年佣金计算 | policy.total_premium, channel_product.commission_rate | commission = premium × rate (首年) | first_year_commission | MUST |
| COM2 | `COM_RENEW_001` | 续期佣金计算 | renewal_premium, channel_product.renewal_rate | commission = premium × rate (续期) | renewal_commission | MUST |
| COM3 | `COM_VAL_001` | 渠道资质校验 | channel.status, channel_product.is_active | 渠道必须为已签约且产品授权有效 | PASS/FAIL: "渠道未签约或产品未授权" | MUST |
| COM4 | `COM_CALC_001` | 阶梯佣金 | monthly_total, commission_tiers | 按月度总量阶梯计算 | tier_commission | SHOULD |
| COM5 | `COM_VAL_002` | 退保扣回 | policy.status (退保), original_commission | 犹豫期后退保 → 扣回已发佣金 | clawback_amount | SHOULD |
| COM6 | `COM_TAX_001` | 佣金税费计算 | commission, tax_rate | tax = commission × tax_rate | tax_amount | COULD |
| COM7 | `COM_SETTLE_001` | 结算状态流转 | commission.status, settlement_date | 待确认→已确认→已结算 | new_status | MUST |

---

## 三、规则层级与匹配优先级

### 3.1 三级层级

```
匹配顺序（高优先级覆盖低优先级）：

  PRODUCT（产品级）     ← 最具体，优先级最高
       ↑
  PRODUCT_TYPE（产品类型级） ← 中间层
       ↑
  COMMON（公共级）       ← 最通用，优先级最低
```

### 3.2 匹配算法

```
1. 先查 PRODUCT 级规则（product_id 精确匹配）
2. 再查 PRODUCT_TYPE 级规则（product_type 匹配）
3. 最后查 COMMON 级规则（scope_type = COMMON）
4. 合并所有匹配到的规则，按 priority 排序
5. 同一 rule_code 有多条时，取高优先级（scope 更具体的）
6. 渠道维度作为附加过滤条件
```

### 3.3 渠道维度

```
同一 rule_code 在不同渠道可有不同参数：

示例：UW_CUMUL_001（累计保额上限）
  ├── 互联网渠道(channel=6)：上限 30万
  ├── 银保渠道(channel=4)：上限 100万
  └── 默认：上限 50万

匹配：先按 product+channel 查 → 无则按 product 默认 → 无则按 product_type → 无则按 COMMON
```

---

## 四、规则分类矩阵

| 规则类型 | 含义 | 规则域 | 评估方式 |
|---|---|---|---|
| **校验类** | 通过/不通过 | validation, claim(部分) | 所有条件满足 → PASS，任一不满足 → FAIL |
| **决策类** | 多结论选择 | underwriting, claim(部分) | 按 priority 逐行匹配，首个全匹配的行的 action 为结论 |
| **计算类** | 数值输出 | premium, commission, claim(计算) | 匹配后执行 expression 计算 |
| **混合类** | 校验+决策+计算 | endorsement | 先校验→再决策→最后计算 |

---

## 五、实施优先级

| 阶段 | 规则域 | 规则数 | 对应 P 期 | 预估 |
|---|---|---|---|---|
| **阶段 1** | validation + underwriting | 16 条 | P2.5 (RE-3) | 2 天 |
| **阶段 2** | premium + claim | 13 条 | P2.5 (RE-3/RE-7) | 2 天 |
| **阶段 3** | endorsement + commission | 13 条 | P2.5 (RE-7) | 1 天 |
| **合计** | 6 个域 | **42 条** | | 5 天 |

> 阶段 1 完成后即可替代现有 8 个硬编码组件（5 个 validate + 3 个 uw）
> 阶段 2 完成后替代 5 个 claim 组件
> 阶段 3 为新增能力

---

## 六、验收标准

### 6.1 统一 API 验收

```
Scenario: 传入完整保单 JSON 进行核保评估
Given 规则引擎已启动且规则表已加载
When POST /api/rule/evaluate
  rule_domain = "underwriting"
  facts 包含完整客户+保单信息
Then 返回 conclusion = STANDARD/LOADING/REJECT 之一
And 返回 matched_rules 数组，每条包含 rule_code/result/message
And 返回 failed_count 和 passed_count
And 响应时间 < 5ms（不含网络）
```

### 6.2 规则提示验收

```
Scenario: 违反规则时返回可读提示
Given 被保人 BMI = 36
When 核保评估
Then conclusion = "REJECT"
And matched_rules 中包含：
  rule_code = "UW_HEALTH_001"
  message = "BMI值36超出正常范围(15-28)，严重肥胖，不予承保"
  suggestion = "无法承保，建议客户选择其他产品"
```

### 6.3 层级覆盖验收

```
Scenario: 产品级规则覆盖公共规则
Given COMMON 级配置累计保额上限 = 50万
And PRODUCT 级配置该产品上限 = 100万
When 评估该产品的投保单
Then 使用 100万 作为上限（产品级覆盖公共级）
```

---

## 七、数据架构与冗余设计

### 7.1 核心原则：规则引擎不查业务表

规则引擎与业务数据库完全解耦。评估时所需的数据全部由调用方通过 `facts` JSON 传入，规则引擎**不直接查询** ins_product / ins_customer / ins_policy 等业务表。

```
┌─────────────┐                ┌─────────────────┐
│ covex-web   │  组装 facts    │ covex-rule-     │
│ (业务服务)  │───────────────→│ engine          │
│             │  JSON          │                 │
│ 查业务表:    │                │ 查规则表:        │
│ ins_product │                │ ins_rule_table  │
│ ins_customer│                │ ins_rule_row    │
│ ins_policy  │                │                 │
│ ins_channel │                │ 查 Redis:       │
│             │                │ 规则缓存         │
└─────────────┘                └─────────────────┘
```

### 7.2 哪些字段需要冗余？

**规则表（ins_rule_table）上的冗余字段 — 已设计，不需要再加：**

| 字段 | 用途 | 是否冗余 | 说明 |
|---|---|---|---|
| `rule_domain` | 规则分类 | 否 | 核心分类字段，必须独立列 |
| `scope_type` | 层级匹配 | 否 | 核心匹配字段 |
| `product_type` | 产品类型级匹配 | 是（冗余） | 但必须独立列，用于 SQL 快速筛选 |
| `product_id` | 产品级匹配 | 是（冗余） | 同上 |
| `channel_id` | 渠道维度匹配 | 是（冗余） | 同上 |

**结论**：规则表上的 `product_type`、`product_id`、`channel_id` 就是冗余字段，用于快速定位规则。这是**正确的冗余**，因为：
- 规则数量可能很大，每次评估都要先筛选出匹配的规则
- 没有这些冗余列，就需要 JOIN 业务表或解析 JSON，性能不可接受
- 规则表是“配置数据”，不是“业务数据”，冗余成本很低

### 7.3 业务表不需要为规则引擎新增列

**核心决策：不修改业务表结构来适配规则引擎。**

原因：
1. `facts` JSON 已经是“动态冗余”——调用方把需要的字段都拍平传入
2. 新增规则场景时，只需在 `facts` 中增加字段，不改表结构
3. 业务表的列增加会导致频繁 DDL，违反“规则变更零代码”原则

**示例**：如果未来要新增“宠物类型”核保规则：
- ✘ 错误做法：给 ins_customer 加 `pet_type` 列
- ✔ 正确做法：covex-web 从 ins_customer.attributes(JSON) 中取出 pet_type，放入 facts

### 7.4 facts 组装职责（covex-web 侧）

covex-web 在调用规则引擎前，负责将业务数据组装为 facts JSON：

```java
// covex-web 中的 FactAssembler 服务
public class FactAssembler {

    public Map<String, Object> assembleUnderwritingFacts(
            Long productId, Long customerId, Long channelId) {
        
        // 1. 从业务表查询数据（可走 Redis 缓存）
        Product product = productService.getById(productId);
        Customer customer = customerService.getById(customerId);
        Channel channel = channelService.getById(channelId);
        
        // 2. 组装为扁平化 facts
        Map<String, Object> facts = new HashMap<>();
        facts.put("applicant", buildApplicantMap(customer));
        facts.put("insured", buildInsuredMap(customer));
        facts.put("policy", buildPolicyMap(product));
        facts.put("existing_policies", policyService.getRiskSummary(customerId));
        
        return facts;
    }
}
```

### 7.5 数据流全景

```
投保单提交
  │
  ├─ covex-web: ProposalService.submit()
  │   │
  │   ├─ 1. 保存投保单到 ins_proposal
  │   ├─ 2. FactAssembler.assembleUnderwritingFacts()
  │   │     ├─ 查 ins_product → 产品属性
  │   │     ├─ 查 ins_customer → 客户信息
  │   │     ├─ 查 ins_policy → 已有保单（累计保额）
  │   │     └─ 组装 facts JSON
  │   │
  │   ├─ 3. POST http://covex-rule-engine:8081/api/rule/evaluate
  │   │     { rule_domain: "underwriting", product_id, channel_id, facts }
  │   │
  │   ├─ 4. 规则引擎处理：
  │   │     ├─ 按 rule_domain + scope 从 Redis/MySQL 加载规则
  │   │     ├─ 逐条用 facts 评估
  │   │     └─ 返回 conclusion + matched_rules
  │   │
  │   └─ 5. 根据结论流转：
  │         ├─ PASS → 进入保费计算
  │         ├─ LOADING → 加费后进入保费计算
  │         ├─ MANUAL → 转入人工核保工作台
  │         └─ REJECT → 投保单拒绝
```

### 7.6 特殊场景：规则需要“跨实体”数据

某些规则需要关联多个实体的数据，例如：
- “累计保额”需要查该客户的所有已有保单
- “同家庭保单数”需要查家庭成员

**处理方式**：由 FactAssembler 预计算后放入 facts，而不是规则引擎去查。

```json
{
  "facts": {
    "existing_policies": {
      "total_risk_amount": 300000,
      "policy_count": 1,
      "family_member_count": 3
    }
  }
}
```

这样规则引擎只看到 `facts.existing_policies.total_risk_amount`，不需要知道“累计保额”是怎么算出来的。

### 7.7 总结：冗余策略

| 位置 | 冗余策略 | 理由 |
|---|---|---|
| **ins_rule_table** | 冗余 `product_type`/`product_id`/`channel_id` | 快速筛选规则，配置数据冗余成本低 |
| **ins_rule_row** | conditions/actions 存 JSON | 灵活表达任意规则，不固化列 |
| **业务表** | **不新增列** | 通过 facts JSON 动态传递，新增规则不改表 |
| **facts JSON** | 调用方组装的“动态冗余” | 包含评估所需的全部数据，跨实体数据预计算 |
| **Redis** | 规则缓存 + 产品/费率缓存 | 热数据双缓存，规则引擎不查业务库 |

---

## 八、规则版本追溯需求

### 8.1 核心需求

规则是“活”的配置，会不断修改。系统必须支持：

1. **全量历史保留**：每条旧规则逻辑删除（is_deleted=1），不物理删除，永远可追溯
2. **双字段定位**：`version`（第几版）+ `requirement_no`（哪个需求）多维度定位
3. **需求号可重复**：同一需求可能多次修改规则（改错了再改），requirement_no 相同
4. **发布快照**：每次发布生成完整快照，可回看任意历史版本的规则全貌
5. **前端可追溯**：两个按钮查看历史

### 8.2 数据模型要求

| 表 | 新增字段 | 说明 |
|---|---|---|
| `ins_rule_table` | `requirement_no VARCHAR(50)` | 需求号，可重复，如 REQ-2026-001 |
| `ins_rule_row` | `version INT DEFAULT 1` | 行版本号，修改时旧行逻辑删除，新行 version+1 |
| `ins_rule_row` | `requirement_no VARCHAR(50)` | 需求号，继承自规则表 |
| `ins_rule_row` | `rule_code VARCHAR(50)` | 规则编码，同一 rule_code 可有多行（不同条件分支） |
| `ins_rule_snapshot` | **新表** | 每次发布一条快照，含完整规则数据 JSON + 版本注释 |

> **性能优化**：`ins_rule_snapshot.comment`（版本注释）为 TEXT 类型，默认不随版本列表加载。只有用户点击具体版本详情时才懒加载，避免大文本字段拖慢列表查询。

### 8.3 逻辑删除规则

```
修改规则行时：
  1. 旧行 is_deleted → 1, deleted_at → NOW()    ← 保留，不物理删除
  2. 新行 is_deleted → 0, version = 旧行.version + 1  ← 继承 rule_code, rule_table_id

查询当前有效规则：WHERE is_deleted = 0
查询规则行全部历史：WHERE rule_table_id = X AND rule_code = Y（含 is_deleted=1）
```

### 8.4 发布快照规则

```
点击“发布”时：
  1. ins_rule_table.version +1, status → 已发布
  2. 生成 ins_rule_snapshot 记录：
     - version = 当前版本号
     - requirement_no = 当前需求号
     - snapshot_data = { table: {...}, rows: [...] }  完整快照
     - change_summary = 变更说明（用户填写）
     - comment = 版本注释（评审意见、修改原因等，可选，按需加载）
     - published_by = 发布人
```

### 8.5 前端追溯功能需求

| 按钮 | 位置 | 功能 |
|---|---|---|
| **[📜 历史版本]** | 规则配置中心工具栏 | 打开版本追溯弹窗，显示该规则表的所有发布快照，可只读查看任意版本，可对比两个版本差异 |
| **[📦 产品规则]** | 规则配置中心工具栏 | 下拉选择产品，显示该产品当前生效的规则，可点击“查看旧版”查看该产品的历史快照 |

**版本追溯弹窗（RuleHistoryDialog）**：
- 左侧：版本列表（v1, v2, v3...）+ 需求号 + 发布时间 + 发布人 + 变更说明
- 右侧：选中版本的快照内容（只读决策表）
- 底部：[对比当前版本] → 打开 VersionDiffViewer（类 Git diff，显示新增/修改/删除的规则行）

**产品规则视图**：
- 下拉选择产品 → 显示该产品当前生效的所有规则表 + 规则行
- [查看旧版] → 该产品关联的规则快照列表

### 8.6 版本追溯 API

| API | 说明 |
|---|---|
| `GET /api/rule/table/{id}/versions` | 版本历史列表（快照元数据，**不含 comment**） |
| `GET /api/rule/table/{id}/versions/{version}` | 指定版本的快照详情（完整规则数据 + comment） |
| `GET /api/rule/table/{id}/versions/{version}/comment` | 单独获取版本注释（懒加载，不随列表加载） |
| `GET /api/rule/table/{id}/versions/diff?v1=1&v2=3` | 两个版本差异对比 |
| `GET /api/rule/product/{productId}/rules` | 当前产品的生效规则 |
| `GET /api/rule/product/{productId}/rule-history` | 当前产品的历史版本快照 |
| `GET /api/rule/requirement/{requirementNo}` | 按需求号查询关联的规则变更 |
| `GET /api/rule/row/{id}/history` | 某条规则行的全部历史（含逻辑删除的旧行） |

### 8.7 验收场景

```
Scenario 1：修改规则后查看历史
Given 规则表 UW_AGE_CHECK 当前为 v2（已发布）
When 修改年龄上限从 60 → 65，发布为 v3
Then [📜 历史版本] 弹窗显示 v1, v2, v3 三个版本
And v2 快照中年龄上限 = 60（只读）
And v3 快照中年龄上限 = 65
And 对比视图显示：年龄上限 60 → 65（高亮差异）

Scenario 2：同一需求多次修改
Given 需求 REQ-2026-001 创建了 UW_AGE_CHECK v1
When 发现规则写错了，修改后发布为 v2（requirement_no 仍为 REQ-2026-001）
Then 按需求号查询 REQ-2026-001 → 返回 v1 + v2 两个版本
And v1 的旧行 is_deleted=1，可查到完整历史

Scenario 3：查看产品规则旧版
Given 产品 A 关联了 3 张规则表，均已发布
When 点击 [📦 产品规则] → 选择产品 A
Then 显示 3 张规则表的当前生效规则
And 点击 [查看旧版] → 显示这 3 张表的历史快照列表
```
