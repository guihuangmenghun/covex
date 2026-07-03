-- V13: 保单险种明细 — 添加乐观锁版本号 + 累计已赔付金额

ALTER TABLE ins_policy_coverage
    ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号',
    ADD COLUMN cumulative_paid DECIMAL(16,2) DEFAULT 0 COMMENT '累计已赔付金额';
