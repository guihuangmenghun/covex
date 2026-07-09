-- V8: 投保单 + 核保记录
-- S9: 承保域

-- 投保单
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ins_proposal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    proposal_no VARCHAR(30) NOT NULL COMMENT '投保单号（全局唯一）',
    product_id BIGINT NOT NULL COMMENT '关联产品',
    channel_id BIGINT DEFAULT NULL COMMENT '渠道商（空=直销）',
    channel_user_id BIGINT DEFAULT NULL COMMENT '销售人员',
    applicant_id BIGINT NOT NULL COMMENT '投保人（关联 ins_customer）',
    insured_id BIGINT NOT NULL COMMENT '被保人（关联 ins_customer）',
    product_snapshot JSON COMMENT '产品快照（从 ins_product 深拷贝）',
    selected_coverages JSON COMMENT '选中的保障责任列表',
    selected_premium_plan JSON COMMENT '选中的缴费计划',
    health_declaration JSON COMMENT '健康告知',
    total_premium DECIMAL(16,2) DEFAULT 0.00 COMMENT '总保费',
    total_sum_insured DECIMAL(16,2) DEFAULT 0.00 COMMENT '总保额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-待校验 2-待核保 3-核保中 4-待支付 5-已支付 6-已出单 7-已拒保 8-已撤销',
    submit_at DATETIME DEFAULT NULL COMMENT '提交时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_proposal_no (proposal_no),
    KEY idx_product_id (product_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_insured_id (insured_id),
    KEY idx_channel_id (channel_id),
    KEY idx_status (status),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投保单';

-- 核保记录
CREATE TABLE IF NOT EXISTS ins_underwriting_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    proposal_id BIGINT NOT NULL COMMENT '投保单ID',
    uw_type TINYINT NOT NULL COMMENT '1-自动核保 2-人工核保',
    uw_result TINYINT NOT NULL COMMENT '1-标准体 2-加费 3-除外 4-延期 5-拒保 6-转人工',
    loading_amount DECIMAL(16,2) DEFAULT NULL COMMENT '加费金额',
    exclusion_desc VARCHAR(500) DEFAULT NULL COMMENT '除外责任描述',
    uw_comment VARCHAR(500) DEFAULT NULL COMMENT '核保备注',
    uw_operator VARCHAR(50) DEFAULT NULL COMMENT '核保操作员',
    uw_at DATETIME DEFAULT NULL COMMENT '核保时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_proposal_id (proposal_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='核保记录';
