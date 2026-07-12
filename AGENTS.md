# Covex - AI Agent 项目上下文

> 本文件是 Agent 进入本项目时的自动加载入口。每次新会话开始编码前必须阅读。

## 项目概况

- **项目名**：Covex（保险核心平台）
- **技术栈**：Spring Boot 3.4.13 + Spring Cloud 2024.0.x + Nacos 2.4.3 + RocketMQ 5.3.3 + Redis + LiteFlow + Aviator + MyBatis-Plus + Sentinel + Log4j2
- **数据库**：MySQL 8.4.9
- **总表数**：40 张（产品配置域 11 + 运营域 29）
- **险种**：寿险、车险、财产险、乘务险

## 关键文档（必读）

| 文档 | 路径 | 用途 |
|---|---|---|
| 技术栈总表 | `docs/4-reference/Covex技术栈总表.md` | 18 个组件的版本、用途、Covex 场景 |
| 数据模型（产品配置域） | `docs/4-reference/Covex数据模型-产品配置域.md` | 11 张产品配置表详细字段定义 |
| 运营域需求规格 | `docs/1-specs/operations/Covex运营域需求规格.md` | 用户故事、状态机、端到端流程、异常处理 |
| 数据模型（运营域） | `docs/4-reference/Covex数据模型-运营域.md` | 运营域 29 张表字段定义、类型、索引、实体关系 |
| 业务流程框架 | `docs/4-reference/Covex业务流程框架.md` | 6 阶段业务全景 + 11 个角色 |
| 开发计划 | `docs/3-delivery/20260705/Covex开发计划.md` | 14 个 Story 开发顺序和任务拆解（纯开发，不含测试） |
| 测试计划 | `docs/3-delivery/20260705/Covex测试计划.md` | 11 个测试 Story（对应原始 S1-S15 开发计划） |
| 补充计划 | `docs/3-delivery/20260708/Covex补充计划20260706.md` | P0~P3 补充任务（Bug修复+模板工厂+权限+审批+架构优化） |
| 补充测试计划 | `docs/3-delivery/20260708/Covex补充测试计划20260708.md` | TS0~TS3 补充测试（对应补充计划，由独立测试 Agent 执行） |
| ER 图 | `docs/4-reference/Covex_ER_Diagram.mermaid` | 40 张表关系图 |
| 枚举值方案 | `docs/2-design/data-model/枚举值重构方案.md` | 39 组字典预置数据 |
| 项目枚举值 | `docs/2-design/data-model/项目枚举值.md` | 全量枚举定义（从 ins_dict 表拉取，39 组 175 条） |

## 不可变约束（编码时必须遵守）

### 命名规范
- 表名：`ins_` 前缀 + snake_case
- 字段名：snake_case
- 禁止：LM/LD 前缀、PascalCase、非标准缩写（如 Risk/Duty/Get/Edor/Cal）

### 权限编码规范（强制）
- 权限编码格式：`模块:动作`，动作编码必须使用以下标准词汇：

| 矩阵标记 | 动作编码 | 中文名 | 说明 |
|---|---|---|---|
| R | `read` | 查看 | 只读访问 |
| W | `edit` | 编辑 | 创建+修改合一（对应路由级 RW 权限） |
| ✓ | 具体动作（如 `approve`/`publish`/`confirm`） | 对应中文 | 按钮级操作 |
| — | 无权限 | — | 不可见 |

- **禁止使用 `update` 作为权限动作编码**，统一用 `edit`（与路由权限矩阵 W 语义一致）
- `create` 仅用于**独立创建页面**的路由级权限（如 `/proposal/create`），不与 `edit` 合并
- 新增权限时必须检查：
  - 编码是否已存在于 `ins_permission` 表（禁止重复）
  - Controller 的 `@RequiresPermission` 注解编码是否与数据库一致
  - 前端 `v-permission` 指令编码是否同步

### 数据类型
- 布尔：`TINYINT(1)`，1=是 0=否（禁止 Y/N）
- 枚举：`TINYINT`/`SMALLINT`，全数字编码（禁止字母 L/A/H）
- 灵活属性：`JSON` 类型
- 金额：`DECIMAL(16,2)`，费率：`DECIMAL(16,6)`

### 字典表同步规范（强制）
- 新增/修改状态码、类型码等枚举值时，**必须同步更新** `ins_dict` 字典表
- 种子数据：修改 `V2__dict_seed_data.sql`（新部署生效）
- 增量迁移：创建新的 `V{N}__xxx.sql` 执行 `UPDATE/INSERT`（已有数据库生效）
- 前端下拉框、状态标签等显示文本以字典表为准，禁止前端硬编码状态名称

### SQL 迁移文件规范（强制）
- **文件头注释**（必须包含）：
  1. `-- V{N}: 简要描述`
  2. `-- 对应计划：{计划文件名} → {Task/Story 编号}`（如 `Covex补充计划20260709.md → Task 0.6`）
  3. `-- 根因：...`（仅 Bug 修复类 SQL 需要）
  4. `-- 执行方式：mysql -u root -p covex --default-character-set=utf8mb4 < V{N}__xxx.sql`
- **字符集声明**：文件头必须添加 `SET NAMES utf8mb4;`
- **导入 SQL 文件时**：必须指定字符集 `--default-character-set=utf8mb4`
- 示例：
  ```sql
  -- V24: 修复 ins_product.sale_channel JSON 格式脏数据
  -- 对应计划：Covex补充计划20260709.md → 第三轮测试 P0-5 修复
  -- 根因：部分早期产品的 sale_channel 存为 "1,2"（逗号分隔字符串）
  -- 执行方式：mysql -u root -p covex --default-character-set=utf8mb4 < V24__fix_sale_channel_json.sql

  SET NAMES utf8mb4;

  UPDATE ins_product SET sale_channel = '["1","2"]' WHERE sale_channel = '"1,2"' AND is_deleted = 0;
  ```
  ```bash
  # 导入 SQL 文件
  mysql -u root -p covex --default-character-set=utf8mb4 < V19__xxx.sql
  ```
- **原因**：避免中文乱码问题（UTF-8 字节被当作 latin1 存储）；计划来源标注便于追溯需求和审计覆盖度

### 禁止组件
- ❌ Drools / JVS-Rules（用 LiteFlow）
- ❌ QLExpress / Groovy（用 Aviator）
- ❌ Hutool（用 Apache Commons + Guava）
- ❌ Logback（用 Log4j2）
- ❌ Springfox / Knife4j（用 springdoc-openapi + Swagger UI）
- ❌ Spring Cloud Alibaba 全家桶（用 Nacos 独立客户端）

### 通用表字段（所有表必须包含）
```sql
id BIGINT AUTO_INCREMENT PRIMARY KEY,
tenant_id BIGINT NOT NULL,
is_deleted TINYINT(1) DEFAULT 0,
deleted_at DATETIME,
created_by VARCHAR(50),
updated_by VARCHAR(50),
created_at DATETIME,
updated_at DATETIME
```

### 规则引擎
- 流程编排：LiteFlow（EL 表达式存 Nacos）
- 表达式计算：Aviator（费率查表走 Redis → DB 二级查询）
- 规则引用：`ins_product_rule.rule_engine` = liteflow/aviator/java

### 数据快照规则
- 投保时从产品配置域快照到投保单
- 出单时从投保单实例化到保单
- 保单独立于产品配置（产品下架不影响已有保单）

## 本地开发环境

- Java 17.0.11（D:\jdk-17.0.11）
- Maven 3.9.11
- MySQL 8.4.9（本地安装）
- Redis 5.0.14（本地安装，D:\Program Files\Redis）
- RocketMQ 5.3.3（本地安装）
- Nacos 2.4.3（D:\Nacos，启动：双击 D:\Nacos\start-nacos.bat）
- IDE：IntelliJ IDEA（D:\ideaIU）

## 编码前检查清单

```
□ 命名规范？（ins_ 前缀，snake_case）
□ 通用字段？（tenant_id, is_deleted, 审计字段）
□ 枚举用数字？（不是字母）
□ 布尔用 TINYINT(1)？（不是 Y/N）
□ 规则用 LiteFlow/Aviator？（不是 Drools）
□ 没使用禁止组件？
□ SQL 文件头有 SET NAMES utf8mb4？
□ 权限编码用 edit 不用 update？与 ins_permission 表一致？
□ 工具类 JSON 参数有多层类型适配？（禁令 1）
□ 校验逻辑参数缺失时报错而非静默跳过？（禁令 2）
□ catch 块有 log.warn 而非空注释？（禁令 3）
□ 需要更新文档？
```

## 测试执行前置条件（强制）

```
1. 编译整个项目：mvn clean install -DskipTests（多模块项目必须全量编译）
2. 重启整个服务：停止后端 Java 进程，重新启动 java -jar covex-web-1.0.0-SNAPSHOT.jar
3. 确认前端已启动：npm run dev（端口 3000）
4. 确认后端已启动：访问 http://localhost:8080/swagger-ui/index.html 返回 200

禁止：在未重启服务的情况下执行测试（旧进程可能加载旧代码，导致测试结果不可信）
```

## 测试 Agent API 调用规范（强制）

**核心原则：所有 API 调用必须以 OpenAPI 规范为唯一权威源，禁止凭记忆或 REST 惯例猜测。**

### 前置步骤（每次 API 测试前必须执行）

测试前先获取完整 API 规范，再发起实际测试请求：

```
步骤 1：获取 OpenAPI 规范（一次请求拿全部）
  方式 A（浏览器 Agent）：通过 evaluate_script 执行
    const res = await fetch('http://localhost:8080/v3/api-docs');
    const spec = await res.json();
  方式 B（终端/脚本 Agent）：通过 curl 或 Invoke-RestMethod 执行
    GET http://localhost:8080/v3/api-docs
  返回 JSON 包含所有接口路径、方法、请求体 Schema、响应结构

步骤 2：从规范中提取目标接口信息
  在 spec.paths 中查找目标接口，确认：
  - 精确路径（如 /api/payment/create，而非 /api/payment）
  - HTTP 方法（POST/PUT/GET/DELETE）
  - Request Body Schema 字段名（如 templateCode，而非 templateId）
  - 路径参数名（如 {proposalId}，而非 {id}）
  - 响应结构（code/data/message）

步骤 3：用确认的信息发起实际测试请求

步骤 4：如果 /v3/api-docs 不可用，退化为读取 Controller 源码：
  在 covex-web/src/main/java/com/covex/web/controller/ 目录下
  找到对应 Controller，读取 @RequestMapping + @PostMapping 等注解
```

> **为什么用 /v3/api-docs 而不用浏览器点开 Swagger UI？**
> - 一次 HTTP 请求获取全部接口规范（~120KB JSON），比逐页点开 Swagger UI 省 Token
> - 结构化 JSON 便于程序化提取目标接口，无需视觉识别
> - 浏览器 Agent 可通过 evaluate_script 执行 fetch，终端 Agent 可直接 curl/Invoke-RestMethod

### 禁止行为

- ❌ **禁止猜测接口路径**：不能假设 REST 惯例（如 `POST /api/payment`），实际可能是 `POST /api/payment/create`
- ❌ **禁止猜测字段名**：不能假设 `templateId`/`id`/`name` 等常见命名，必须从 OpenAPI Schema 确认
- ❌ **禁止猜测枚举值**：状态码、类型码必须从字典表或枚举定义确认，不能假设从 0 或 1 开始
- ❌ **禁止使用过时的记忆**：上一次会话的接口信息可能已变更，每次必须重新从 /v3/api-docs 读取

### 已知的非标准接口（易踩坑）

| 接口 | 正确路径/字段 | 常见错误 |
|------|-------------|----------|
| 模板创建产品 | `POST /api/product-template/create-product`，body: `{"templateCode":"TERM_LIFE",...}` | ~~`templateId: 1`~~ |
| 核保审批 | `POST /api/underwriting/manual/{proposalId}` | ~~`PUT /api/underwriting/{id}/approve`~~ |
| 创建支付 | `POST /api/payment/create` | ~~`POST /api/payment`~~ |
| 用户登录 | `POST /api/user/login` | ~~`POST /api/auth/login`~~ |

### 测试执行检查清单

```
执行每个 API 测试前：
□ 是否已从 /v3/api-docs 确认了接口路径？
□ 是否已确认请求体的所有字段名与 OpenAPI Schema 一致？
□ 是否已确认路径参数名和方法正确？
□ 如果是非标准路径，是否在测试计划中显式标注了？
```

## 问题排查三步法（强制）

```
1. 确认部署：改的代码是否真正部署到了运行环境？
   - 检查 mvn install 是否执行（多模块项目必须 install 非启动模块）
   - 检查日志中的代码行号是否与源码一致
2. 查日志：错误堆栈指向哪里？哪个类的哪一行？
3. 查需求：需求规格对这块的描述是什么？应该怎么做？

禁止：跳过第 1 步直接分析框架源码。
```

## 修复后自省（强制）

Agent 在修复 Bug 后必须调用 `/post-fix-review` Skill 进行结构化自省。

**强制触发时机**：
1. 修复任何 Bug 后（P0–P3）
2. 新增校验/拦截逻辑后
3. 编写工具类/Helper 后
4. 发现静默失效问题后

**自省流程**：5 个结构化问题（根因 → 反模式 → 通用化 → 规范提取 → 记忆更新）

**输出动作**：
- 如果反模式可泛化为通用规则 → 写入“编码质量三禁令”或新增禁令
- 如果需要跨项目记住 → 创建/更新 Memory
- 始终输出 Summary 表格

## 编码质量三禁令（强制）

### 禁令 1：工具类禁止只处理单一预期类型

工具类方法接收 `Object`/JSON 类型参数时，必须实现多层类型适配，禁止假设输入一定是某种特定类型：

```java
// ✅ 正确：多层类型适配（Map → String(JSON) → null）
public static Map<String, Object> extractJsonField(Map<String, Object> source, String key) {
    Object value = source.get(key);
    if (value instanceof Map) return (Map<String, Object>) value;
    if (value instanceof String) {
        try { return objectMapper.readValue((String) value, new TypeReference<>() {}); }
        catch (Exception ignored) {}
    }
    return null;
}

// ❌ 错误：只处理 Map，其他类型静默返回 null
if (value instanceof Map) return (Map) value;
return null;
```

**原因**：ORM/JSON 框架对嵌套字段的反序列化结果类型不可控（可能为 Map、String、LinkedHashMap 等），工具类必须防御性处理。若直接返回 null，调用方会使用默认值，导致业务逻辑静默偏离预期。

### 禁令 2：校验逻辑禁止静默跳过

校验/拦截器在关键参数缺失时，必须明确报错并阻止流程，禁止用 `if (x != null)` 包裹整段校验逻辑：

```java
// ✅ 正确：参数缺失时报错阻止流程
if (entity == null) {
    context.addError("实体信息未加载，无法执行校验");
    return;
}
if (entity.getEffectiveDate() == null) {
    context.addError("生效日期缺失，无法执行校验");
    return;
}
// 正常校验逻辑
if (entity.getAmount().compareTo(limit) > 0) context.addError("...");

// ❌ 错误：条件不满足时整个校验被跳过，不报错不日志
if (entity != null && entity.getEffectiveDate() != null) {
    if (entity.getAmount().compareTo(limit) > 0) context.addError("...");
}
```

**原因**：参数缺失 ≠ "不需要校验"，而是 "校验前置条件不满足，应报错阻止流程继续"。静默跳过会导致不合法数据通过校验。

### 禁令 3：异常处理禁止空 catch

数据加载、外部调用等可能失败的方法中，catch 块必须记录 warn 级别日志，禁止用空注释替代：

```java
// ✅ 正确：记录异常上下文便于排查
try {
    context.setEntity(entityService.getById(id));
} catch (Exception e) {
    log.warn("加载实体失败 id={}: {}", id, e.getMessage());
}

// ❌ 错误：空注释吞掉异常，调用方完全无感知
try {
    context.setEntity(entityService.getById(id));
} catch (Exception e) {
    // Entity may not exist
}
```

**原因**：空 catch → 变量为 null → 触发禁令 2 的静默跳过 → 校验完全失效且无法排查。这是“静默失效链”的典型根因。

## 跨服务集成检查清单

开发计划中涉及“A 触发 B”的任务时，必须检查：

```
□ A 的代码中是否有触发 B 的调用/MQ/事件？
□ B 的消费者/监听器是否已创建？
□ 失败时 B 是否有兆底策略？
□ 端到端测试是否覆盖了 A→B 的完整链路？
□ 需求规格中的 AC 是否逐条映射到开发计划任务？
```
