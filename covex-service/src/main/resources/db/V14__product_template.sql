-- ============================================================
-- V14: 产品模板表 + 产品表新增模板溯源字段
-- ============================================================

-- 1. 新增表：ins_product_template
CREATE TABLE IF NOT EXISTS ins_product_template (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT       NOT NULL DEFAULT 0      COMMENT '租户ID（0=系统模板）',
    template_code   VARCHAR(30)  NOT NULL                 COMMENT '模板编码，如 TERM_LIFE',
    template_name   VARCHAR(100) NOT NULL                 COMMENT '模板名称',
    template_desc   VARCHAR(500) DEFAULT NULL             COMMENT '模板说明',
    product_type    TINYINT      NOT NULL                 COMMENT '对应产品分类（1-寿险 2-意外 3-健康 4-车险 5-财产 6-责任 7-乘务）',
    icon            VARCHAR(50)  DEFAULT NULL             COMMENT '前端展示图标',
    sort_order      INT          NOT NULL DEFAULT 0       COMMENT '排序',
    is_active       TINYINT(1)   NOT NULL DEFAULT 1       COMMENT '是否启用',
    template_data   JSON         NOT NULL                 COMMENT '完整模板数据（product/coverages/premiums/rules）',
    param_schema    JSON         NOT NULL                 COMMENT 'PM需要填写的参数定义（前端渲染表单用）',
    created_by      VARCHAR(50)  DEFAULT NULL             COMMENT '创建人',
    updated_by      VARCHAR(50)  DEFAULT NULL             COMMENT '修改人',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    deleted_at      DATETIME     DEFAULT NULL             COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_template_code (tenant_id, template_code),
    KEY idx_product_type (product_type),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品模板表';

-- 2. 产品表新增模板溯源字段
ALTER TABLE ins_product
    ADD COLUMN template_source TINYINT DEFAULT 1 COMMENT '产品来源：1-空白创建 2-系统模板 3-公司模板 4-克隆产品' AFTER parent_version_id,
    ADD COLUMN template_ref_id BIGINT  DEFAULT NULL COMMENT '来源模板ID或来源产品ID' AFTER template_source;
