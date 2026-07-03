# Covex 技术栈总表（2026.7 确认版）

> 每个组件标注了「做什么」和「Covex 里用在哪」，开发时遇到相关场景可以直接查。

---

## 一、基础框架层

### Spring Boot 3.5.16
- **做什么**：Java Web 应用的基础框架，提供自动配置、内嵌服务器、依赖管理
- **Covex 场景**：整个项目的基座，所有微服务都基于它

### Spring Cloud 2024.0.x
- **做什么**：微服务基础设施（服务发现、负载均衡、网关路由）
- **Covex 场景**：Spring Cloud Gateway 做统一入口路由，LoadBalancer 做服务间调用负载均衡

### Spring AI Alibaba 1.1.2.0
- **做什么**：Spring 生态的 AI 集成框架，对接通义千问等大模型
- **Covex 场景**：
  - 产品配置域：上传保险条款 PDF → AI 抽取结构化数据（投保年龄/保障责任/免赔额/等待期）
  - 核保域：智能核保辅助（根据健康告知 + 历史数据给核保建议）
  - 理赔域：理赔材料 OCR 识别 + 信息抽取

---

## 二、中间件层

### Nacos 2.4.3（独立客户端接入，不用 SCA 全家桶）
- **做什么**：服务注册中心 + 配置中心
- **Covex 场景**：
  - 注册中心：所有微服务启动时注册到 Nacos，服务间调用通过服务名发现
  - 配置中心：LiteFlow 规则链 EL 表达式存 Nacos，热更新不用重启；Aviator 表达式存 Nacos；数据字典缓存刷新通知

### RocketMQ 5.3.3
- **做什么**：消息队列，异步解耦、削峰填谷
- **Covex 场景**：
  - 出单成功后异步触发：生成电子保单、发送通知、计算佣金、更新累计保额
  - 佣金结算：出单消息 → 佣金服务消费 → 计算佣金
  - 字典变更通知：字典更新 → 发 MQ 消息 → 各服务刷新本地缓存

### Redis 5.0.14 + Redisson
- **做什么**：Redis 是内存缓存；Redisson 是 Redis 的 Java 客户端，提供分布式锁、分布式集合等高级功能
- **Covex 场景**：
  - 费率表缓存：`ins:rate:{code}:{version}` Hash 结构，Aviator 计算保费时先查 Redis
  - 数据字典缓存：`dict:{type}` 缓存字典值，避免每次查库
  - 分布式锁：支付回调幂等（Redisson 锁 payment_no）、保单号生成（Redisson 锁防重复）
  - 投保单防重提交：`lock:proposal:{id}` 防止同一投保单重复提交

---

## 三、规则引擎层

### LiteFlow
- **做什么**：轻量级流程编排引擎，用 EL 表达式定义业务链路，组件化编排
- **Covex 场景**：
  - `validate` 链：投保校验（年龄→证件→保额→主附险→渠道授权）
  - `underwrite` 链：核保规则（健康评估→职业风险→累计保额）
  - `issue` 链：出单流程（创建保单→险种明细→缴费计划→通知）
  - `claim` 链：理赔流程（校验→计算→审核→赔付）
  - `endorsement` 链：保全变更流程
  - `renewal` 链：续期催缴流程
- **关键点**：规则链定义存 Nacos，Java 组件实现 LiteflowComponent 接口

### Aviator
- **做什么**：高性能表达式引擎，编译成字节码执行，比 Groovy/SpEL 快
- **Covex 场景**：
  - 保费计算：`premium = sumInsured * rate(age, gender, term)`
  - 退保金计算：`surrenderValue = cashValue(paidYears, premium) - deductions`
  - 赔付计算：`claim = (loss - deductible) * claimRatio`
  - 费率查表：自定义函数 `rateLookup(tableCode, version, dimensionKey)` → Redis HGET

---

## 四、数据访问层

### MyBatis-Plus
- **做什么**：MyBatis 的增强工具，提供 CRUD 自动生成、分页、多租户、逻辑删除等
- **Covex 场景**：
  - 所有 40 张表的 CRUD 操作（自动生成 Mapper 方法，不用手写 SQL）
  - 多租户拦截器：`TenantLineInnerInterceptor` 自动追加 `WHERE tenant_id = ?`
  - 逻辑删除：`@TableLogic` 注解，DELETE 变 UPDATE is_deleted=1
  - 自动填充：`MetaObjectHandler` 自动填充 created_at/updated_at/created_by

### Druid 1.2.25
- **做什么**：数据库连接池 + SQL 监控
- **Covex 场景**：
  - 连接池管理（比 Spring Boot 默认 HikariCP 多了监控面板）
  - 慢 SQL 监控：发现查询性能问题
  - SQL 防注入：内置防火墙过滤器

---

## 五、可观测层

### Log4j2
- **做什么**：日志框架（替代 Spring Boot 默认的 Logback），异步性能强 10-18 倍
- **Covex 场景**：
  - 全链路日志：每条日志自动带 traceId（串联投保→核保→出单全流程）
  - 异步日志：开门红万级 TPS 时不阻塞业务线程
  - 结构化输出：JSON 格式日志，方便后续 ELK 聚合分析
  - 审计日志：支付、核保、理赔等关键操作单独输出到 audit.log

### Micrometer Tracing + Zipkin
- **做什么**：分布式链路追踪。Micrometer Tracing 是 API 层（Spring Boot 3.x 内置），Zipkin 是可视化后端
- **Covex 场景**：
  - 追踪一次投保请求经过了哪些服务、每步耗时多少
  - 发现性能瓶颈（比如核保链某个规则执行过慢）
  - 排查问题（某笔支付回调为什么没触发后续流程）

### springdoc-openapi 2.8.6 (Swagger UI)
- **做什么**：自动生成 API 文档。springdoc-openapi 生成 OpenAPI 规范，Swagger UI 提供可视化界面
- **Covex 场景**：
  - 所有 Controller 自动生成在线 API 文档
  - 前端/移动端开发者直接在 Swagger UI 界面调试接口
  - 接口参数、返回值、状态码一目了然

---

## 六、限流与调度

### Sentinel 1.8.8
- **做什么**：流量控制、熔断降级、系统负载保护
- **Covex 场景**：
  - 投保接口限流：开门红期间限制每秒投保请求数，防止系统被打垮
  - 核保接口熔断：如果核保规则引擎响应过慢，自动熔断返回"转人工"
  - 支付回调限流：防止支付通道批量回调冲垮系统
  - 规则存 Nacos：限流规则热更新，不用重启

### Spring @Scheduled（初期）→ Quartz（后期）
- **做什么**：定时任务调度
- **Covex 场景**：
  - 续期账单生成：每天扫描 next_due_date，提前 30 天生成账单
  - 缴费提醒：T-30/T-7/T-3/T日发送提醒
  - 支付超时扫描：每 5 分钟扫描超时未支付的投保单 → 自动撤销
  - 代扣协议到期提醒：提前 30 天提醒重新签约
  - 月度佣金结算：每月 1 日 00:00 汇总上月佣金

---

## 七、工具库

### MapStruct 1.6.3
- **做什么**：编译期生成对象映射代码，DTO ↔ Entity 转换，比手写 getter/setter 快且类型安全
- **Covex 场景**：
  - `ProposalCreateDTO` → `InsProposal` Entity
  - `InsPolicy` Entity → `PolicyDetailVO`（返回给前端的视图对象）
  - `InsCustomer` → `CustomerSearchResultDTO`
  - 性能：编译期生成代码，运行时无反射开销

### Apache Commons
- **做什么**：Apache 出品的通用工具库，按功能模块化
- **Covex 场景**：
  - `commons-lang3`：`StringUtils`（字符串判空/截取）、`NumberUtils`（数字转换）、`RandomStringUtils`（生成保单号随机部分）
  - `commons-codec`：`DigestUtils`（MD5/SHA 签名）、`Base64`（编解码）
  - `commons-collections4`：`CollectionUtils`（集合判空/合并）、`MapUtils`
  - `commons-io`：`FileUtils`（文件读写）、`IOUtils`（流操作）
  - `commons-validator`：邮箱/URL 格式校验

### Google Guava
- **做什么**：Google 出品的 Java 核心工具库，性能优秀，API 设计精良
- **Covex 场景**：
  - `Preconditions`：参数校验（`checkNotNull`、`checkArgument`），比手写 if-throw 简洁
  - `CacheBuilder`：本地缓存（字典值、产品配置等热数据本地缓存，减少 Redis 调用）
  - `RateLimiter`：单机限流（配合 Sentinel 做双层限流）
  - `Hashing`：一致性哈希（数据分片）
  - `ImmutableList/Map`：不可变集合（线程安全的常量数据）

---

## 八、组件速查表

| 开发场景 | 用什么 | 怎么用 |
|---|---|---|
| 需要写一条 SQL 查询 | MyBatis-Plus | `mapper.selectList(queryWrapper)` |
| 需要加分布式锁 | Redisson | `RLock lock = client.getLock(key); lock.tryLock()` |
| 需要缓存数据到 Redis | Spring Data Redis | `redisTemplate.opsForHash().put(key, field, value)` |
| 需要本地缓存 | Guava CacheBuilder | `CacheBuilder.newBuilder().expireAfterWrite(10, MINUTES).build()` |
| 需要发送异步消息 | RocketMQ | `rocketMQTemplate.send(topic, message)` |
| 需要编排业务流程 | LiteFlow | EL 表达式定义链路 + Java 组件实现节点 |
| 需要计算公式/表达式 | Aviator | `AviatorEvaluator.execute(expression, env)` |
| 需要限流保护 | Sentinel | `@SentinelResource(value="xxx", blockHandler="fallback")` |
| 需要定时执行 | @Scheduled | `@Scheduled(cron = "0 0 1 * * ?")` |
| 需要 DTO ↔ Entity 转换 | MapStruct | 定义 Mapper 接口 + `@Mapper` 注解 |
| 需要字符串/数字工具 | Apache Commons | `StringUtils.isBlank(str)` / `NumberUtils.toInt(str)` |
| 需要参数校验 | Guava Preconditions | `Preconditions.checkNotNull(obj, "xxx不能为空")` |
| 需要加密 | Apache Commons Codec | `DigestUtils.sha256Hex(data)` |
| 需要记录日志 | SLF4J + Log4j2 | `log.info("保单{}出单成功", policyNo)` |
| 需要追踪调用链 | Micrometer Tracing | 自动注入，日志自带 traceId |
| 需要 AI 调用 | Spring AI Alibaba | `ChatClient.prompt("...").call().content()` |
| 需要 API 文档 | springdoc-openapi | Controller 加 `@Tag` + `@Operation` 注解 |
| 需要操作数据库事务 | Spring @Transactional | `@Transactional(rollbackFor = Exception.class)` |
