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
| 技术栈总表 | `docs/Covex技术栈总表.md` | 18 个组件的版本、用途、Covex 场景 |
| 数据模型（产品配置域） | `docs/Covex数据模型-产品配置域.md` | 11 张产品配置表详细字段定义 |
| 运营域需求规格 | `docs/Covex运营域需求规格.md` | 用户故事、状态机、端到端流程、异常处理 |
| 数据模型（运营域） | `docs/Covex数据模型-运营域.md` | 运营域 29 张表字段定义、类型、索引、实体关系 |
| 业务流程框架 | `docs/Covex业务流程框架.md` | 6 阶段业务全景 + 11 个角色 |
| 开发计划 | `docs/Covex开发计划.md` | 14 个 Story 开发顺序和任务拆解（纯开发，不含测试） |
| 测试计划 | `docs/Covex测试计划.md` | 11 个测试 Story（对应原始 S1-S15 开发计划） |
| 补充计划 | `docs/Covex补充计划20260706.md` | P0~P3 补充任务（Bug修复+模板工厂+权限+审批+架构优化） |
| 补充测试计划 | `docs/Covex补充测试计划20260708.md` | TS0~TS3 补充测试（对应补充计划，由独立测试 Agent 执行） |
| ER 图 | `docs/Covex_ER_Diagram.mermaid` | 40 张表关系图 |
| 枚举值方案 | `docs/枚举值重构方案.md` | 39 组字典预置数据 |

## 不可变约束（编码时必须遵守）

### 命名规范
- 表名：`ins_` 前缀 + snake_case
- 字段名：snake_case
- 禁止：LM/LD 前缀、PascalCase、非标准缩写（如 Risk/Duty/Get/Edor/Cal）

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

### SQL 字符集规范（强制）
- **创建 SQL 文件时**：文件头必须添加 `SET NAMES utf8mb4;`
- **导入 SQL 文件时**：必须指定字符集 `--default-character-set=utf8mb4`
- 示例：
  ```sql
  -- V19__xxx.sql
  SET NAMES utf8mb4;
  
  UPDATE ins_role SET role_name = '管理员' WHERE role_code = 'admin';
  ```
  ```bash
  # 导入 SQL 文件
  mysql -u root -p covex --default-character-set=utf8mb4 < V19__xxx.sql
  ```
- **原因**：避免中文乱码问题（UTF-8 字节被当作 latin1 存储）

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
□ 需要更新文档？
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

## 跨服务集成检查清单

开发计划中涉及“A 触发 B”的任务时，必须检查：

```
□ A 的代码中是否有触发 B 的调用/MQ/事件？
□ B 的消费者/监听器是否已创建？
□ 失败时 B 是否有兆底策略？
□ 端到端测试是否覆盖了 A→B 的完整链路？
□ 需求规格中的 AC 是否逐条映射到开发计划任务？
```
