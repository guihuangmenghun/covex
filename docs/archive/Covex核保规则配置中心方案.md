# Covex 核保规则配置中心 — 技术方案

> ⚠️ **已废弃** — 本方案已被 `Covex规则引擎服务方案.md` v2.2（独立微服务架构）完全替代。
> 仅供历史参考，请勿按本文档执行开发。

> **版本**：v1.0（已废弃）
> **日期**：2026-07-06
> **目标用户**：核保老师（业务人员，非开发者）
> **核心能力**：可视化配置核保规则 + 热更新 + 分级管理 + 实时 API 决策

---

## 一、系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    核保规则配置中心（前端）                      │
│  ┌──────────┐ ┌──────────────┐ ┌──────────┐ ┌───────────┐  │
│  │ 规则树    │ │ 决策表编辑器  │ │ 规则测试  │ │ 发布管理   │  │
│  │ (左侧导航)│ │ (类Excel)    │ │ (模拟输入)│ │ (版本对比) │  │
│  └──────────┘ └──────────────┘ └──────────┘ └───────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │ REST API
┌────────────────────────┴────────────────────────────────────┐
│                  RuleEngineService（后端核心）                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ RuleTableMgr │  │ RuleRowMgr   │  │ RuleEvaluator    │  │
│  │ (表管理)     │  │ (行管理)     │  │ (Aviator 求值)   │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ RuleCache    │  │ RulePublisher│                        │
│  │ (Redis缓存)  │  │ (热更新发布) │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
           │                              │
    ┌──────┴──────┐               ┌───────┴───────┐
    │   MySQL     │               │    Redis      │
    │ ins_rule_*  │               │ 规则缓存+Pub  │
    └─────────────┘               └───────────────┘
```

**零新组件依赖**：全部基于现有 Aviator + Redis + MySQL + Spring Boot。

---

## 二、规则分级体系

### 2.1 三级规则层级

```
优先级从低到高（高优先级覆盖低优先级）：

Level 1: COMMON（公共规则）
├── 适用于所有产品、所有渠道
├── 例：投保人年龄 18-70 岁
├── 例：同一被保人累计风险保额 ≤ 500 万
└── 例：职业等级 5-6 级拒保

Level 2: PRODUCT_TYPE（产品类型规则）
├── 按 product_type 区分（寿险/年金险/意外险/财产险）
├── 例：寿险 → 等待期 180 天
├── 例：意外险 → 无等待期
└── 例：年金险 → 最低缴费期 5 年

Level 3: PRODUCT（产品级规则）
├── 绑定具体 product_id
├── 可进一步按 channel_id 区分
├── 例：产品A + 渠道1 → 保额上限 50 万
├── 例：产品A + 渠道2 → 保额上限 100 万
└── 例：产品A + 所有渠道 → 最低保额 10 万
```

### 2.2 渠道维度

```
同一产品不同渠道的规则拆分：

产品A（寿险）
├── 渠道：个险 → 保额上限 100 万，免体检
├── 渠道：银保 → 保额上限 50 万，简化健康告知
├── 渠道：经代 → 保额上限 30 万，标准流程
└── 默认（channel_id=0）→ 保额上限 20 万
```

**匹配优先级**：`产品+渠道 > 产品默认 > 产品类型 > 公共`

---

## 三、数据模型

### 3.1 规则表定义

```sql
CREATE TABLE ins_rule_table (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    table_code      VARCHAR(50) NOT NULL COMMENT '唯一编码，如 UW_AGE_CHECK',
    table_name      VARCHAR(100) NOT NULL COMMENT '显示名称，如 年龄校验规则',
    description     VARCHAR(500) COMMENT '规则说明（核保老师填写）',
    
    -- 分级维度
    scope_type      TINYINT NOT NULL COMMENT '1=公共 2=产品类型 3=产品级',
    product_type    VARCHAR(20) COMMENT '产品类型（scope=2时必填）：life/annuity/accident/property',
    product_id      BIGINT COMMENT '产品ID（scope=3时必填）',
    channel_id      BIGINT DEFAULT 0 COMMENT '渠道ID（0=所有渠道）',
    
    -- 输入输出定义
    input_fields    JSON NOT NULL COMMENT '输入字段定义 [{"field":"applicant.age","label":"投保人年龄","type":"number"},...]',
    output_fields   JSON NOT NULL COMMENT '输出字段定义 [{"field":"uw_decision","label":"核保结论","type":"enum"},...]',
    
    -- 版本与状态
    version         INT DEFAULT 1,
    status          TINYINT DEFAULT 1 COMMENT '1=草稿 2=已发布 3=已归档',
    
    -- 审计字段
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1) DEFAULT 0,
    deleted_at      DATETIME,
    
    UNIQUE KEY uk_code_tenant (table_code, tenant_id)
) COMMENT='核保规则表定义';
```

### 3.2 规则行（决策表行）

```sql
CREATE TABLE ins_rule_row (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    rule_table_id   BIGINT NOT NULL COMMENT '关联 ins_rule_table.id',
    row_no          INT NOT NULL COMMENT '行号（决定匹配优先级）',
    priority        INT DEFAULT 100 COMMENT '优先级（数字越小越先匹配）',
    rule_name       VARCHAR(100) COMMENT '规则名称（方便核保老师识别）',
    
    -- 条件与动作（标准化 JSON 格式，可迁移至任意 BRMS）
    conditions      JSON NOT NULL COMMENT '条件表达式',
    actions         JSON NOT NULL COMMENT '动作/结论',
    
    -- 状态
    is_active       TINYINT(1) DEFAULT 1 COMMENT '1=启用 0=停用',
    
    -- 审计字段
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1) DEFAULT 0,
    deleted_at      DATETIME,
    
    KEY idx_table_priority (rule_table_id, priority)
) COMMENT='核保规则行（决策表行）';
```

### 3.3 conditions / actions JSON 格式标准

**conditions 格式**：
```json
{
  "logic": "AND",
  "items": [
    {
      "field": "applicant.age",
      "label": "投保人年龄",
      "operator": ">=",
      "value": 18
    },
    {
      "field": "applicant.age",
      "label": "投保人年龄",
      "operator": "<=",
      "value": 70
    }
  ]
}
```

**支持的 operator**：
| 操作符 | 说明 | 示例 |
|---|---|---|
| `==` | 等于 | `{"field":"gender","operator":"==","value":1}` |
| `!=` | 不等于 | `{"field":"smoking","operator":"!=","value":1}` |
| `>` / `>=` / `<` / `<=` | 数值比较 | `{"field":"bmi","operator":">","value":30}` |
| `in` | 包含于集合 | `{"field":"product_type","operator":"in","value":["life","annuity"]}` |
| `contains` | 字符串包含 | `{"field":"medical_history","operator":"contains","value":"糖尿病"}` |
| `exists` | 字段存在且非空 | `{"field":"health_declaration","operator":"exists","value":true}` |

**actions 格式**：
```json
{
  "uw_decision": "reject",
  "reason": "投保人年龄超出承保范围（18-70岁）",
  "loading_amount": 0,
  "exclusion": "",
  "referral": false
}
```

**uw_decision 枚举值**：
| 值 | 含义 | 后续动作 |
|---|---|---|
| `pass` | 标准体通过 | 继续流程 |
| `loading` | 加费承保 | 附加 loading_amount |
| `exclusion` | 除外责任 | 附加 exclusion 描述 |
| `defer` | 延期 | 通知补充材料 |
| `reject` | 拒保 | 终止流程 |
| `manual` | 转人工 | 进入核保员工作台 |

---

## 四、规则评估引擎

### 4.1 评估 API

```
POST /api/underwriting/evaluate

请求体（投保单 JSON）：
{
  "proposal_id": 23,
  "product_id": 13,
  "product_type": "life",
  "channel_id": 1,
  "applicant": {
    "id": 5,
    "name": "张三",
    "age": 35,
    "gender": 1,
    "id_type": "id_card"
  },
  "insured": {
    "id": 5,
    "name": "张三",
    "age": 35,
    "gender": 1,
    "occupation_risk_level": 2,
    "bmi": 23.5,
    "smoking_status": 0,
    "medical_history": {}
  },
  "sum_insured": 500000,
  "health_declaration": [
    {"question": "Q1", "answer": false},
    {"question": "Q2", "answer": false}
  ]
}

响应体（核保结论）：
{
  "decision": "loading",
  "decision_label": "加费承保",
  "reason": "BMI 28.5 超重，加费 500 元/年",
  "loading_amount": 500.00,
  "exclusion": "",
  "referral": false,
  "matched_rules": [
    {
      "table_code": "UW_BMI_CHECK",
      "table_name": "BMI 校验",
      "row_no": 3,
      "rule_name": "BMI 25-30 加费",
      "decision": "loading",
      "reason": "BMI 28.5 超重"
    },
    {
      "table_code": "UW_AGE_CHECK",
      "table_name": "年龄校验",
      "row_no": 1,
      "rule_name": "标准承保年龄",
      "decision": "pass",
      "reason": "年龄在承保范围内"
    }
  ],
  "evaluated_count": 15,
  "eval_time_ms": 3
}
```

### 4.2 评估流程

```
输入：投保单 JSON
  │
  ▼
Step 1：JSON 扁平化为 Facts Map
  {"applicant.age": 35, "insured.bmi": 23.5, "product_type": "life", ...}
  │
  ▼
Step 2：按优先级加载规则（从 Redis）
  ① COMMON 规则（所有 scope=1 的已发布规则表）
  ② PRODUCT_TYPE 规则（匹配当前产品类型）
  ③ PRODUCT 规则（匹配当前产品ID，优先渠道匹配）
  │
  ▼
Step 3：逐表逐行评估
  对每个规则表：
    按 priority 排序遍历规则行
    → 将 conditions 转为 Aviator 表达式
    → 用 Facts Map 求值
    → 首个全部 conditions 匹配的行 → 取其 actions
    → 记录匹配结果
  │
  ▼
Step 4：合并所有 actions
  遍历所有匹配结果，取最严格的结论：
  reject > manual > defer > exclusion > loading > pass
  │
  ▼
输出：核保结论 JSON
```

### 4.3 Aviator 表达式转换

```java
// conditions JSON → Aviator 表达式
// 输入：
// {"logic":"AND", "items":[
//   {"field":"applicant.age","operator":">=","value":18},
//   {"field":"applicant.age","operator":"<=","value":70}
// ]}
// 
// 输出 Aviator 表达式：
// "applicant.age >= 18 && applicant.age <= 70"

// 编译缓存（Aviator 编译后的 Expression 对象，线程安全）
private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

public boolean evaluate(RuleRow row, Map<String, Object> facts) {
    String expr = toAviatorExpression(row.getConditions());
    Expression compiled = expressionCache.computeIfAbsent(expr, 
        e -> AviatorEvaluator.compile(e));
    Object result = compiled.execute(facts);
    return Boolean.TRUE.equals(result);
}
```

---

## 五、Redis 缓存与热更新

### 5.1 缓存结构

```
# 规则表列表（按 scope 分组）
HSET covex:rules:tables:common     {table_code} {table_meta_json}
HSET covex:rules:tables:type:life  {table_code} {table_meta_json}
HSET covex:rules:tables:product:13 {table_code} {table_meta_json}

# 规则行（每个规则表的所有行）
HSET covex:rules:rows:{table_id}   {row_id} {row_json}

# 版本号（用于快速检测是否需要刷新）
SET  covex:rules:version            {全局版本号}
```

### 5.2 热更新机制

```
核保老师在页面修改规则 → 点击"发布"
  │
  ├─ 1. 写入 MySQL（ins_rule_table.status → 已发布）
  ├─ 2. 更新 Redis 缓存（HSET 规则行数据）
  ├─ 3. 递增全局版本号
  └─ 4. PUBLISH covex:rules:changed {table_code}
         │
         ▼
    所有应用实例收到 Redis 消息
         │
         ├─ 清除本地 expressionCache
         ├─ 重新从 Redis 加载该表规则行
         └─ 完成（毫秒级，无重启）
```

**性能保证**：
- 规则数据全在 Redis，评估时零 DB 查询
- Aviator 表达式预编译缓存，评估耗时 < 5ms
- 热更新延迟 < 1 秒（Redis Pub/Sub 推送）

---

## 六、前端界面设计

### 6.1 页面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│  核保规则配置中心                                          [测试] [发布] │
├──────────────────┬──────────────────────────────────────────────────┤
│                  │                                                  │
│  📁 公共规则      │  规则表：年龄校验规则（UW_AGE_CHECK）                │
│  ├ 📋 年龄校验    │  范围：公共  |  状态：已发布  |  版本：v3           │
│  ├ 📋 BMI校验    │                                                  │
│  ├ 📋 职业风险    │  输入字段：投保人年龄 | 被保人BMI | 职业等级          │
│  ├ 📋 累计保额    │  输出字段：核保结论 | 原因说明 | 加费金额             │
│  └ 📋 健康告知    │                                                  │
│                  │  ┌────┬──────────┬──────┬──────────┬──────┬─────┐│
│  📁 寿险规则      │  │ 行 │ 规则名称  │ 年龄  │ 年龄     │ 结论  │ 操作 ││
│  ├ 📋 等待期     │  ├────┼──────────┼──────┼──────────┼──────┼─────┤│
│  ├ 📋 保额上限    │  │ 1  │ 标准承保  │ >=18 │ <=70     │ 通过  │ ⚙️  ││
│  └ 📋 费率规则    │  │ 2  │ 未成年拒  │ <18  │          │ 拒保  │ ⚙️  ││
│                  │  │ 3  │ 超龄拒保  │ >70  │          │ 拒保  │ ⚙️  ││
│  📁 年金险规则    │  │ 4  │ 高龄加费  │ >=55 │ <=70     │ 加费  │ ⚙️  ││
│  └ 📋 最低缴费   │  └────┴──────────┴──────┴──────────┴──────┴─────┘│
│                  │                                                  │
│  📁 意外险规则    │  [+ 添加规则行]  [批量导入]  [导出]                   │
│                  │                                                  │
│  📁 产品级规则    │  ─────────────────────────────────────────────── │
│  ├ 📋 产品A-个险  │  规则行详情（点击编辑）                             │
│  ├ 📋 产品A-银保  │  规则名称：[高龄加费          ]                    │
│  └ 📋 产品B      │  条件：                                           │
│                  │    投保人年龄 [>=] [55   ]  [AND]                  │
│  [新建规则表]     │    投保人年龄 [<=] [70   ]                        │
│                  │  动作：                                           │
│                  │    核保结论：[加费 ▼]                               │
│                  │    加费金额：[500    ] 元                          │
│                  │    原因说明：[高龄投保人，加费承保    ]               │
│                  │                                                  │
│                  │              [保存草稿]  [保存并发布]                │
└──────────────────┴──────────────────────────────────────────────────┘
```

### 6.2 规则测试面板

```
┌──────────────────────────────────────────┐
│  规则测试（模拟核保）                       │
├──────────────────────────────────────────┤
│  输入 JSON：                              │
│  ┌────────────────────────────────────┐  │
│  │ {                                  │  │
│  │   "applicant": {"age": 55},        │  │
│  │   "insured": {"bmi": 28.5},        │  │
│  │   "product_type": "life",          │  │
│  │   "product_id": 13,                │  │
│  │   "channel_id": 1                  │  │
│  │ }                                  │  │
│  └────────────────────────────────────┘  │
│  [执行测试]                               │
│                                          │
│  测试结果：                               │
│  ┌────────────────────────────────────┐  │
│  │ 核保结论：加费承保                    │  │
│  │ 加费金额：500 元                     │  │
│  │ 原因：高龄投保人 + BMI 超重           │  │
│  │ 耗时：3ms                           │  │
│  │                                    │  │
│  │ 匹配规则：                           │  │
│  │ ✅ UW_AGE_CHECK #4 高龄加费 → 加费   │  │
│  │ ✅ UW_BMI_CHECK #3 BMI超重 → 加费    │  │
│  │ ✅ UW_COMMON #1 年龄范围 → 通过      │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 七、与现有系统集成

### 7.1 替代现有 LiteFlow 核保组件

```
现有：
ProposalService.submitProposal()
  → flowExecutor.execute2Resp("underwriteChain")
    → UwHealthComponent（硬编码 BMI>35）
    → UwOccupationComponent（硬编码 riskLevel<=2）
    → UwCumulativeAmountComponent（硬编码）

改造后：
ProposalService.submitProposal()
  → RuleEngineService.evaluate(proposalFacts)
    → 从 Redis 加载规则
    → Aviator 表达式求值
    → 返回核保结论
  → 根据结论决定后续流程
```

### 7.2 数据组装（投保单 → Facts Map）

```java
public Map<String, Object> buildFacts(ProposalEntity proposal) {
    Map<String, Object> facts = new HashMap<>();
    
    // 投保人信息
    facts.put("applicant.age", calculateAge(proposal.getApplicantBirthday()));
    facts.put("applicant.gender", proposal.getApplicantGender());
    
    // 被保人信息
    facts.put("insured.age", calculateAge(proposal.getInsuredBirthday()));
    facts.put("insured.bmi", insuredHealth.getBmi());
    facts.put("insured.occupation_risk_level", insuredHealth.getOccupationRiskLevel());
    facts.put("insured.smoking_status", insuredHealth.getSmokingStatus());
    
    // 产品信息
    facts.put("product_id", proposal.getProductId());
    facts.put("product_type", product.getProductType());
    facts.put("channel_id", proposal.getChannelId());
    
    // 投保信息
    facts.put("sum_insured", proposal.getTotalSumInsured());
    facts.put("health_declaration", proposal.getHealthDeclaration());
    
    return facts;
}
```

---

## 八、实施计划

### Task R-1：数据模型 + 后端 CRUD（2 天）
- [ ] 创建 ins_rule_table / ins_rule_row DDL
- [ ] 创建 Entity / Mapper / Service / Controller
- [ ] 实现规则表 CRUD API
- [ ] 实现规则行 CRUD API（支持批量保存）

### Task R-2：规则评估引擎（2 天）
- [ ] 实现 JSON → Aviator 表达式转换器
- [ ] 实现 RuleEvaluator：加载规则 → 逐行匹配 → 合并结论
- [ ] 实现 Facts Map 构建器（投保单 → 扁平化 Map）
- [ ] 实现 POST /api/underwriting/evaluate API
- [ ] 集成测试

### Task R-3：Redis 缓存 + 热更新（1 天）
- [ ] 规则发布时写入 Redis
- [ ] Redis Pub/Sub 通知机制
- [ ] 应用端监听 + 本地缓存刷新
- [ ] 性能测试（< 5ms 评估延迟）

### Task R-4：前端规则配置中心（3 天）
- [ ] 页面布局：左侧规则树 + 右侧决策表编辑器
- [ ] 决策表编辑器（类 Excel，支持增删改行）
- [ ] 条件/动作可视化编辑弹窗
- [ ] 规则测试面板（JSON 输入 → 结论输出）
- [ ] 发布按钮 + 版本管理

### Task R-5：集成替换现有核保逻辑（1 天）
- [ ] 将现有 UwHealth/UwOccupation 等硬编码规则迁移到决策表
- [ ] ProposalService 调用 RuleEngineService 替代 LiteFlow underwriteChain
- [ ] E2E 验证

**总预估**：9 天

---

## 九、示例规则数据

### 示例 1：公共规则 — 年龄校验

```json
// ins_rule_table
{
  "table_code": "UW_AGE_CHECK",
  "table_name": "投保人年龄校验",
  "scope_type": 1,
  "input_fields": [
    {"field": "applicant.age", "label": "投保人年龄", "type": "number"}
  ],
  "output_fields": [
    {"field": "uw_decision", "label": "核保结论", "type": "enum"},
    {"field": "reason", "label": "原因", "type": "string"}
  ]
}

// ins_rule_row（4 行）
[
  {"row_no":1, "priority":10, "rule_name":"未成年拒保",
   "conditions":{"logic":"AND","items":[{"field":"applicant.age","operator":"<","value":18}]},
   "actions":{"uw_decision":"reject","reason":"投保人未满18周岁"}},
  
  {"row_no":2, "priority":20, "rule_name":"标准承保年龄",
   "conditions":{"logic":"AND","items":[{"field":"applicant.age","operator":">=","value":18},{"field":"applicant.age","operator":"<=","value":70}]},
   "actions":{"uw_decision":"pass","reason":"年龄在承保范围内"}},
  
  {"row_no":3, "priority":30, "rule_name":"高龄加费",
   "conditions":{"logic":"AND","items":[{"field":"applicant.age","operator":">","value":55},{"field":"applicant.age","operator":"<=","value":70}]},
   "actions":{"uw_decision":"loading","reason":"高龄投保人","loading_amount":500}},
  
  {"row_no":4, "priority":40, "rule_name":"超龄拒保",
   "conditions":{"logic":"AND","items":[{"field":"applicant.age","operator":">","value":70}]},
   "actions":{"uw_decision":"reject","reason":"投保人年龄超出70周岁"}}
]
```

### 示例 2：产品级规则 — 按渠道拆分保额上限

```json
// 规则表：产品A 保额上限（产品级 + 渠道维度）
{
  "table_code": "UW_PROD_A_SUM_LIMIT",
  "table_name": "产品A保额上限",
  "scope_type": 3,
  "product_id": 13,
  "channel_id": 0,
  "input_fields": [
    {"field": "sum_insured", "label": "投保保额", "type": "number"},
    {"field": "channel_id", "label": "渠道", "type": "number"}
  ]
}

// 规则行：不同渠道不同上限
[
  {"row_no":1, "priority":10, "rule_name":"个险渠道上限100万",
   "conditions":{"logic":"AND","items":[{"field":"channel_id","operator":"==","value":1},{"field":"sum_insured","operator":">","value":1000000}]},
   "actions":{"uw_decision":"reject","reason":"个险渠道保额上限100万"}},
  
  {"row_no":2, "priority":20, "rule_name":"银保渠道上限50万",
   "conditions":{"logic":"AND","items":[{"field":"channel_id","operator":"==","value":2},{"field":"sum_insured","operator":">","value":500000}]},
   "actions":{"uw_decision":"reject","reason":"银保渠道保额上限50万"}},
  
  {"row_no":3, "priority":30, "rule_name":"默认渠道上限20万",
   "conditions":{"logic":"AND","items":[{"field":"sum_insured","operator":">","value":200000}]},
   "actions":{"uw_decision":"reject","reason":"保额超出默认上限20万"}}
]
```
