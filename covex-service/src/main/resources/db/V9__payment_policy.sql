-- V9: 支付 + 保单
-- S10: 支付域 + 保单域

-- 支付记录
CREATE TABLE IF NOT EXISTS ins_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    payment_no VARCHAR(30) NOT NULL COMMENT '支付流水号',
    policy_id BIGINT DEFAULT NULL COMMENT '保单ID（首期支付时保单尚未生成）',
    proposal_id BIGINT NOT NULL COMMENT '投保单ID',
    payment_type TINYINT NOT NULL DEFAULT 1 COMMENT '1-首期保费 2-续期保费 3-退保金 4-理赔金 5-保单借款',
    amount DECIMAL(16,2) NOT NULL COMMENT '金额',
    pay_channel TINYINT DEFAULT NULL COMMENT '1-微信 2-支付宝 3-银行转账 4-线下',
    pay_channel_no VARCHAR(50) DEFAULT NULL COMMENT '第三方支付流水号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-待支付 2-已支付 3-已退款 4-支付失败/挂起',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_proposal_id (proposal_id),
    KEY idx_policy_id (policy_id),
    KEY idx_status (status),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录';

-- 保单主表
CREATE TABLE IF NOT EXISTS ins_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    policy_no VARCHAR(30) NOT NULL COMMENT '保单号（全局唯一）',
    proposal_id BIGINT NOT NULL COMMENT '投保单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    channel_id BIGINT DEFAULT NULL COMMENT '渠道商',
    applicant_id BIGINT NOT NULL COMMENT '投保人',
    insured_id BIGINT NOT NULL COMMENT '被保人',
    product_snapshot JSON COMMENT '产品快照（从投保单复制）',
    effective_date DATE DEFAULT NULL COMMENT '生效日',
    expiry_date DATE DEFAULT NULL COMMENT '到期日（终身为null）',
    total_premium DECIMAL(16,2) NOT NULL DEFAULT 0.00 COMMENT '总保费',
    total_sum_insured DECIMAL(16,2) NOT NULL DEFAULT 0.00 COMMENT '总保额',
    payment_mode TINYINT DEFAULT NULL COMMENT '1-趸交 2-期交',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 2-中止 3-终止',
    termination_reason TINYINT DEFAULT NULL COMMENT '1-满期 2-退保 3-犹豫期退保 4-理赔终止 5-身故 6-复效超期 7-拒保终止',
    terminated_at DATETIME DEFAULT NULL COMMENT '终止时间',
    beneficiaries JSON COMMENT '受益人列表',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_policy_no (policy_no),
    KEY idx_proposal_id (proposal_id),
    KEY idx_product_id (product_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_insured_id (insured_id),
    KEY idx_status (status),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保单主表';

-- 保单险种明细
CREATE TABLE IF NOT EXISTS ins_policy_coverage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    policy_id BIGINT NOT NULL COMMENT '保单ID',
    coverage_code VARCHAR(20) NOT NULL COMMENT '责任编码',
    coverage_name VARCHAR(100) NOT NULL COMMENT '责任名称',
    sum_insured DECIMAL(16,2) DEFAULT 0.00 COMMENT '保额',
    premium DECIMAL(16,2) DEFAULT 0.00 COMMENT '保费',
    deductible DECIMAL(16,2) DEFAULT 0.00 COMMENT '免赔额',
    coverage_detail JSON COMMENT '保障详情快照',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-有效 2-已终止',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_policy_id (policy_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保单险种明细';

-- 保单缴费计划
CREATE TABLE IF NOT EXISTS ins_policy_premium (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    policy_id BIGINT NOT NULL COMMENT '保单ID',
    premium_plan_code VARCHAR(20) NOT NULL COMMENT '缴费计划编码',
    payment_frequency TINYINT DEFAULT NULL COMMENT '缴费频率',
    payment_term SMALLINT DEFAULT NULL COMMENT '缴费期间',
    payment_term_unit TINYINT DEFAULT NULL COMMENT '期间单位',
    period_premium DECIMAL(16,2) DEFAULT 0.00 COMMENT '每期保费',
    total_periods SMALLINT DEFAULT NULL COMMENT '总期数',
    paid_periods SMALLINT DEFAULT 0 COMMENT '已缴期数',
    next_due_date DATE DEFAULT NULL COMMENT '下期应缴日',
    grace_period SMALLINT DEFAULT NULL COMMENT '宽限天数',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_policy_id (policy_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保单缴费计划';
