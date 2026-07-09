-- Covex 客户域表结构
-- S4+S5: 客户域（5 张表）

-- 1. 客户主表
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ins_customer` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_code` VARCHAR(20)  NOT NULL COMMENT '客户编码（系统生成 UUID 短码）',
    `customer_name` VARCHAR(50)  NOT NULL COMMENT '姓名',
    `id_type`       TINYINT      NOT NULL COMMENT '证件类型：1-身份证 2-护照 3-军官证 4-港澳通行证 5-统一社会信用代码 6-其他',
    `id_no`         VARCHAR(100) NOT NULL COMMENT '证件号码（AES加密存储）',
    `id_expiry`     DATE         DEFAULT NULL COMMENT '证件有效期',
    `gender`        TINYINT      NOT NULL DEFAULT 0 COMMENT '性别：1-男 2-女 0-未知',
    `birth_date`    DATE         DEFAULT NULL COMMENT '出生日期',
    `nationality`   VARCHAR(20)  DEFAULT NULL COMMENT '国籍',
    `phone`         VARCHAR(100) DEFAULT NULL COMMENT '手机号（AES加密存储）',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `customer_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1-个人 2-团体',
    `role_flags`    JSON         DEFAULT NULL COMMENT '角色标记 {"applicant":true,"insured":false,"beneficiary":false}',
    `source`        TINYINT      NOT NULL DEFAULT 1 COMMENT '来源：1-自主注册 2-代理人录入 3-渠道导入 4-第三方平台',
    `attributes`    JSON         DEFAULT NULL COMMENT '扩展属性（团险企业信息等）',
    `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`    DATETIME     DEFAULT NULL COMMENT '删除时间',
    `created_by`    VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by`    VARCHAR(50)  DEFAULT NULL COMMENT '修改人',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id_type_no` (`tenant_id`, `id_type`, `id_no`),
    KEY `idx_customer_code` (`customer_code`),
    KEY `idx_customer_name` (`customer_name`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户主表';

-- 2. 投保人扩展
CREATE TABLE IF NOT EXISTS `ins_customer_applicant` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`             BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_id`           BIGINT        NOT NULL COMMENT '关联 ins_customer.id',
    `annual_income`         DECIMAL(16,2) DEFAULT NULL COMMENT '年收入',
    `income_source`         VARCHAR(50)   DEFAULT NULL COMMENT '收入来源',
    `education_level`       TINYINT       DEFAULT NULL COMMENT '学历：1-高中及以下 2-大专 3-本科 4-硕士及以上',
    `marital_status`        TINYINT       DEFAULT NULL COMMENT '婚姻：1-未婚 2-已婚 3-离异 4-丧偶',
    `has_social_security`   TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否有社保',
    `has_other_insurance`   TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否有其他保险',
    `other_insurance_desc`  VARCHAR(200)  DEFAULT NULL COMMENT '其他保险说明',
    `is_deleted`            TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`            DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`            VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`            VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_customer` (`tenant_id`, `customer_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投保人扩展';

-- 3. 被保人扩展 + 健康档案
CREATE TABLE IF NOT EXISTS `ins_customer_insured` (
    `id`                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`             BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_id`           BIGINT        NOT NULL COMMENT '关联 ins_customer.id',
    `occupation`            VARCHAR(50)   DEFAULT NULL COMMENT '职业',
    `occupation_code`       VARCHAR(10)   DEFAULT NULL COMMENT '职业类别编码',
    `occupation_risk_level` TINYINT       DEFAULT NULL COMMENT '职业风险等级：1-极低 2-低 3-中 4-高 5-极高 6-拒保',
    `smoking_status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0-不吸烟 1-已戒烟 2-吸烟',
    `drinking_status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0-不饮酒 1-已戒酒 2-偶尔 3-经常',
    `bmi`                   DECIMAL(4,1)  DEFAULT NULL COMMENT 'BMI',
    `blood_type`            VARCHAR(5)    DEFAULT NULL COMMENT '血型',
    `medical_history`       JSON          DEFAULT NULL COMMENT '既往病史',
    `family_history`        JSON          DEFAULT NULL COMMENT '家族病史',
    `current_medications`   JSON          DEFAULT NULL COMMENT '当前用药',
    `last_health_update`    DATETIME      DEFAULT NULL COMMENT '健康信息最后更新时间',
    `is_deleted`            TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`            DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`            VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`            VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_customer` (`tenant_id`, `customer_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被保人扩展 + 健康档案';

-- 4. 银行账户
CREATE TABLE IF NOT EXISTS `ins_customer_bank_account` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_id`       BIGINT       NOT NULL COMMENT '关联 ins_customer.id',
    `account_holder`    VARCHAR(50)  NOT NULL COMMENT '账户户名',
    `bank_name`         VARCHAR(100) NOT NULL COMMENT '开户行名称',
    `bank_code`         VARCHAR(20)  DEFAULT NULL COMMENT '银行编码',
    `branch_name`       VARCHAR(100) DEFAULT NULL COMMENT '支行名称',
    `account_no`        VARCHAR(100) NOT NULL COMMENT '银行账号（AES加密存储）',
    `account_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-储蓄卡 2-信用卡 3-对公账户',
    `usage_type`        TINYINT      NOT NULL DEFAULT 3 COMMENT '1-缴费扣款 2-理赔收款 3-两者皆可',
    `is_default`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否默认账户',
    `agreement_no`      VARCHAR(50)  DEFAULT NULL COMMENT '代扣协议编号',
    `agreement_expiry`  DATE         DEFAULT NULL COMMENT '代扣协议到期日',
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '1-正常 2-已冻结 3-已注销',
    `is_deleted`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`        DATETIME     DEFAULT NULL COMMENT '删除时间',
    `created_by`        VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by`        VARCHAR(50)  DEFAULT NULL COMMENT '修改人',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_agreement_no` (`agreement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户银行账户';

-- 5. 联系地址
CREATE TABLE IF NOT EXISTS `ins_customer_address` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_id`   BIGINT       NOT NULL COMMENT '关联 ins_customer.id',
    `address_type`  TINYINT      NOT NULL COMMENT '1-户籍 2-常住 3-工作 4-通讯',
    `province`      VARCHAR(20)  DEFAULT NULL COMMENT '省',
    `city`          VARCHAR(20)  DEFAULT NULL COMMENT '市',
    `district`      VARCHAR(20)  DEFAULT NULL COMMENT '区',
    `detail`        VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    `postal_code`   VARCHAR(10)  DEFAULT NULL COMMENT '邮编',
    `is_default`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否该类型默认',
    `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`    DATETIME     DEFAULT NULL COMMENT '删除时间',
    `created_by`    VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by`    VARCHAR(50)  DEFAULT NULL COMMENT '修改人',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_customer_type` (`tenant_id`, `customer_id`, `address_type`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户联系地址';
