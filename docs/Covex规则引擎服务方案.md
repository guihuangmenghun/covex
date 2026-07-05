# Covex 规则引擎服务 — 技术方案 v2（独立微服务）

> **版本**：v2.2
> **日期**：2026-07-06
> **定位**：独立规则引擎微服务，支持核保/费率/理赔/佣金等多种规则场景
> **注册中心**：Nacos（服务名：covex-rule-engine）
> **端口**：8081
> **认证**：复用 covex-web 的 JWT 登录体系，前端通过 covex-web 代理调用规则引擎 API
> **权限矩阵**：详见 `docs/Covex路由权限矩阵.md`（13 角色）
> **版本追溯**：version + requirement_no 双定位，逻辑删除保留全量历史

---

## 一、整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                     前端（Covex-ui:3000）                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │ 核保规则配置   │  │ 费率规则配置  │  │ 理赔规则配置  │  ...更多     │
│  │ /rule-center  │  │              │  │              │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
└─────────┼──────────────────┼──────────────────┼─────────────────────┘
          │                  │                  │
          └──────────────────┴──────────────────┘
                             │
                    ┌────────┴────────┐
                    │  covex-web:8080 │  ← JWT 认证 + 角色鉴权
                    │  (业务网关)      │     前端所有请求都经过这里
                    │                 │
                    │ 业务API + 规则API代理 │
                    └───┬─────────┬───┘
                        │         │
         业务逻辑     │         │ 规则引擎调用
                        │         │ (Nacos 服务发现)
            ┌─────┴──┐     ┌───┴───────────┐
            │covex-   │     │ covex-rule-   │
            │service  │     │ engine:8081   │
            │(投保/核保│     │ (独立规则服务) │
            │ /理赔)  │     │               │
            └────┬────┘     │ ┌───────────┐ │
                 │          │ │RuleEngine │ │
                 │          │ │ Core API  │ │
                 │          │ └─────┬─────┘ │
                 │          │ ┌─────┴─────┐ │
                 │          │ │ Aviator   │ │
                 │          │ └───────────┘ │
                 │          └──┬─────────┬──┘
                 │             │         │
            ┌────┴───┐   ┌───┴───┐ ┌──┴──────┐
            │ MySQL  │   │ MySQL │ │  Redis  │
            │业务数据 │   │规则数据│ │缓存+Pub │
            └────────┘   └───────┘ └─────────┘
```

**认证流程**：
- 前端登录 → covex-web 签发 JWT（复用现有体系）
- 规则管理页面 → 前端请求 covex-web → covex-web 校验 JWT + 角色权限 → 转发至 covex-rule-engine
- 规则引擎本身不处理认证，由 covex-web 代理鉴权后透传

**核心原则**：
- 规则引擎是**通用基础设施**，不包含任何保险业务逻辑
- 业务语义（核保/费率/理赔）由**规则表定义**决定，而非代码
- 任何新规则场景 = 新建一组规则表，零代码变更

---

## 二、模块结构

```
covex-parent/
├── covex-common/          ← 通用工具（BaseEntity, Result, JwtUtil...）
├── covex-api/             ← API DTO 定义
├── covex-service/         ← 业务 Service（投保/核保/理赔...）
├── covex-web/             ← 业务 Controller（8080）
└── covex-rule-engine/     ← 【新增】独立规则引擎服务（8081）
    ├── pom.xml
    └── src/main/java/com/covex/rule/
        ├── RuleEngineApplication.java
        ├── controller/
        │   ├── RuleTableController.java      # 规则表 CRUD
        │   ├── RuleRowController.java        # 规则行 CRUD
        │   ├── RuleEvalController.java       # 规则评估 API
        │   ├── RulePublishController.java    # 规则发布/版本管理
        │   └── RuleHistoryController.java    # 【新增】规则追溯/历史版本查询
        ├── service/
        │   ├── RuleTableService.java
        │   ├── RuleRowService.java
        │   ├── RuleEvaluator.java            # 核心：规则评估引擎
        │   ├── AviatorExpressionCompiler.java # JSON → Aviator 转换
        │   ├── RuleCacheService.java         # Redis 缓存管理
        │   ├── RulePublishService.java       # 发布 + Pub/Sub 热更新
        │   └── RuleHistoryService.java       # 【新增】版本追溯 + 快照管理
        ├── entity/
        │   ├── RuleTableEntity.java
        │   ├── RuleRowEntity.java
        │   └── RuleSnapshotEntity.java       # 【新增】发布快照实体
        ├── mapper/
        │   ├── RuleTableMapper.java
        │   └── RuleRowMapper.java
        └── dto/
            ├── RuleEvalRequest.java          # 评估请求
            ├── RuleEvalResponse.java         # 评估响应
            └── RulePublishEvent.java         # 发布事件
```

---

## 三、通用规则模型

### 3.1 规则表（ins_rule_table）

```sql
CREATE TABLE ins_rule_table (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    
    -- 唯一标识
    table_code      VARCHAR(50) NOT NULL COMMENT '唯一编码',
    table_name      VARCHAR(100) NOT NULL COMMENT '显示名称',
    description     VARCHAR(500) COMMENT '规则说明',
    
    -- 规则分类（通用，不限于核保）
    rule_domain     VARCHAR(30) NOT NULL COMMENT '规则域：underwriting/premium/claim/commission/...',
    scope_type      TINYINT NOT NULL COMMENT '1=公共 2=产品类型 3=产品级',
    product_type    VARCHAR(20) COMMENT '产品类型（scope=2时）',
    product_id      BIGINT COMMENT '产品ID（scope=3时）',
    channel_id      BIGINT DEFAULT 0 COMMENT '渠道ID（0=所有渠道）',
    
    -- 输入输出定义（通用 JSON Schema）
    input_fields    JSON NOT NULL COMMENT '输入字段 [{"field":"age","label":"年龄","type":"number"}]',
    output_fields   JSON NOT NULL COMMENT '输出字段 [{"field":"decision","label":"结论","type":"enum"}]',
    
    -- 版本与状态
    version         INT DEFAULT 1 COMMENT '当前版本号，每次发布 +1',
    status          TINYINT DEFAULT 1 COMMENT '1=草稿 2=已发布 3=已归档',
    requirement_no  VARCHAR(50) COMMENT '需求号，可重复，如 REQ-2026-001',
    
    -- 通用字段
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1) DEFAULT 0,
    deleted_at      DATETIME,
    
    UNIQUE KEY uk_code_tenant (table_code, tenant_id),
    KEY idx_domain_scope (rule_domain, scope_type),
    KEY idx_product (product_id, status),
    KEY idx_requirement (requirement_no)
) COMMENT='规则表定义（通用）';
```

### 3.2 规则行（ins_rule_row）

```sql
CREATE TABLE ins_rule_row (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    rule_table_id   BIGINT NOT NULL,
    row_no          INT NOT NULL,
    priority        INT DEFAULT 100,
    rule_name       VARCHAR(100),
    rule_code       VARCHAR(50) COMMENT '规则编码，同一 rule_code 可有多行（不同条件分支）',
    
    -- 标准化 JSON（可迁移至任意 BRMS）
    conditions      JSON NOT NULL COMMENT '条件表达式',
    actions         JSON NOT NULL COMMENT '动作/结论',
    
    -- 版本追溯
    version         INT DEFAULT 1 COMMENT '该行版本号，修改时旧行逻辑删除，新行 version+1',
    requirement_no  VARCHAR(50) COMMENT '需求号，继承自规则表，可重复',
    
    is_active       TINYINT(1) DEFAULT 1,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1) DEFAULT 0 COMMENT '修改时旧行=1，新行=0',
    deleted_at      DATETIME COMMENT '逻辑删除时间',
    
    KEY idx_table_priority (rule_table_id, priority),
    KEY idx_rule_code (rule_table_id, rule_code),
    KEY idx_version (rule_table_id, version, is_deleted)
) COMMENT='规则行（通用，含版本追溯）';
```

### 3.3 规则发布快照（ins_rule_snapshot）

```sql
CREATE TABLE ins_rule_snapshot (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    rule_table_id   BIGINT NOT NULL COMMENT '关联规则表',
    version         INT NOT NULL COMMENT '发布版本号',
    requirement_no  VARCHAR(50) COMMENT '本次发布的需求号',
    
    -- 完整快照（包含规则表元数据 + 所有规则行）
    snapshot_data   JSON NOT NULL COMMENT '{table: {...}, rows: [{...}, ...]}',
    
    -- 发布元信息
    published_by    VARCHAR(50) COMMENT '发布人',
    published_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    change_summary  VARCHAR(500) COMMENT '变更说明（本次改了什么）',
    comment         TEXT COMMENT '版本注释（评审意见、修改原因等，默认不加载，按需查询）',
    
    -- 通用字段
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1) DEFAULT 0,
    deleted_at      DATETIME,
    
    UNIQUE KEY uk_table_version (rule_table_id, version),
    KEY idx_table_id (rule_table_id),
    KEY idx_requirement (requirement_no)
) COMMENT='规则发布快照（每次发布一条，用于版本追溯与对比）';
```

### 3.4 rule_domain 支持的规则域

| rule_domain | 说明 | 调用方 | 典型规则表示例 |
|---|---|---|---|
| `underwriting` | 核保规则 | covex-web 投保流程 | UW_AGE_CHECK, UW_BMI_CHECK |
| `premium` | 费率/保费规则 | covex-web 保费计算 | PREM_RATE_LIFE, PREM_DISCOUNT |
| `claim` | 理赔审核规则 | covex-web 理赔流程 | CLAIM_VALIDATE, CLAIM_CALC |
| `commission` | 佣金计算规则 | covex-web 佣金结算 | COMM_RATE, COMM_BONUS |
| `validation` | 通用校验规则 | 任意服务 | VALIDATE_ID, VALIDATE_AGE |

**新增规则域 = 零代码变更**，只需在前端新建规则表并指定 `rule_domain`。

---

## 四、核心 API

### 4.1 规则评估 API（通用）

```
POST /api/rule/evaluate

请求体：
{
  "rule_domain": "underwriting",        // 规则域
  "context": {                           // 评估上下文
    "product_id": 13,
    "product_type": "life",
    "channel_id": 1,
    "facts": {                           // 扁平化事实数据
      "applicant.age": 35,
      "applicant.gender": 1,
      "insured.bmi": 28.5,
      "insured.occupation_risk_level": 2,
      "sum_insured": 500000
    }
  }
}

响应体：
{
  "matched": true,
  "decision": "loading",                // 最终结论（取 actions 中最高优先级匹配）
  "details": {                           // 完整输出字段
    "reason": "BMI 28.5 超重，加费承保",
    "loading_amount": 500
  },
  "matched_rules": [                     // 命中的规则明细
    {
      "table_code": "UW_BMI_CHECK",
      "table_name": "BMI校验",
      "row_no": 3,
      "rule_name": "BMI 25-30 加费",
      "priority": 30,
      "actions": {"decision":"loading","reason":"BMI超重","loading_amount":500}
    }
  ],
  "evaluated_tables": 5,                 // 评估了多少张规则表
  "eval_time_ms": 2                      // 耗时
}
```

### 4.2 按规则表评估（精确调用）

```
POST /api/rule/evaluate-table

请求体：
{
  "table_code": "UW_BMI_CHECK",
  "facts": {
    "insured.bmi": 28.5
  }
}

响应体：
{
  "matched": true,
  "row_no": 3,
  "rule_name": "BMI 25-30 加费",
  "actions": {"decision":"loading","loading_amount":500}
}
```

### 4.3 规则管理 API

```
# 规则表 CRUD
GET    /api/rule/table?domain=underwriting&scope=1     # 查询规则表列表
POST   /api/rule/table                                  # 创建规则表
PUT    /api/rule/table/{id}                             # 修改规则表
DELETE /api/rule/table/{id}                             # 删除规则表

# 规则行 CRUD
GET    /api/rule/table/{tableId}/rows                   # 查询规则行（仅当前有效）
POST   /api/rule/table/{tableId}/rows                   # 添加规则行
PUT    /api/rule/row/{id}                               # 修改规则行（旧行逻辑删除 + 新行创建）
DELETE /api/rule/row/{id}                               # 删除规则行（逻辑删除）
POST   /api/rule/table/{tableId}/rows/batch             # 批量保存规则行

# 发布管理
POST   /api/rule/table/{id}/publish                     # 发布规则表（生成快照）
POST   /api/rule/table/{id}/archive                     # 归档规则表
GET    /api/rule/table/{id}/versions                    # 查看版本历史（快照列表，不含 comment）
GET    /api/rule/table/{id}/versions/{version}          # 查看指定版本的快照详情（含 comment）
GET    /api/rule/table/{id}/versions/{version}/comment  # 单独获取版本注释（懒加载）
GET    /api/rule/table/{id}/versions/diff?v1=1&v2=3     # 对比两个版本差异

# 版本追溯
GET    /api/rule/product/{productId}/rules              # 查看当前产品的生效规则
GET    /api/rule/product/{productId}/rule-history       # 查看当前产品的历史版本快照
GET    /api/rule/requirement/{requirementNo}            # 按需求号查询关联的规则变更
GET    /api/rule/row/{id}/history                       # 查看某条规则行的全部历史（含逻辑删除的旧行）
```

---

## 五、规则评估引擎核心逻辑

### 5.1 评估流程

```
输入：rule_domain + context (product_id, product_type, channel_id, facts)
  │
  ▼
Step 1：加载规则表（从 Redis）
  按 rule_domain 查询所有已发布的规则表
  按 scope_type 分组：COMMON → PRODUCT_TYPE → PRODUCT
  │
  ▼
Step 2：过滤匹配的规则表
  COMMON：全部加载
  PRODUCT_TYPE：只加载 product_type 匹配的
  PRODUCT：只加载 product_id 匹配的
    └── 同 product_id 下，优先 channel_id 精确匹配，其次 channel_id=0
  │
  ▼
Step 3：逐表逐行评估
  对每张规则表：
    从 Redis 加载规则行（按 priority 排序）
    遍历规则行：
      → conditions JSON 转 Aviator 表达式
      → 用 facts Map 求值
      → 首个全部匹配的行 → 记录其 actions
      → 如果规则表是"首匹配即停"模式 → 停止遍历该表
  │
  ▼
Step 4：合并结果
  收集所有匹配行的 actions
  按 priority 排序，取最高优先级的 decision 作为最终结论
  返回完整匹配明细
```

### 5.2 Aviator 表达式转换

```java
// conditions JSON → Aviator 表达式字符串
public String compile(ConditionGroup group) {
    String logic = "AND".equalsIgnoreCase(group.getLogic()) ? " && " : " || ";
    return group.getItems().stream()
        .map(this::compileItem)
        .collect(Collectors.joining(logic));
}

private String compileItem(ConditionItem item) {
    String field = item.getField();   // e.g. "applicant.age"
    String op = item.getOperator();   // e.g. ">="
    Object val = item.getValue();     // e.g. 18
    
    return switch (op) {
        case "==" -> field + " == " + quote(val);
        case "!=" -> field + " != " + quote(val);
        case ">"  -> field + " > " + quote(val);
        case ">=" -> field + " >= " + quote(val);
        case "<"  -> field + " < " + quote(val);
        case "<=" -> field + " <= " + quote(val);
        case "in" -> "seq.contains(" + quote(val) + ", " + field + ")";
        case "contains" -> "string.contains(" + field + ", " + quote(val) + ")";
        case "exists" -> (Boolean.TRUE.equals(val) ? field + " != nil" : field + " == nil");
        default -> throw new BizException("不支持的操作符: " + op);
    };
}
```

### 5.3 表达式编译缓存

```java
// Aviator 编译后的 Expression 线程安全，全局缓存
private final Map<String, Expression> cache = new ConcurrentHashMap<>();

public Object evaluate(String expressionStr, Map<String, Object> env) {
    Expression expr = cache.computeIfAbsent(expressionStr, 
        AviatorEvaluator::compile);
    return expr.execute(env);
}

// 热更新时清除缓存
public void clearCache() {
    cache.clear();
}
```

---

## 六、Redis 缓存 + 热更新

### 6.1 缓存结构

```
# 规则表元数据（按 domain 分组）
HSET covex:rule:tables:{domain}              {table_code} {table_meta_json}

# 规则表元数据（按 scope 细分，用于评估时快速加载）
HSET covex:rule:tables:{domain}:common        {table_code} {table_meta_json}
HSET covex:rule:tables:{domain}:type:{ptype}  {table_code} {table_meta_json}
HSET covex:rule:tables:{domain}:product:{pid} {table_code} {table_meta_json}

# 规则行（每个规则表的所有行）
HSET covex:rule:rows:{table_id}               {row_id} {row_json}

# 全局版本号
SET  covex:rule:version                        {version_number}
```

### 6.2 热更新流程

```
前端点击"发布"
  │
  ├─ 1. MySQL：规则表 status → 已发布，version +1
  ├─ 2. Redis：更新 HSET 规则表元数据 + 规则行数据
  ├─ 3. Redis：INCR covex:rule:version
  └─ 4. Redis：PUBLISH covex:rule:changed {"table_code":"UW_BMI_CHECK","domain":"underwriting"}
         │
         ▼
    所有 covex-rule-engine 实例收到消息
         │
         ├─ 清除本地 Aviator Expression 缓存
         ├─ 从 Redis 重新加载该表的规则行
         └─ 完成（< 100ms）
```

### 6.3 启动预热

```
covex-rule-engine 启动时：
  1. 从 MySQL 加载所有 status=已发布 的规则表
  2. 写入 Redis
  3. 预编译所有规则行的 Aviator 表达式
  4. 就绪后注册 Nacos
```

---

## 6.5、版本管理与规则追溯

### 6.5.1 设计理念

规则是“活”的配置，会不断修改。必须能回答三个问题：
1. **当前生效的是哪个版本？** → `status=2` 的规则表 + `is_deleted=0` 的规则行
2. **上一次发布的是什么内容？** → `ins_rule_snapshot` 快照表
3. **某条规则为什么改成这样？** → `requirement_no` 需求号追溯

### 6.5.2 version + requirement_no 双字段定位

```
┌─────────────────────────────────────────────────────────────┐
│                    双字段定位机制                              │
│                                                             │
│  version（版本号）          requirement_no（需求号）           │
│  ├─ 每次发布 +1              ├─ 可重复，同一需求多次修改       │
│  ├─ 严格递增                  ├─ 格式自由，如 REQ-001         │
│  ├─ 用于“第几版”              ├─ 用于“为什么改”               │
│  └─ 快照表有完整记录          └─ 可跨多个规则表共享             │
│                                                             │
│  示例：                                                     │
│  ┌────────────────┬─────────┬──────────────┐  │
│  │ 规则表          │ version │ requirement_no │  │
│  ├────────────────┼─────────┼──────────────┤  │
│  │ UW_AGE_CHECK   │ 1       │ REQ-2026-001 │  │ ← 初始创建
│  │ UW_AGE_CHECK   │ 2       │ REQ-2026-001 │  │ ← 同需求修改
│  │ UW_AGE_CHECK   │ 3       │ REQ-2026-005 │  │ ← 新需求修改
│  │ UW_BMI_CHECK   │ 1       │ REQ-2026-001 │  │ ← 同需求创建
│  │ UW_BMI_CHECK   │ 2       │ REQ-2026-005 │  │ ← 同需求修改
│  └────────────────┴─────────┴──────────────┘  │
│                                                             │
│  查询“REQ-2026-001 改了哪些规则？”→ 返回 UW_AGE_CHECK v1-v2 + UW_BMI_CHECK v1  │
│  查询“UW_AGE_CHECK 的演变？”→ 返回 v1 → v2 → v3 全量历史       │
└─────────────────────────────────────────────────────────────┘
```

### 6.5.3 规则行修改流程（逻辑删除）

```
用户修改规则行：

  旧行 (id=10, version=1, is_deleted=0)
    │
    ├─ 1. 旧行 is_deleted → 1, deleted_at → NOW()   ← 逻辑删除，不物理删除
    │
    └─ 2. 新行 (id=11, version=2, is_deleted=0)       ← 新建，version+1
           继承 rule_code, rule_table_id, requirement_no
           conditions / actions 为新值

结果：
  rule_table_id=5 的所有行：
  ┌─────┬──────────┬────────────┬────────────┐
  │ id  │ version  │ is_deleted │ rule_code  │
  ├─────┼──────────┼────────────┼────────────┤
  │ 10  │ 1        │ 1 ★        │ UW_AGE_001 │  ← 旧版（逻辑删除）
  │ 11  │ 2        │ 0          │ UW_AGE_001 │  ← 当前版
  │ 12  │ 1        │ 0          │ UW_AGE_002 │  ← 未修改的行
  └─────┴──────────┴────────────┴────────────┘
```

### 6.5.4 发布流程（生成快照）

```
用户点击“发布”：

  1. ins_rule_table: status → 2(已发布), version +1
  2. 生成快照 → ins_rule_snapshot:
     {
       "rule_table_id": 5,
       "version": 2,
       "requirement_no": "REQ-2026-005",
       "snapshot_data": {
         "table": { "table_code": "UW_AGE_CHECK", "table_name": "年龄校验", ... },
         "rows": [
           { "rule_code": "UW_AGE_001", "conditions": {...}, "actions": {...}, "priority": 1 },
           { "rule_code": "UW_AGE_002", "conditions": {...}, "actions": {...}, "priority": 2 }
         ]
       },
       "published_by": "admin",
       "change_summary": "调整年龄上限从60岁到65岁"
     }
  3. Redis 更新缓存 + Pub/Sub 通知
```

### 6.5.5 追溯查询场景

| 场景 | 查询方式 | 数据来源 |
|---|---|---|
| 查看当前产品的规则 | `WHERE product_id=X AND is_deleted=0 AND status=2` | ins_rule_table + ins_rule_row |
| 查看当前产品的旧版规则 | `WHERE product_id=X` 的快照列表 | ins_rule_snapshot |
| 查看已发布规则的旧版 | 指定 table_id 的快照版本列表 | ins_rule_snapshot |
| 对比两个版本差异 | 取两个快照的 snapshot_data 做 diff | ins_rule_snapshot |
| 按需求号查规则变更 | `WHERE requirement_no='REQ-001'` | ins_rule_snapshot |
| 查看某条规则行的全部历史 | `WHERE rule_table_id=X AND rule_code=Y` 含已删除 | ins_rule_row (含 is_deleted=1) |
| 查看版本注释 | `SELECT comment FROM ins_rule_snapshot WHERE id=X`（单独查询，不随列表加载） | ins_rule_snapshot.comment |

---

## 七、covex-web 调用方式

### 7.1 通过 HTTP 调用规则引擎

```java
// covex-web 中的 ProposalService
@Service
public class ProposalService {
    
    private final RestTemplate restTemplate; // 或 WebClient
    
    // 核保评估
    public UnderwriteResult evaluateUnderwriting(ProposalEntity proposal) {
        Map<String, Object> facts = buildFacts(proposal);
        
        RuleEvalRequest request = new RuleEvalRequest();
        request.setRuleDomain("underwriting");
        RuleEvalRequest.Context ctx = new RuleEvalRequest.Context();
        ctx.setProductId(proposal.getProductId());
        ctx.setProductType(getProductType(proposal));
        ctx.setChannelId(proposal.getChannelId());
        ctx.setFacts(facts);
        request.setContext(ctx);
        
        // 调用规则引擎服务（Nacos 服务发现）
        RuleEvalResponse response = restTemplate.postForObject(
            "http://covex-rule-engine/api/rule/evaluate", 
            request, 
            RuleEvalResponse.class
        );
        
        return convertToUnderwriteResult(response);
    }
}
```

### 7.2 替代现有 LiteFlow 核保组件

```
现有：
ProposalService → LiteFlow underwriteChain → UwHealth/UwOccupation（硬编码）

改造后：
ProposalService → HTTP → covex-rule-engine（规则表驱动）
                            ├── UW_AGE_CHECK（可配置）
                            ├── UW_BMI_CHECK（可配置）
                            ├── UW_OCCUPATION_CHECK（可配置）
                            └── ...更多规则表（核保老师随时添加）
```

---

## 八、前端页面规划

```
Covex-ui/src/views/rule-engine/
├── RuleConfigCenter.vue          # 规则配置中心（主页面）
│   ├── 左侧：规则域切换 + 规则表树
│   ├── 中间：决策表编辑器（类 Excel）
│   └── 右侧：规则测试面板
├── RuleHistoryDialog.vue         # 【新增】规则版本追溯弹窗
├── components/
│   ├── RuleTree.vue              # 规则表树形导航
│   ├── DecisionTableEditor.vue   # 决策表编辑器
│   ├── ConditionEditor.vue       # 条件编辑弹窗
│   ├── ActionEditor.vue          # 动作编辑弹窗
│   ├── RuleTestPanel.vue         # 规则测试面板
│   └── VersionDiffViewer.vue     # 【新增】版本对比视图（类 Git diff）
└── api/
    └── ruleEngine.ts             # 规则引擎 API 调用
```

**一个配置中心，多个 Tab**：

| Tab | rule_domain | 说明 |
|---|---|---|
| 核保规则 | underwriting | 核保老师配置 |
| 费率规则 | premium | 精算师配置 |
| 理赔规则 | claim | 理赔主管配置 |
| 佣金规则 | commission | 财务配置 |
| 校验规则 | validation | 通用校验 |

**版本追溯功能（工具栏按钮）**：

```
┌─────────────────────────────────────────────────────────┐
│ 核保规则  │ [发布] [保存草稿] │ [📜 历史版本] [📦 产品规则] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [📜 历史版本] 按钮 → 打开 RuleHistoryDialog             │
│    ├─ 左侧：版本列表（v1, v2, v3...）+ 需求号 + 发布时间  │
│    ├─ 右侧：选中版本的快照内容（只读）                    │
│    └─ 底部：[对比当前版本] 按钮 → VersionDiffViewer      │
│                                                         │
│  [📦 产品规则] 按钮 → 按产品筛选查看                    │
│    ├─ 下拉选择产品                                       │
│    ├─ 显示该产品当前生效的规则                            │
│    └─ [查看旧版] 按钮 → 该产品的历史快照列表             │
└─────────────────────────────────────────────────────────┘
```

---

## 九、pom.xml 配置

```xml
<!-- covex-rule-engine/pom.xml 核心依赖 -->
<dependencies>
    <!-- Covex 通用模块 -->
    <dependency>
        <groupId>com.covex</groupId>
        <artifactId>covex-common</artifactId>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Nacos 服务注册/发现 -->
    <dependency>
        <groupId>com.alibaba.nacos</groupId>
        <artifactId>nacos-client</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- Redis + Redisson -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Aviator 表达式引擎 -->
    <dependency>
        <groupId>com.googlecode.aviator</groupId>
        <artifactId>aviator</artifactId>
    </dependency>
    
    <!-- SpringDoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

**注意**：不依赖 covex-service（业务逻辑），只依赖 covex-common（通用工具）。保持规则引擎纯净。

---

## 十、实施计划

### Task RE-1：创建 covex-rule-engine 模块骨架（0.5 天）
- [ ] 创建 Maven 模块 + pom.xml
- [ ] 创建 RuleEngineApplication.java
- [ ] 配置 application.yml（端口 8081、Nacos、MySQL、Redis）
- [ ] 注册到父 pom.xml
- [ ] 验证启动 + Nacos 注册

### Task RE-2：数据模型 + CRUD API（2 天）
- [ ] 创建 ins_rule_table / ins_rule_row / ins_rule_snapshot DDL
- [ ] 创建 Entity / Mapper / Service
- [ ] 实现 RuleTableController（CRUD + 按 domain 查询）
- [ ] 实现 RuleRowController（CRUD + 批量保存 + 逻辑删除）
- [ ] 实现 RuleHistoryController（版本历史 + 快照查询 + 版本对比）
- [ ] Swagger UI 验证

### Task RE-3：规则评估引擎（2 天）
- [ ] 实现 AviatorExpressionCompiler（JSON → Aviator）
- [ ] 实现 RuleEvaluator（加载规则 → 逐行匹配 → 合并结论）
- [ ] 实现 RuleEvalController（POST /api/rule/evaluate）
- [ ] 支持多种 operator（==, !=, >, >=, <, <=, in, contains, exists）
- [ ] 单元测试

### Task RE-4：Redis 缓存 + 热更新（1 天）
- [ ] 规则发布时写入 Redis
- [ ] Redis Pub/Sub 通知机制
- [ ] 本地 Expression 缓存刷新
- [ ] 启动预热逻辑
- [ ] 性能测试

### Task RE-5：前端规则配置中心 + 版本追溯（3.5 天）
- [ ] RuleConfigCenter.vue 主页面布局
- [ ] RuleTree.vue 左侧规则树（按 domain 分组）
- [ ] DecisionTableEditor.vue 决策表编辑器
- [ ] ConditionEditor.vue / ActionEditor.vue 编辑弹窗
- [ ] RuleTestPanel.vue 测试面板
- [ ] 发布按钮 + 发布时生成快照
- [ ] RuleHistoryDialog.vue 版本追溯弹窗（历史版本列表 + 快照只读查看）
- [ ] VersionDiffViewer.vue 版本对比视图（类 Git diff）
- [ ] [📦 产品规则] 按钮（按产品查看规则 + 历史快照）

### Task RE-6：covex-web 集成调用（1 天）
- [ ] ProposalService 调用规则引擎替代 LiteFlow underwriteChain
- [ ] 迁移现有硬编码规则到决策表
- [ ] E2E 验证

### Task RE-7：扩展其他规则域（1 天）
- [ ] 创建 premium 费率规则表示例
- [ ] 创建 claim 理赔规则表示例
- [ ] 前端增加对应 Tab
- [ ] 验证通用性

**总预估**：11 天

---

## 十一、与 v1 方案对比

| 维度 | v1（嵌入 covex-web） | v2（独立微服务） |
|---|---|---|
| 部署 | 随 covex-web 一起 | 独立部署、独立扩缩容 |
| 复用性 | 只能被 covex-web 调用 | 任何服务都可调用 |
| 规则域 | 仅核保 | 核保/费率/理赔/佣金/校验 |
| 资源隔离 | 规则评估影响业务接口 | 独立 JVM，互不影响 |
| 扩展性 | 新增规则域需改 covex-web | 零代码，前端加 Tab 即可 |
| 运维成本 | 低（一个服务） | 中（多一个服务） |
| 适用场景 | 规则简单、调用量小 | 规则复杂、需独立演进 |

**结论**：独立微服务更适合 Covex 的长期发展，规则引擎作为基础设施独立演进。
