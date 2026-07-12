# Covex 开发计划补全交接

> 本文档由全量代码审计生成，包含开发计划中标记 [x] 但实际未落地的全部遗漏项。

## 一、项目基本信息

- **项目路径**：d:\Projects\Covex
- **技术栈**：Spring Boot 3.4.13 + MyBatis-Plus + RocketMQ（rocketmq-spring-boot-starter 2.3.1）+ Redis + Redisson + Aviator 5.4.3
- **Java 版本**：17（D:\jdk-17.0.11）
- **Maven 版本**：3.9.11
- **模块结构**：covex-parent / covex-common / covex-api / covex-service / covex-web

## 二、不可变约束

- 禁止 Hutool，用 Apache Commons + Guava
- 禁止 Logback，用 Log4j2
- 禁止 Drools / QLExpress / Groovy
- 枚举用数字编码（禁止字母）
- 表名 `ins_` 前缀 + snake_case
- 代码变更必须同步更新 docs/ 下相关文档
- Shell 是 Windows PowerShell，不支持 `&&`，用 `;` 分隔命令

---

## 三、全量审计结果

### 3.1 Redis 缓存现状

| Service | 是否缓存 | 问题 |
|---|---|---|
| DictService | ✅ L1 Guava + L2 Redis | 无 |
| RateTableService | ✅ Redis Hash | 无 |
| ClaimService | ✅ Redis 轮转计数器 | 无 |
| IssueCreatePolicyComponent | ✅ Redis 序列号 | 无 |
| **UserService.getUserPermissions()** | ❌ 每次 3 次 DB 查询 | 每次 API 请求都走，高频热路径 |
| **UserService.getUserRoles()** | ❌ 无缓存 | 登录+鉴权都调用 |
| **RoleService.getRolePermissions()** | ❌ 无缓存 | 权限校验时调用 |
| **DataScopeService.getDataScopes()** | ❌ 无缓存 | 数据权限拦截器每次调用 |

### 3.2 MQ 现状

| Topic | Producer | Consumer | 问题 |
|---|---|---|---|
| POLICY_ISSUED | ✅ IssueNotifyComponent | ❌ 无消费者 | 佣金无法自动计算 |
| CLAIM_PAID | ✅ ClaimPaymentService | ❌ 无消费者 | 赔付通知无人处理 |
| PAYMENT_TIMEOUT | ❌ 无 | ❌ 无 | 支付超时未用 MQ 延迟消息 |

### 3.3 定时任务现状

- `@EnableScheduling`：CovexApplication 上已声明 ✅
- `@Scheduled` 方法：**0 个** ❌
- `PaymentService.handlePaymentTimeout()`：仅暴露为 POST `/api/payment/timeout-scan` 手动端点

### 3.4 权限体系现状

- JWT 认证（JwtAuthenticationFilter）：✅ 已实现
- 方法级权限控制（`@RequiresPermission`）：❌ 完全缺失
- 数据权限拦截器（DataPermissionInterceptor）：❌ 完全缺失
- 25 个 Controller 全部无方法级权限注解，任何登录用户可访问任何接口

### 3.5 其他架构问题

- Redisson 分布式锁：依赖存在（redisson-spring-boot-starter），但无任何 RLock/tryLock 代码
- `@Transactional` 缺少 `rollbackFor`：大部分 `@Transactional` 未指定 `rollbackFor = Exception.class`
- 序号生成（ProposalService、PaymentService）：使用 `System.nanoTime() % 1000000`，并发下有碰撞风险

---

## 四、需要实现的 10 个任务

### 任务 1（P0）：PolicyIssuedConsumer — 出单消息消费者

**问题**：POLICY_ISSUED 消息发出后无人消费，佣金无法自动计算

**方案**：

在 `covex-service/src/main/java/com/covex/service/service/` 下创建 `PolicyIssuedConsumer.java`：

- `@RocketMQMessageListener(topic = "POLICY_ISSUED", consumerGroup = "covex-commission-consumer")`
- 实现 `RocketMQListener<String>`
- 消费逻辑：
  1. 解析消息（格式：`"policyNo=xxx, policyId=123, premium=100.00, sumInsured=50000.00"`）
  2. `PolicyMapper` → 查 `ins_policy` 获取 `proposalId`
  3. `ProposalMapper` → 查 `ins_proposal` 获取 `channelId`, `channelUserId`, `totalPremium`, `productId`
  4. `ChannelProductMapper` → 查 `ins_channel_product` 获取 `firstYearRate`
  5. 调用 `CommissionService.calculateCommission(tenantId, policyId, channelId, channelUserId, totalPremium, 1, firstYearRate)`
  6. operator 设为 `"SYSTEM"`
- 幂等已由 CommissionService 保证（`commissionNo = policyId + "-" + commissionType` 去重）
- 需要注入：`PolicyMapper`, `ProposalMapper`, `ChannelProductMapper`, `CommissionService`

### 任务 2（P0）：支付超时 — 改用 RocketMQ 延迟消息

**问题**：支付超时用手动 REST 端点，不是自动化处理

**方案**：

1. 修改 `PaymentService.createPayment()`：创建支付记录后，发送延迟消息
   - topic = `"PAYMENT_TIMEOUT"`
   - 消息内容：`paymentId` + `proposalId`
   - 延迟 30 分钟（rocketmq-spring 的 `syncSend` 或 `Message.setDelayTimeSec`）
2. 创建 `PaymentTimeoutConsumer.java`：
   - `@RocketMQMessageListener(topic = "PAYMENT_TIMEOUT", consumerGroup = "covex-payment-timeout-consumer")`
   - 消费逻辑：查 payment 状态，仍为待支付 → 撤销投保单；已支付 → 忽略（幂等）
3. 保留 `PaymentController` 的 `/api/payment/timeout-scan` 作为手动兜底

### 任务 3（P0）：支付回调丢失补偿

**问题**：支付回调可能丢失，无兜底机制

**方案**：

创建 `PaymentCompensationService.java`：

- 扫描 `status=1`（待支付）且 `createdAt` 超 24 小时的 `ins_payment`
- 将超时记录标记为 `status=5`（挂起），对应投保单改为 `status=8`（已撤销）
- `@Scheduled(cron = "0 0 2 * * ?")` 每天凌晨 2 点执行
- 在 covex-web 下创建 `ScheduledTaskService.java` 统一管理调度

### 任务 4（P0）：权限 Redis 缓存 — PermissionCacheService

**问题**：每次 API 请求都查 DB 获取用户权限（3 次 DB 查询），性能差

**方案**：

创建 `PermissionCacheService.java`：

- `getUserPermissions(Long userId)`：
  - 先查 Redis key `auth:perms:{userId}`（`Set<String>`）
  - 未命中 → 查 DB（`UserService.getUserPermissions`）→ 写 Redis（TTL 30 分钟）
- `getDataScope(Long userId)`：
  - 先查 Redis key `auth:scope:{userId}`
  - 未命中 → 查 DB → 写 Redis（TTL 30 分钟）
- `evictUserPermissions(Long userId)`：删除单个用户缓存
- `evictAllPermissions()`：删除 `auth:perms:*` + `auth:scope:*`

修改点：

- `UserService.assignRoles()` 末尾调用 `evictUserPermissions(userId)`
- `RoleService.assignPermissions()` 末尾调用 `evictAllPermissions()`（角色权限变了，所有用户缓存失效）
- `DataScopeService.setDataScopes()` 末尾调用 `evictAllPermissions()`

### 任务 5（P1）：@RequiresPermission 注解 + AOP 权限校验

**问题**：25 个 Controller 无任何方法级权限控制

**方案**：

1. `covex-common` 下创建 `annotation/RequiresPermission.java`（`code` 属性）
2. `covex-service` 下创建 `aspect/PermissionAspect.java`：
   - `@Around` 拦截 `@RequiresPermission`
   - 从 `SecurityContextHolder` 获取 `userId`
   - 调用 `PermissionCacheService.getUserPermissions(userId)`
   - 校验权限码，不匹配抛 `BizException(403)`
3. 在关键 Controller 方法上添加注解：
   - 产品 CRUD：`product:create`, `product:edit`, `product:delete`, `product:publish`
   - 核保审批：`underwriting:manual`
   - 理赔审核：`claim:review`, `claim:pay`
   - 用户管理：`user:create`, `user:edit`, `user:assign_role`
   - 角色权限：`role:create`, `role:edit`, `role:assign_perm`
   - 佣金结算：`commission:settle`
   - 渠道管理：`channel:create`, `channel:edit`

### 任务 6（P1）：DataPermissionInterceptor 数据权限

**问题**：无数据权限，任何登录用户可看所有数据

**方案**：

创建 `DataPermissionInterceptor.java`（实现 MyBatis-Plus `InnerInterceptor`）：

- 从 `SecurityContextHolder` 获取 `userId`
- 调用 `PermissionCacheService.getDataScope(userId)`
- 根据 `scope_type` 追加 SQL：
  - `1`（全部）→ 不追加
  - `2`（本部门）→ 追加条件
  - `3`（自定义）→ `IN (scope_value)`
- 注册到 `MybatisPlusConfig` 拦截器链（多租户之后、分页之前）

### 任务 7（P1）：@Transactional 补全 rollbackFor

**问题**：大部分 `@Transactional` 未指定 `rollbackFor = Exception.class`，checked 异常不回滚

**方案**：

全局替换：`@Transactional` → `@Transactional(rollbackFor = Exception.class)`

已有 `rollbackFor` 的保持不变（UserService、RoleService）。

涉及文件：ProposalService、PaymentService、ClaimPaymentService、UnderwritingService、ProductService、ChannelService、PolicyService、CommissionService 等

### 任务 8（P2）：序号生成改用 Redis INCR

**问题**：ProposalService、PaymentService 用 `System.nanoTime() % 1000000` 生成序号，并发碰撞

**方案**：

参考 `IssueCreatePolicyComponent` 已有的 Redis 序列号模式：

- `ProposalService.generateProposalNo()` → Redis INCR `proposal_no:{date}`
- `PaymentService.generatePaymentNo()` → Redis INCR `payment_no:{date}`
- `ClaimService.generateClaimNo()` → Redis INCR `claim_no:{date}`

### 任务 9（P2）：Redisson 分布式锁

**问题**：Redisson 依赖存在但无使用，关键并发操作无保护

**方案**：

在以下场景添加 Redisson 分布式锁：

- `PaymentService.handlePaymentCallback()`：防止同一支付单重复回调
- `ClaimPaymentService.processPayment()`：防止同一理赔单重复赔付
- `UserService.assignRoles()`：防止并发角色分配冲突

使用方式：

```java
RLock lock = redissonClient.getLock("lock:payment:callback:" + paymentNo);
if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}
```

### 任务 10（P2）：CLAIM_PAID Consumer

**问题**：CLAIM_PAID 消息发出后无人消费

**方案**：

创建 `ClaimPaidConsumer.java`：

- `@RocketMQMessageListener(topic = "CLAIM_PAID", consumerGroup = "covex-claim-paid-consumer")`
- 消费逻辑：记录赔付通知日志（当前阶段仅需日志，后续可扩展短信/邮件通知）

---

## 五、关键文件路径参考

| 文件 | 路径 |
|---|---|
| UserService | covex-service/src/main/java/com/covex/service/service/UserService.java |
| PermissionService | covex-service/src/main/java/com/covex/service/service/PermissionService.java |
| RoleService | covex-service/src/main/java/com/covex/service/service/RoleService.java |
| DataScopeService | covex-service/src/main/java/com/covex/service/service/DataScopeService.java |
| CommissionService | covex-service/src/main/java/com/covex/service/service/CommissionService.java |
| PaymentService | covex-service/src/main/java/com/covex/service/service/PaymentService.java |
| ClaimPaymentService | covex-service/src/main/java/com/covex/service/service/ClaimPaymentService.java |
| PolicyService | covex-service/src/main/java/com/covex/service/service/PolicyService.java |
| ProposalService | covex-service/src/main/java/com/covex/service/service/ProposalService.java |
| ClaimService | covex-service/src/main/java/com/covex/service/service/ClaimService.java |
| MybatisPlusConfig | covex-service/src/main/java/com/covex/service/config/MybatisPlusConfig.java |
| SecurityConfig | covex-web/src/main/java/com/covex/web/config/SecurityConfig.java |
| JwtAuthenticationFilter | covex-web/src/main/java/com/covex/web/config/JwtAuthenticationFilter.java |
| CovexApplication | covex-web/src/main/java/com/covex/web/CovexApplication.java |
| application.yml | covex-web/src/main/resources/application.yml |
| 开发计划 | docs/Covex开发计划.md |
| 进度报告 | docs/Covex项目进度报告.md |

---

## 六、执行顺序

1. 任务 1（PolicyIssuedConsumer）→ 编译验证
2. 任务 2（支付超时 MQ）→ 编译验证
3. 任务 3（支付补偿定时任务）→ 编译验证
4. 任务 4（PermissionCacheService）→ 编译验证
5. 任务 5（@RequiresPermission + AOP）→ 编译验证
6. 任务 6（DataPermissionInterceptor）→ 编译验证
7. 任务 7（@Transactional rollbackFor 全局替换）→ 编译验证
8. 任务 8（序号改 Redis INCR）→ 编译验证
9. 任务 9（Redisson 分布式锁）→ 编译验证
10. 任务 10（CLAIM_PAID Consumer）→ 编译验证
11. 全部完成后更新 docs/Covex开发计划.md 和 docs/Covex项目进度报告.md

---

## 七、当前 MQ 已就绪的代码

### Producer 端

- `IssueNotifyComponent.java`（covex-service/src/main/java/com/covex/service/liteflow/）
  - 发送 topic=`"POLICY_ISSUED"`
  - 消息格式：`"policyNo=xxx, policyId=123, premium=100.00, sumInsured=50000.00"`
- `ClaimPaymentService.java`（covex-service/src/main/java/com/covex/service/service/）
  - 发送 topic=`"CLAIM_PAID"`
  - 消息格式：`"claimId=123, paidAt=2026-07-05T10:00:00"`

### 配置

- `application.yml` 已配置：
  ```yaml
  rocketmq:
    name-server: 127.0.0.1:9876
    producer:
      group: covex-producer-group
      send-message-timeout: 3000
      retry-times-when-send-failed: 2
  ```

### 关键 Service 方法签名

- `CommissionService.calculateCommission(Long tenantId, Long policyId, Long channelId, Long channelUserId, BigDecimal premiumAmount, Integer commissionType, BigDecimal commissionRate)` — 已有幂等
- `PaymentService.handlePaymentTimeout()` — 逻辑已有：扫描 status=4 且 submitAt 超 30 分钟的投保单→撤销为 status=8
- `ChannelProductEntity` 有 `firstYearRate` / `renewalRate` 字段
- `ProposalEntity` 有 `channelId`, `channelUserId`, `totalPremium`, `productId` 字段
