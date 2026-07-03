-- Covex 渠道域表结构
-- S6+S7: 渠道域（4 张表）

-- 1. 渠道商
CREATE TABLE IF NOT EXISTS `ins_channel` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `channel_code`    VARCHAR(20)   NOT NULL COMMENT '渠道编码',
    `channel_name`    VARCHAR(100)  NOT NULL COMMENT '渠道名称',
    `channel_type`    TINYINT       NOT NULL COMMENT '类型：1-个人代理 2-直销 3-专业代理 4-银保 5-经纪 6-互联网',
    `license_no`      VARCHAR(50)   DEFAULT NULL COMMENT '许可证号',
    `license_expiry`  DATE          DEFAULT NULL COMMENT '许可证到期日',
    `contact_name`    VARCHAR(50)   DEFAULT NULL COMMENT '联系人',
    `contact_phone`   VARCHAR(100)  DEFAULT NULL COMMENT '联系电话（AES加密）',
    `contact_email`   VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    `region_code`     VARCHAR(20)   DEFAULT NULL COMMENT '区域编码',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1-待审核 2-已签约 3-已暂停 4-已终止',
    `contract_no`     VARCHAR(50)   DEFAULT NULL COMMENT '合同编号',
    `contract_start`  DATE          DEFAULT NULL COMMENT '合同起始日',
    `contract_end`    DATE          DEFAULT NULL COMMENT '合同到期日',
    `attributes`      JSON          DEFAULT NULL COMMENT '扩展属性',
    `is_deleted`      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`      DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`      VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`      VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_channel_code` (`tenant_id`, `channel_code`),
    KEY `idx_channel_name` (`channel_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道商';

-- 2. 渠道-产品授权
CREATE TABLE IF NOT EXISTS `ins_channel_product` (
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
    `channel_id`       BIGINT         NOT NULL COMMENT '渠道商ID',
    `product_id`       BIGINT         NOT NULL COMMENT '产品ID',
    `first_year_rate`  DECIMAL(5,2)   NOT NULL DEFAULT 0.00 COMMENT '首年佣金比例(%)',
    `renewal_rate`     DECIMAL(5,2)   NOT NULL DEFAULT 0.00 COMMENT '续期佣金比例(%)',
    `sale_region`      VARCHAR(100)   DEFAULT NULL COMMENT '销售区域',
    `is_active`        TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_deleted`       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`       DATETIME       DEFAULT NULL COMMENT '删除时间',
    `created_by`       VARCHAR(50)    DEFAULT NULL COMMENT '创建人',
    `updated_by`       VARCHAR(50)    DEFAULT NULL COMMENT '修改人',
    `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_channel_product` (`tenant_id`, `channel_id`, `product_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道-产品授权';

-- 3. 渠道商账号
CREATE TABLE IF NOT EXISTS `ins_channel_user` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `channel_id`        BIGINT        NOT NULL COMMENT '渠道商ID',
    `username`          VARCHAR(50)   NOT NULL COMMENT '登录用户名',
    `password_hash`     VARCHAR(200)  NOT NULL COMMENT '密码哈希',
    `real_name`         VARCHAR(50)   DEFAULT NULL COMMENT '真实姓名',
    `agent_license_no`  VARCHAR(50)   DEFAULT NULL COMMENT '代理人资格证号',
    `phone`             VARCHAR(100)  DEFAULT NULL COMMENT '手机号（AES加密）',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-锁定 3-停用',
    `last_login_at`     DATETIME      DEFAULT NULL COMMENT '最后登录时间',
    `is_deleted`        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`        DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`        VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`        VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    KEY `idx_channel_id` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道商账号';

-- 4. 佣金记录
CREATE TABLE IF NOT EXISTS `ins_commission` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
    `channel_id`        BIGINT         NOT NULL COMMENT '渠道商ID',
    `channel_user_id`   BIGINT         DEFAULT NULL COMMENT '销售人员ID（可空）',
    `policy_id`         BIGINT         NOT NULL COMMENT '关联保单ID',
    `commission_type`   TINYINT        NOT NULL COMMENT '类型：1-首年佣金 2-续期佣金 3-奖金',
    `premium_amount`    DECIMAL(16,2)  NOT NULL DEFAULT 0.00 COMMENT '对应保费金额',
    `commission_rate`   DECIMAL(5,2)   NOT NULL DEFAULT 0.00 COMMENT '佣金比例(%)',
    `commission_amount` DECIMAL(16,2)  NOT NULL DEFAULT 0.00 COMMENT '佣金金额',
    `settle_month`      VARCHAR(7)     DEFAULT NULL COMMENT '结算月份(YYYY-MM)',
    `settle_status`     TINYINT        NOT NULL DEFAULT 1 COMMENT '结算状态：1-待结算 2-已确认 3-已支付',
    `settled_at`        DATETIME       DEFAULT NULL COMMENT '结算时间',
    `commission_no`     VARCHAR(30)    NOT NULL COMMENT '佣金编号（policy_id + commission_type 拼接，用于幂等）',
    `is_deleted`        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`        DATETIME       DEFAULT NULL COMMENT '删除时间',
    `created_by`        VARCHAR(50)    DEFAULT NULL COMMENT '创建人',
    `updated_by`        VARCHAR(50)    DEFAULT NULL COMMENT '修改人',
    `created_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_commission_no` (`tenant_id`, `commission_no`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_policy_id` (`policy_id`),
    KEY `idx_settle_month` (`settle_month`),
    KEY `idx_settle_status` (`settle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金记录';
