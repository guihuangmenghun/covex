-- Covex S8: 产品配置域表结构（10 张新表，ins_dict 已在 V1 创建）

-- 1. 产品主表
CREATE TABLE IF NOT EXISTS `ins_product` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_code`      VARCHAR(20)   NOT NULL COMMENT '产品编码',
    `version`           VARCHAR(20)   NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    `version_status`    TINYINT       NOT NULL DEFAULT 1 COMMENT '版本状态：1-草稿 2-待审批 3-已发布 4-已冻结 5-审批驳回',
    `product_name`      VARCHAR(120)  NOT NULL COMMENT '产品全称',
    `short_name`        VARCHAR(60)   DEFAULT NULL COMMENT '产品简称',
    `product_type`      TINYINT       NOT NULL COMMENT '产品分类：1-寿险 2-意外险 3-健康险 4-车险 5-财产险 6-责任险 7-乘务险',
    `product_nature`    TINYINT       DEFAULT NULL COMMENT '产品性质：1-个人 2-团体 3-银行代理 4-综合',
    `term_type`         TINYINT       DEFAULT NULL COMMENT '期限类型：1-长期 2-一年期 3-极短期',
    `main_rider_flag`   TINYINT       DEFAULT NULL COMMENT '主附险：1-主险 2-附加险 3-两者皆可',
    `sale_channel`      JSON          DEFAULT NULL COMMENT '销售渠道数组',
    `start_date`        DATE          DEFAULT NULL COMMENT '开办日期',
    `end_date`          DATE          DEFAULT NULL COMMENT '停办日期',
    `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '销售状态：1-未上架 2-已上架 3-已下架',
    `capabilities`      JSON          DEFAULT NULL COMMENT '产品能力声明',
    `attributes`        JSON          DEFAULT NULL COMMENT '产品扩展属性',
    `parent_version_id` BIGINT        DEFAULT NULL COMMENT '来源版本ID',
    `is_deleted`        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`        DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`        VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`        VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code_version` (`tenant_id`, `product_code`, `version`),
    KEY `idx_product_type` (`product_type`),
    KEY `idx_version_status` (`version_status`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品主表';

-- 2. 保障定义
CREATE TABLE IF NOT EXISTS `ins_product_coverage` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id`       BIGINT        NOT NULL COMMENT '关联 ins_product.id',
    `coverage_code`    VARCHAR(20)   NOT NULL COMMENT '责任编码',
    `coverage_name`    VARCHAR(100)  NOT NULL COMMENT '责任名称',
    `selection_mode`   TINYINT       NOT NULL DEFAULT 1 COMMENT '选择方式：1-必选 2-可选',
    `benefit_type`     TINYINT       DEFAULT NULL COMMENT '给付类型：1-生存金 2-满期金 3-年金 4-理赔金 5-津贴 6-费用补偿 7-定额给付',
    `coverage_detail`  JSON          DEFAULT NULL COMMENT '保障详细属性',
    `sort_order`       INT           NOT NULL DEFAULT 0 COMMENT '排序',
    `is_deleted`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`       DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`       VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`       VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_coverage_code` (`coverage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保障定义';

-- 3. 缴费规则
CREATE TABLE IF NOT EXISTS `ins_product_premium` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`           BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id`          BIGINT        NOT NULL COMMENT '关联 ins_product.id',
    `premium_plan_code`   VARCHAR(20)   NOT NULL COMMENT '缴费计划编码',
    `premium_plan_name`   VARCHAR(60)   NOT NULL COMMENT '缴费计划名称',
    `payment_frequency`   TINYINT       NOT NULL DEFAULT 12 COMMENT '缴费频率：0-趸交 1-月交 3-季交 6-半年交 12-年交 99-不定期',
    `payment_term`        SMALLINT      DEFAULT NULL COMMENT '缴费期间',
    `payment_term_unit`   TINYINT       DEFAULT 1 COMMENT '期间单位：1-年 2-月 3-日',
    `grace_period`        SMALLINT      DEFAULT 60 COMMENT '宽限天数',
    `rounding_mode`       TINYINT       DEFAULT 1 COMMENT '取整方式：1-四舍五入 2-截断 3-进位',
    `premium_detail`      JSON          DEFAULT NULL COMMENT '缴费扩展属性',
    `is_deleted`          TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`          DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`          VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `updated_by`          VARCHAR(50)   DEFAULT NULL COMMENT '修改人',
    `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_plan_code` (`premium_plan_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费规则';

-- 4. 责任-缴费关联
CREATE TABLE IF NOT EXISTS `ins_coverage_premium_rel` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`     BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `coverage_id`   BIGINT        NOT NULL COMMENT '关联 ins_product_coverage.id',
    `premium_id`    BIGINT        NOT NULL COMMENT '关联 ins_product_premium.id',
    `is_deleted`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_coverage_premium` (`tenant_id`, `coverage_id`, `premium_id`),
    KEY `idx_coverage_id` (`coverage_id`),
    KEY `idx_premium_id` (`premium_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='责任-缴费关联';

-- 5. 规则引用
CREATE TABLE IF NOT EXISTS `ins_product_rule` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`     BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id`    BIGINT        NOT NULL COMMENT '关联 ins_product.id',
    `coverage_id`   BIGINT        DEFAULT NULL COMMENT '关联 ins_product_coverage.id（空=产品级规则）',
    `rule_type`     TINYINT       NOT NULL COMMENT '规则类型：1-核保 2-校验 3-保全 4-退保 5-费率计算 6-给付计算 7-理赔',
    `rule_engine`   VARCHAR(20)   NOT NULL COMMENT '引擎类型：liteflow / aviator / java',
    `rule_code`     VARCHAR(100)  NOT NULL COMMENT '规则编码',
    `rule_name`     VARCHAR(100)  DEFAULT NULL COMMENT '规则名称',
    `sort_order`    INT           NOT NULL DEFAULT 0 COMMENT '执行顺序',
    `is_active`     TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_deleted`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`    DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_coverage_id` (`coverage_id`),
    KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则引用';

-- 6. 主附险关联
CREATE TABLE IF NOT EXISTS `ins_product_rider_rel` (
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`            BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `main_product_code`    VARCHAR(20)   NOT NULL COMMENT '主险产品编码',
    `rider_product_code`   VARCHAR(20)   NOT NULL COMMENT '附加险产品编码',
    `max_rider_count`      SMALLINT      DEFAULT NULL COMMENT '最多附加险数量（空=不限）',
    `is_active`            TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_deleted`           TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`           DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_main_rider` (`tenant_id`, `main_product_code`, `rider_product_code`),
    KEY `idx_main_product_code` (`main_product_code`),
    KEY `idx_rider_product_code` (`rider_product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主附险关联';

-- 7. 条款文档
CREATE TABLE IF NOT EXISTS `ins_product_document` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id`      BIGINT        NOT NULL COMMENT '关联 ins_product.id',
    `document_type`   TINYINT       NOT NULL COMMENT '文档类型：1-产品条款 2-费率表 3-投保须知 4-产品说明书',
    `document_name`   VARCHAR(120)  NOT NULL COMMENT '文档名称',
    `file_url`        VARCHAR(500)  DEFAULT NULL COMMENT '文件存储路径',
    `version`         VARCHAR(20)   DEFAULT NULL COMMENT '文档版本',
    `effective_date`  DATE          DEFAULT NULL COMMENT '生效日期',
    `expiry_date`     DATE          DEFAULT NULL COMMENT '失效日期',
    `is_deleted`      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`      DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`      VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_document_type` (`document_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='条款文档';

-- 8. 变更日志
CREATE TABLE IF NOT EXISTS `ins_product_changelog` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id`        BIGINT        NOT NULL COMMENT '关联 ins_product.id',
    `change_type`       TINYINT       NOT NULL COMMENT '变更类型：1-创建 2-修改 3-发布 4-冻结 5-下架 6-删除',
    `change_target`     VARCHAR(50)   DEFAULT 'product' COMMENT '变更对象',
    `change_target_id`  BIGINT        DEFAULT NULL COMMENT '变更对象ID',
    `field_name`        VARCHAR(50)   DEFAULT NULL COMMENT '变更字段名',
    `old_value`         TEXT          DEFAULT NULL COMMENT '变更前值',
    `new_value`         TEXT          DEFAULT NULL COMMENT '变更后值',
    `operator`          VARCHAR(50)   DEFAULT NULL COMMENT '操作人',
    `operated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark`            VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_product_time` (`tenant_id`, `product_id`, `operated_at`),
    KEY `idx_change_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='变更日志';

-- 9. 费率表元数据
CREATE TABLE IF NOT EXISTS `ins_rate_table` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`        BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
    `rate_table_code`  VARCHAR(30)   NOT NULL COMMENT '费率表编码',
    `rate_table_name`  VARCHAR(100)  NOT NULL COMMENT '费率表名称',
    `product_id`       BIGINT        DEFAULT NULL COMMENT '关联产品（空=公共费率表）',
    `version`          VARCHAR(20)   NOT NULL DEFAULT '1.0.0' COMMENT '费率表版本',
    `table_schema`     JSON          DEFAULT NULL COMMENT '维度定义',
    `effective_date`   DATE          DEFAULT NULL COMMENT '生效日期',
    `expiry_date`      DATE          DEFAULT NULL COMMENT '失效日期',
    `is_deleted`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`       DATETIME      DEFAULT NULL COMMENT '删除时间',
    `created_by`       VARCHAR(50)   DEFAULT NULL COMMENT '创建人',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_rate_table_code` (`rate_table_code`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费率表元数据';

-- 10. 费率表行数据
CREATE TABLE IF NOT EXISTS `ins_rate_table_row` (
    `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rate_table_id`    BIGINT         NOT NULL COMMENT '关联 ins_rate_table.id',
    `dimension_key`    VARCHAR(100)   NOT NULL COMMENT '维度组合键',
    `dimension_json`   JSON           DEFAULT NULL COMMENT '维度原始值',
    `rate_value`       DECIMAL(16,6)  NOT NULL DEFAULT 0.000000 COMMENT '费率值',
    `extra_values`     JSON           DEFAULT NULL COMMENT '其他指标值',
    PRIMARY KEY (`id`),
    KEY `idx_rate_table_dimension` (`rate_table_id`, `dimension_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费率表行数据';
