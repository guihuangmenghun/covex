# Covex 数据模型（v5 - 生产就绪）

> **设计理念**：产品配置表只存"是什么"，不存"怎么算"。
> 流程编排由 **LiteFlow** 承载，表达式计算由 **Aviator** 承载，业务决策用 Java 组件实现。

---

## 一、表结构（11 张表）

> 所有表均包含 `tenant_id`（租户隔离）、`is_deleted` + `deleted_at`（软删除）、审计字段。

### 1. `ins_product` — 产品主表

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_code` | VARCHAR(20) | 产品编码，租户内唯一 |
| `version` | VARCHAR(20) | 版本号（如 1.0.0） |
| `version_status` | TINYINT | 版本状态：1-草稿，2-待审批，3-已发布，4-已冻结，5-审批驳回 |
| `product_name` | VARCHAR(120) | 产品全称 |
| `short_name` | VARCHAR(60) | 产品简称 |
| `product_type` | TINYINT | 产品分类：1-寿险，2-意外险，3-健康险，4-车险，5-财产险，6-责任险，7-乘务险 |
| `product_nature` | TINYINT | 产品性质：1-个人，2-团体，3-银行代理，4-综合 |
| `term_type` | TINYINT | 期限类型：1-长期，2-一年期，3-极短期 |
| `main_rider_flag` | TINYINT | 主附险：1-主险，2-附加险，3-两者皆可 |
| `sale_channel` | JSON | 销售渠道数组：["1","2","3"] |
| `start_date` | DATE | 开办日期 |
| `end_date` | DATE | 停办日期 |
| `status` | TINYINT | 销售状态：1-未上架，2-已上架，3-已下架 |
| `capabilities` | JSON | 产品能力声明 |
| `attributes` | JSON | 产品扩展属性（按险种不同） |
| `parent_version_id` | BIGINT | 来源版本ID（版本克隆时记录） |
| `is_deleted` | TINYINT(1) | 软删除：0-正常，1-已删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

> **唯一索引**：`UNIQUE(tenant_id, product_code, version)`

**版本管理机制**：
- 新产品创建 → `version_status=1(草稿)`
- 提交审批 → `version_status=2(待审批)`
- 审批通过 → `version_status=3(已发布)`，`status` 可设为已上架
- 产品升级 → 克隆当前记录，新记录 `parent_version_id=旧ID`，`version` 递增，`version_status=1(草稿)`
- 老版本 → `version_status=4(已冻结)`，不可修改，已有保单继续引用

---

### 2. `ins_product_coverage` — 保障定义

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_id` | BIGINT | 关联 ins_product.id |
| `coverage_code` | VARCHAR(20) | 责任编码（产品内唯一） |
| `coverage_name` | VARCHAR(100) | 责任名称 |
| `selection_mode` | TINYINT | 选择方式：1-必选，2-可选 |
| `benefit_type` | TINYINT | 给付类型：1-生存金，2-满期金，3-年金，4-理赔金，5-津贴，6-费用补偿，7-定额给付 |
| `coverage_detail` | JSON | 保障详细属性（按险种不同） |
| `sort_order` | INT | 排序 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

---

### 3. `ins_product_premium` — 缴费规则

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_id` | BIGINT | 关联 ins_product.id |
| `premium_plan_code` | VARCHAR(20) | 缴费计划编码 |
| `premium_plan_name` | VARCHAR(60) | 缴费计划名称 |
| `payment_frequency` | TINYINT | 缴费频率：0-趸交，1-月交，3-季交，6-半年交，12-年交，99-不定期 |
| `payment_term` | SMALLINT | 缴费期间 |
| `payment_term_unit` | TINYINT | 期间单位：1-年，2-月，3-日 |
| `grace_period` | SMALLINT | 宽限天数 |
| `rounding_mode` | TINYINT | 取整方式：1-四舍五入，2-截断，3-进位 |
| `premium_detail` | JSON | 缴费扩展属性 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

---

### 4. `ins_coverage_premium_rel` — 责任-缴费关联

解决 coverage 和 premium 多对多的关系（一个责任可对应多个缴费计划，一个缴费计划可覆盖多个责任）。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `coverage_id` | BIGINT | 关联 ins_product_coverage.id |
| `premium_id` | BIGINT | 关联 ins_product_premium.id |
| `is_deleted` | TINYINT(1) | 软删除 |
| `created_at` | DATETIME | 创建时间 |

> **唯一索引**：`UNIQUE(tenant_id, coverage_id, premium_id)`

---

### 5. `ins_product_rule` — 规则引用

关联 LiteFlow 规则链 / Aviator 表达式。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_id` | BIGINT | 关联 ins_product.id |
| `coverage_id` | BIGINT | 关联 ins_product_coverage.id（可空，空表示产品级规则） |
| `rule_type` | TINYINT | 规则类型：1-核保，2-校验，3-保全，4-退保，5-费率计算，6-给付计算，7-理赔 |
| `rule_engine` | VARCHAR(20) | 引擎类型：`liteflow`（流程链）、`aviator`（表达式）、`java`（Java组件） |
| `rule_code` | VARCHAR(100) | LiteFlow 链名称 或 Aviator 表达式ID 或 Java 组件名 |
| `rule_name` | VARCHAR(100) | 规则名称（便于阅读） |
| `sort_order` | INT | 执行顺序 |
| `is_active` | TINYINT(1) | 是否启用 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

---

### 6. `ins_product_rider_rel` — 主附险关联

定义哪些附加险可以挂在哪些主险下面。寿险场景刚需：重疾险（主险）可挂住院医疗（附险），但不是所有附险都能挂所有主险。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `main_product_code` | VARCHAR(20) | 主险产品编码 |
| `rider_product_code` | VARCHAR(20) | 附加险产品编码 |
| `max_rider_count` | SMALLINT | 该主险下最多可挂附加险数量（可空=不限） |
| `is_active` | TINYINT(1) | 是否启用 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_at` | DATETIME | 创建时间 |

> **唯一索引**：`UNIQUE(tenant_id, main_product_code, rider_product_code)`

---

### 7. `ins_product_document` — 条款文档

管理产品关联的条款、费率表、投保须知等文档。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_id` | BIGINT | 关联 ins_product.id |
| `document_type` | TINYINT | 文档类型：1-产品条款，2-费率表，3-投保须知，4-产品说明书 |
| `document_name` | VARCHAR(120) | 文档名称 |
| `file_url` | VARCHAR(500) | 文件存储路径/URL |
| `version` | VARCHAR(20) | 文档版本 |
| `effective_date` | DATE | 生效日期 |
| `expiry_date` | DATE | 失效日期 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `created_at` | DATETIME | 创建时间 |

---

### 8. `ins_product_changelog` — 变更日志

记录产品配置的每次变更，满足审计和监管要求。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `product_id` | BIGINT | 关联 ins_product.id |
| `change_type` | TINYINT | 变更类型：1-创建，2-修改，3-发布，4-冻结，5-下架，6-删除 |
| `change_target` | VARCHAR(50) | 变更对象：product / coverage / premium / rule / document |
| `change_target_id` | BIGINT | 变更对象ID |
| `field_name` | VARCHAR(50) | 变更字段名（修改时） |
| `old_value` | TEXT | 变更前值 |
| `new_value` | TEXT | 变更后值 |
| `operator` | VARCHAR(50) | 操作人 |
| `operated_at` | DATETIME | 操作时间 |
| `remark` | VARCHAR(200) | 备注 |

> **索引**：`INDEX(tenant_id, product_id, operated_at)`

---

### 9. `ins_rate_table` — 费率表元数据

定义费率表的结构、版本和生效期间。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID |
| `rate_table_code` | VARCHAR(30) | 费率表编码 |
| `rate_table_name` | VARCHAR(100) | 费率表名称 |
| `product_id` | BIGINT | 关联产品（可空，公共费率表为空） |
| `version` | VARCHAR(20) | 费率表版本 |
| `table_schema` | JSON | 维度定义（见下方示例） |
| `effective_date` | DATE | 生效日期 |
| `expiry_date` | DATE | 失效日期 |
| `is_deleted` | TINYINT(1) | 软删除 |
| `deleted_at` | DATETIME | 删除时间 |
| `created_by` | VARCHAR(50) | 创建人 |
| `created_at` | DATETIME | 创建时间 |

**table_schema 示例**（寿险费率表）：
```json
{
  "dimensions": [
    {"name": "age", "type": "int", "label": "年龄"},
    {"name": "gender", "type": "enum", "label": "性别", "values": [1, 2]},
    {"name": "term", "type": "int", "label": "期限"}
  ],
  "metrics": [
    {"name": "rate", "type": "decimal", "label": "费率", "unit": "元/千元保额"}
  ]
}
```

### 10. `ins_rate_table_row` — 费率表行数据

每行存储一条费率记录，独立查询和维护。

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `rate_table_id` | BIGINT | 关联 ins_rate_table.id |
| `dimension_key` | VARCHAR(100) | 维度组合键（如 `age:30|gender:1|term:20`） |
| `dimension_json` | JSON | 维度原始值（如 `{"age":30,"gender":1,"term":20}`） |
| `rate_value` | DECIMAL(16,6) | 费率值 |
| `extra_values` | JSON | 其他指标值（当 table_schema 有多个 metrics 时使用） |

> **注意**：此表不包含 `tenant_id` 字段。多租户隔离由父表 `ins_rate_table`（含 `tenant_id`）保障——费率行数据始终通过 `rate_table_id` 关联查询，不会独立按租户过滤。`MybatisPlusConfig.IGNORE_TABLES` 已将此表加入多租户拦截器忽略列表。

> **索引**：`INDEX(rate_table_id, dimension_key)` — 支持快速查找

**Redis 缓存策略**：

```
# 缓存结构：Hash
# Key: ins:rate:{rate_table_code}:{version}
# Field: dimension_key → rate_value

# 示例：
HSET ins:rate:RT_7235_01:1.0.0 "age:20|gender:1|term:20" "3.50"
HSET ins:rate:RT_7235_01:1.0.0 "age:20|gender:2|term:20" "2.80"
HSET ins:rate:RT_7235_01:1.0.0 "age:30|gender:1|term:20" "5.20"

# 查询：Aviator 表达式中通过自定义函数调用 Redis
# rate = redis.hget("ins:rate:RT_7235_01:1.0.0", "age:" + age + "|gender:" + gender + "|term:" + term)
```

**加载流程**：
1. 费率表发布/更新时 → 从 DB 加载所有行 → 写入 Redis Hash → 设置 TTL（默认 24h）
2. 产品配置引用费率表时 → 先查 Redis → miss 则从 DB 加载并回填缓存
3. 费率表版本切换 → 旧版本 Key 标记过期，新版本 Key 加载

---

### 11. `ins_dict` — 数据字典

| 字段名 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `tenant_id` | BIGINT | 租户ID（0=全局字典） |
| `dict_type` | VARCHAR(50) | 字典类型 |
| `dict_code` | VARCHAR(20) | 字典值 |
| `dict_name` | VARCHAR(100) | 中文含义 |
| `parent_code` | VARCHAR(20) | 父级编码（支持层级字典） |
| `sort_order` | INT | 排序 |
| `is_active` | TINYINT(1) | 是否启用 |
| `remark` | VARCHAR(200) | 备注 |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

---

## 二、规则引擎架构

### 分层设计

| 层 | 引擎 | 用途 | 配置方式 |
|---|---|---|---|
| 流程编排 | **LiteFlow** | 投保链、保全链、理赔链 | EL 表达式存 Nacos |
| 表达式计算 | **Aviator** | 保费、保额、退保金、费率查表 | 表达式存 Nacos |
| 业务决策 | **Java 组件** | 核保结论、校验规则 | LiteFlow 节点组件 |
| 配置中心 | **Nacos** | 规则热加载、EL 链管理 | 项目已有 |

### LiteFlow 链示例

```
// 寿险投保链
THEN(validate, underwrite, calculate_premium, issue_policy, async_notify);

// 车险投保链（无核保）
THEN(validate, query_vehicle, query_ncd, calculate_premium, issue_policy, async_notify);

// 财产险投保链（含询价）
THEN(validate, underwrite, rate_query, calculate_premium, issue_policy, async_notify);
```

### Aviator 表达式示例

```
// 寿险保费 = 保额 × 费率(age, gender, term)
premium = sumInsured * rateTable.get(age, gender, term);

// 车险保费 = 基准保费 × NCD系数 × 车型系数 × 地区系数
autoPremium = basePremium * ncdFactor * vehicleFactor * regionFactor;

// 退保金 = 已交保费 × 退保比例(已交年数)
surrenderValue = totalPaid * surrenderRate(paidYears);
```

---

## 三、各险种 JSON 模板

### 寿险/健康险 — `capabilities`

```json
{
  "underwriting": true,
  "renewal_premium": true,
  "survival_benefit": true,
  "endorsement": true,
  "renewal": false,
  "reinsurance": false,
  "shared_amount": false,
  "policy_loan": true
}
```

### 寿险/健康险 — `attributes`

```json
{
  "life_category": 1,
  "liability_type": 3,
  "health_category": 3,
  "treatment_type": 2,
  "term_length_type": 3,
  "min_applicant_age": 18,
  "max_applicant_age": 60,
  "min_insured_age": 0,
  "max_insured_age": 65,
  "max_maturity_age": 70,
  "multi_insured": false,
  "beneficiary_type": 0,
  "disclosure_required": true,
  "effective_date_mode": 4,
  "accrual_rate": 0.035,
  "amount_sales_mode": 1,
  "unit_amount": 1000,
  "documents": {
    "clause_file": "product_7235_clause_v1.pdf",
    "rate_table_ref": "RT_7235_01"
  }
}
```

### 寿险/健康险 — `coverage_detail`

```json
{
  "coverage_period": 20,
  "coverage_period_unit": 1,
  "benefit_start_term": 0,
  "benefit_start_unit": 1,
  "benefit_interval": 0,
  "increment_flag": false,
  "max_benefit_count": null,
  "gender_linked": 0,
  "count_toward_amount": true,
  "valid_after_death": false,
  "require_application": false,
  "use_shared_amount": false,
  "deductible": 0,
  "deductible_days": 0,
  "observation_period": 90,
  "claim_ratio": 1.0,
  "min_benefit": 0,
  "max_benefit": null,
  "loading": {
    "health": false,
    "occupation": false
  },
  "post_benefit_action": 1
}
```

### 车险 — `capabilities`

```json
{
  "underwriting": false,
  "renewal_premium": true,
  "endorsement": true,
  "renewal": true,
  "ncd_discount": true,
  "claim_history_check": true,
  "vehicle_inspection": false
}
```

### 车险 — `attributes`

```json
{
  "vehicle_types": ["private_car", "commercial_vehicle", "motorcycle"],
  "mandatory_coverage": true,
  "commercial_coverage": true,
  "ncd_max_discount": 0.3,
  "ncd_penalty_rate": 0.1,
  "region_factor_enabled": true,
  "vehicle_age_limit": 15,
  "effective_date_mode": 2
}
```

### 车险 — `coverage_detail`

```json
{
  "auto_coverage_type": "vehicle_damage",
  "deductible": 0,
  "deductible_type": "fixed",
  "third_party_limit": null,
  "per_accident_limit": null,
  "per_person_limit": null,
  "depreciation_rate": 0.006,
  "max_vehicle_age": 15,
  "glass_separate": false,
  "flood_coverage": false,
  "theft_coverage": false,
  "repair_shop_type": "authorized"
}
```

### 财产险 — `capabilities`

```json
{
  "underwriting": true,
  "renewal_premium": true,
  "endorsement": true,
  "renewal": true,
  "co_insurance": true,
  "reinsurance": true,
  "property_valuation": true
}
```

### 财产险 — `attributes`

```json
{
  "property_types": ["building", "machinery", "inventory", "household"],
  "peril_based": true,
  "valuation_method": "replacement_cost",
  "co_insurance_rate": 0.8,
  "sub_limit_enabled": true,
  "region_factor_enabled": true,
  "effective_date_mode": 1
}
```

### 财产险 — `coverage_detail`

```json
{
  "peril_type": "fire",
  "property_type": "building",
  "valuation_method": "replacement_cost",
  "sub_limit": null,
  "deductible_type": "percentage",
  "deductible_rate": 0.05,
  "deductible_amount": 5000,
  "business_interruption": false,
  "bi_indemnity_period": null,
  "extra_expense": false
}
```

### 乘务险 — `capabilities`

```json
{
  "underwriting": false,
  "renewal_premium": true,
  "endorsement": true,
  "renewal": true,
  "passenger_count_based": true,
  "route_based": false
}
```

### 乘务险 — `attributes`

```json
{
  "transport_types": ["bus", "taxi", "train", "ship", "aircraft"],
  "passenger_count_based": true,
  "per_seat_pricing": true,
  "route_based": false,
  "effective_date_mode": 1
}
```

### 乘务险 — `coverage_detail`

```json
{
  "transport_type": "bus",
  "per_passenger_limit": 500000,
  "per_accident_limit": 5000000,
  "medical_expense_limit": 50000,
  "medical_expense_ratio": 0.1,
  "disability_coverage": true,
  "death_coverage": true,
  "baggage_limit": 5000,
  "delay_compensation": false
}
```

---

## 四、capabilities 能力编码汇总

| 能力编码 | 名称 | 适用品种 |
|---|---|---|
| `underwriting` | 核保 | 寿险、健康险、财产险 |
| `renewal_premium` | 续期收费 | 全部 |
| `survival_benefit` | 生存给付 | 寿险 |
| `endorsement` | 保全 | 全部 |
| `renewal` | 续保 | 车险、财产险、乘务险 |
| `reinsurance` | 再保险 | 财产险 |
| `shared_amount` | 公共保额 | 寿险 |
| `policy_loan` | 保单借款 | 寿险（有现金价值） |
| `ncd_discount` | 无赔优待 | 车险 |
| `claim_history_check` | 历史赔付查询 | 车险 |
| `vehicle_inspection` | 验车 | 车险 |
| `co_insurance` | 共保 | 财产险 |
| `property_valuation` | 标的估值 | 财产险 |
| `passenger_count_based` | 按人数计费 | 乘务险 |
| `route_based` | 按路线计费 | 乘务险 |

---

## 五、车险/财产险/乘务险 编码参考

### 车险险别
| 编码 | 险别 |
|---|---|
| `compulsory` | 交强险 |
| `vehicle_damage` | 车损险 |
| `third_party` | 第三者责任险 |
| `passenger_liability` | 车上人员责任险 |
| `theft` | 全车盗抢险 |
| `glass` | 玻璃单独破碎险 |
| `spontaneous_combustion` | 自燃损失险 |
| `flood` | 涉水险 |
| `scratch` | 车身划痕险 |

### 财产险灾因
| 编码 | 灾因 |
|---|---|
| `fire` | 火灾 |
| `explosion` | 爆炸 |
| `lightning` | 雷击 |
| `storm` | 暴风暴雨 |
| `flood` | 洪水 |
| `earthquake` | 地震 |
| `theft` | 盗窃 |
| `water_damage` | 水暖管爆裂 |
| `impact` | 外界物体倒塌/碰撞 |

### 乘务险运输类型
| 编码 | 运输类型 |
|---|---|
| `bus` | 公路客运 |
| `taxi` | 出租车 |
| `train` | 铁路客运 |
| `ship` | 水路客运 |
| `aircraft` | 航空客运 |

---

## 六、设计注释

### 1. 架构演进
- v1-v3：原型阶段 30 张表，字段规范化清理
- v4：重构为 5 张表 + JSON + Drools
- v5（本版）：5→11 张表，修复 18 个问题，规则引擎改为 LiteFlow + Aviator
- 新增表：coverage_premium_rel、product_rider_rel、product_document、product_changelog、rate_table（元数据）、rate_table_row（行数据+Redis缓存）

### 2. v5 修复清单
| # | 问题 | 修复方式 |
|---|---|---|
| 1 | 规则引擎全是 Drools | 全部替换为 LiteFlow + Aviator |
| 2 | 无版本冻结机制 | 新增 `version_status` 字段 + `parent_version_id` + 版本管理说明 |
| 3 | coverage 和 premium 无关联 | 新增 `ins_coverage_premium_rel` 关联表 |
| 4 | loading_type 旧编码 | 改为 JSON `{"health":false,"occupation":false}` |
| 5 | sale_channel 逗号分隔 | 改为 JSON 数组 |
| 6 | 无审计表 | 新增 `ins_product_changelog` |
| 7 | 无审批状态 | version_status 增加"待审批"和"审批驳回" |
| 8 | 无软删除 | 所有表增加 `is_deleted` + `deleted_at` |
| 9 | 无租户隔离 | 所有表增加 `tenant_id` |
| 10 | 无文档管理 | 新增 `ins_product_document` |
| 11 | capabilities/attributes 重叠 | 删除 attributes 中的重复字段，只在 capabilities 声明 |
| 12 | 子表缺审计字段 | coverage、premium、rule 表增加审计字段 |
| 13 | 字典表缺层级 | 新增 `parent_code` 字段 |
| 14 | 费率表结构缺失 | 新增 `ins_rate_table` |
| 15 | rule_code 指向 Drools | 新增 `rule_engine` 字段，区分 liteflow/aviator/java |
| 16 | ins_dict 缺审计字段 | 增加 created_by/updated_by/created_at/updated_at |
| 17 | 主附险关联缺失 | 新增 `ins_product_rider_rel` 关联表 |
| 18 | 费率表大数据量性能 | 拆为 rate_table（元数据）+ rate_table_row（行数据）+ Redis Hash 缓存 |

### 3. JSON vs 关系表边界
- **关系表**：需要独立 CRUD、独立生命周期、被多方引用的实体
- **JSON**：依附于父实体的属性集，不需要独立查询
- **关联表**：多对多关系的 junction（coverage ↔ premium）

### 4. 险种扩展方式
- 新增险种不需要建新表：字典加值 + JSON 模板 + LiteFlow 链 + Aviator 表达式
- 示例：宠物险 = product_type 加"8" + 宠物属性 JSON + 对应 LiteFlow 投保链

### 5. 后续待建表（不在产品配置域）
- 客户表（投保人/被保人/受益人）
- 权限分级表（角色/权限/用户）
- 渠道商表（代理公司/经纪人/银行渠道）
- 保单主表（保单基本信息/状态/生效日）
- 保单险种明细表（保单关联的产品+责任+保额+保费）
- 理赔表
- 保全变更表

### 6. 生产环境注意事项

> 以下问题在设计阶段可暂不处理，开发/上线时需关注。

**6.1 changelog 字段容量**
- `old_value` / `new_value` 为 TEXT 类型（最大 64KB）
- 如果记录整个 JSON 字段变更（如 capabilities/attributes），可能超限
- 建议改为 MEDIUMTEXT（最大 16MB），或在应用层对 JSON diff 做精简记录

**6.2 费率表版本管理**
- 当前 `ins_rate_table` 已有 version 字段，但无 version_status
- 如需多版本并行生效（如新旧费率表切换期间），建议增加 `version_status` 字段
- 或采用"新增不修改"策略：费率表更新时新建一条记录，旧的设置 `expiry_date`
