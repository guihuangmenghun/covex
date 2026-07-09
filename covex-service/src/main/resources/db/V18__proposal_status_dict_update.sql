-- V18: 更新投保单状态字典 — status=5 明确为"已支付(待出单)"中间态
-- 需求来源：Covex运营域需求规格.md Story 3.3 AC-2/AC-3

SET NAMES utf8mb4;

UPDATE ins_dict
SET dict_name = '已支付(待出单)'
WHERE dict_type = 'proposal_status' AND dict_code = '5';
