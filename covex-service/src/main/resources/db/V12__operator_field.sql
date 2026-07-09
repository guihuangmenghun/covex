-- V12: 为运营域表补充通用操作人字段（operator）
-- 已有专用操作人字段的表（如 ins_underwriting_record.uw_operator）不做修改

SET NAMES utf8mb4;

ALTER TABLE ins_proposal ADD COLUMN operator VARCHAR(50) COMMENT '投保录入人';
ALTER TABLE ins_payment ADD COLUMN operator VARCHAR(50) COMMENT '支付确认人';
ALTER TABLE ins_commission ADD COLUMN operator VARCHAR(50) COMMENT '佣金确认人';
ALTER TABLE ins_endorsement ADD COLUMN operator VARCHAR(50) COMMENT '保全执行人';
ALTER TABLE ins_renewal_bill ADD COLUMN operator VARCHAR(50) COMMENT '续期处理人';
ALTER TABLE ins_policy_loan ADD COLUMN operator VARCHAR(50) COMMENT '借款处理人';
ALTER TABLE ins_claim_payment ADD COLUMN operator VARCHAR(50) COMMENT '赔付处理人';
