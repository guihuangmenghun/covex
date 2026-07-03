-- V11: 理赔赔付记录
-- S14: 赔付支付 + 结案

-- 理赔赔付记录
CREATE TABLE IF NOT EXISTS ins_claim_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    claim_id BIGINT NOT NULL COMMENT '理赔案件ID',
    payment_id BIGINT DEFAULT NULL COMMENT '关联支付记录ID（ins_payment）',
    beneficiary_id BIGINT DEFAULT NULL COMMENT '收款人（关联 ins_customer）',
    beneficiary_name VARCHAR(50) DEFAULT NULL COMMENT '收款人姓名',
    amount DECIMAL(16,2) NOT NULL COMMENT '赔付金额',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_claim_id (claim_id),
    KEY idx_payment_id (payment_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='理赔赔付记录';
