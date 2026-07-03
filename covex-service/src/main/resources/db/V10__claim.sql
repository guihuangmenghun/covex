-- V10: 理赔域
-- S13: 理赔案件 + 理赔材料 + 理赔审核

-- 理赔案件
CREATE TABLE IF NOT EXISTS ins_claim (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    claim_no VARCHAR(30) NOT NULL COMMENT '理赔案件号',
    policy_id BIGINT NOT NULL COMMENT '保单ID',
    coverage_id BIGINT NOT NULL COMMENT '保单险种明细ID',
    reporter_id BIGINT DEFAULT NULL COMMENT '报案人（关联 ins_customer）',
    reporter_relation TINYINT DEFAULT NULL COMMENT '报案人与被保人关系：1-本人 2-投保人 3-代理人 4-其他',
    accident_date DATE NOT NULL COMMENT '出险日期',
    accident_type VARCHAR(50) DEFAULT NULL COMMENT '事故类型',
    accident_desc VARCHAR(500) DEFAULT NULL COMMENT '事故描述',
    accident_location VARCHAR(200) DEFAULT NULL COMMENT '出险地点',
    claim_amount DECIMAL(16,2) DEFAULT 0.00 COMMENT '申请赔付金额',
    approved_amount DECIMAL(16,2) DEFAULT NULL COMMENT '核准赔付金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-已报案 2-审核中 3-需调查 4-已赔付 5-已拒赔 6-已结案',
    claim_handler VARCHAR(50) DEFAULT NULL COMMENT '理赔员',
    reported_at DATETIME DEFAULT NULL COMMENT '报案时间',
    closed_at DATETIME DEFAULT NULL COMMENT '结案时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_claim_no (claim_no),
    KEY idx_policy_id (policy_id),
    KEY idx_coverage_id (coverage_id),
    KEY idx_reporter_id (reporter_id),
    KEY idx_status (status),
    KEY idx_claim_handler (claim_handler),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='理赔案件';

-- 理赔材料
CREATE TABLE IF NOT EXISTS ins_claim_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    claim_id BIGINT NOT NULL COMMENT '理赔案件ID',
    document_type TINYINT NOT NULL COMMENT '材料类型：1-身份证明 2-医疗单据 3-事故证明 4-死亡证明 5-伤残鉴定 6-其他',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_name VARCHAR(100) DEFAULT NULL COMMENT '文件名',
    uploaded_at DATETIME DEFAULT NULL COMMENT '上传时间',
    uploaded_by VARCHAR(50) DEFAULT NULL COMMENT '上传人',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_claim_id (claim_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='理赔材料';

-- 理赔审核记录
CREATE TABLE IF NOT EXISTS ins_claim_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    claim_id BIGINT NOT NULL COMMENT '理赔案件ID',
    review_type TINYINT NOT NULL COMMENT '审核类型：1-自动审核 2-人工审核 3-调查审核',
    review_result TINYINT DEFAULT NULL COMMENT '结论：1-正常赔付 2-部分赔付 3-拒赔 4-需调查',
    approved_amount DECIMAL(16,2) DEFAULT NULL COMMENT '核准金额',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒赔原因',
    review_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    reviewer VARCHAR(50) DEFAULT NULL COMMENT '审核人',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    created_by VARCHAR(50) DEFAULT NULL,
    updated_by VARCHAR(50) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_claim_id (claim_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='理赔审核记录';
