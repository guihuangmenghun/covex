-- V21: 字典表补全 — 修复缺失字典 + 对齐前端硬编码
-- 对应 Task 0.2.1: 字典表补全

SET NAMES utf8mb4;

-- ========== 1. 新增 commission_status（佣金结算状态）==========
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'commission_status', '1', '待结算', 1),
(0, 'commission_status', '2', '已确认', 2),
(0, 'commission_status', '3', '已支付', 3),
(0, 'commission_status', '4', '已驳回', 4);

-- ========== 2. 新增 pay_channel（支付渠道）==========
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'pay_channel', '1', '微信', 1),
(0, 'pay_channel', '2', '支付宝', 2),
(0, 'pay_channel', '3', '银行转账', 3),
(0, 'pay_channel', '4', '线下', 4);

-- ========== 3. 新增 payment_mode（缴费方式）==========
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'payment_mode', '1', '趸交', 1),
(0, 'payment_mode', '2', '期交', 2);

-- ========== 4. 新增 change_type（变更操作类型）==========
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'change_type', '1', '创建', 1),
(0, 'change_type', '2', '更新', 2),
(0, 'change_type', '3', '删除', 3),
(0, 'change_type', '4', '发布', 4),
(0, 'change_type', '5', '冻结', 5),
(0, 'change_type', '6', '克隆', 6);

-- ========== 5. 补充 channel_status 缺失项（已暂停→已冻结, 新增已驳回）==========
-- 原值: 1-待审核 2-已签约 3-已暂停 4-已终止
-- 修正: 增加 5-已冻结 6-已驳回（前端 ChannelDetail 使用）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'channel_status', '5', '已冻结', 5),
(0, 'channel_status', '6', '已驳回', 6);

-- ========== 6. 补充 claim_status 缺失项（前端扩展的理赔状态）==========
-- 原值: 1-已报案 2-审核中 3-需调查 4-已赔付 5-已拒赔 6-已结案
-- 补充前端使用的额外状态
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'claim_status', '2', '已分配', 2),
(0, 'claim_status', '7', '审核通过', 7),
(0, 'claim_status', '8', '审核拒绝', 8),
(0, 'claim_status', '9', '待调查', 9),
(0, 'claim_status', '10', '调查完成', 10),
(0, 'claim_status', '11', '待总经理审批', 11)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 修正 claim_status code=2（原"审核中"改为"已分配"以匹配前端流程）
UPDATE ins_dict SET dict_name = '审核中' WHERE tenant_id = 0 AND dict_type = 'claim_status' AND dict_code = '2' AND dict_name = '已分配';

-- ========== 7. 补充 underwriting_result 缺失项（转人工）==========
-- 原值已有 6-转人工，确认存在
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'underwriting_result', '6', '转人工', 6)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- ========== 8. 修正 channel_type 标签对齐 ==========
-- 原值: 1-个人代理 2-直销 3-专业代理 4-银保 5-经纪 6-互联网
-- 前端使用: 1-代理人 2-经纪人 3-银保 4-互联网 5-其他
-- 策略：保留字典原始值（数据模型的 source of truth），前端通过 dictStore 读取时自然对齐
-- 此处仅追加"其他"选项
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'channel_type', '5', '其他', 5)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);
