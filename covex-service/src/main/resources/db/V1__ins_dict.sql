-- Covex 数据字典表
-- S2: 数据字典服务

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ins_dict` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID（0=全局）',
    `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型',
    `dict_code`   VARCHAR(20)  NOT NULL COMMENT '字典值',
    `dict_name`   VARCHAR(100) NOT NULL COMMENT '中文含义',
    `parent_code` VARCHAR(20)  DEFAULT NULL COMMENT '父级编码（层级字典）',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `is_active`   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
    `remark`      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '软删除',
    `deleted_at`  DATETIME     DEFAULT NULL COMMENT '删除时间',
    `created_by`  VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_by`  VARCHAR(50)  DEFAULT NULL COMMENT '修改人',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_type_code` (`tenant_id`, `dict_type`, `dict_code`),
    KEY `idx_type` (`dict_type`),
    KEY `idx_parent` (`parent_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典';
