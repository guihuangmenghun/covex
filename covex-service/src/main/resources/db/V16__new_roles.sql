-- ============================================================
-- V16: 角色体系重构 — 13 角色（v3.0 权限矩阵）
-- 将旧大写编码改为小写，新增 8 个角色
-- ============================================================

-- 1. 重新编码现有 5 个角色
UPDATE ins_role SET role_code = 'admin',         description = '超级管理员，拥有全部权限'                  WHERE role_code = 'ADMIN';
UPDATE ins_role SET role_code = 'product_mgr',   role_name = '产品经理',   description = '负责产品创建与配置'      WHERE role_code = 'PRODUCT_MANAGER';
UPDATE ins_role SET role_code = 'underwriter',    description = '负责投保单核保审批'                      WHERE role_code = 'UNDERWRITER';
UPDATE ins_role SET role_code = 'claim_handler',  description = '负责理赔案件处理与审核'                  WHERE role_code = 'CLAIM_HANDLER';
UPDATE ins_role SET role_code = 'channel_mgr',    role_name = '渠道经理',   description = '负责渠道商管理与佣金'    WHERE role_code = 'CHANNEL_ADMIN';

-- 2. 新增 8 个角色
INSERT INTO ins_role (tenant_id, role_code, role_name, description, is_system, created_by)
VALUES
(0, 'sub_admin',    '副管理员',   '协助管理员，可查看系统配置但不可创建用户', 1, 'system'),
(0, 'actuary',      '精算师',     '负责费率表设计与规则配置',              1, 'system'),
(0, 'agent',        '代理人',     '负责投保单录入和客户管理',              1, 'system'),
(0, 'service_rep',  '客服录入员', '负责客户信息录入，仅看自己的数据',       1, 'system'),
(0, 'conservation', '保全专员',   '负责保单保全和续期管理',                1, 'system'),
(0, 'investigator', '调查员',     '负责理赔案件调查取证',                  1, 'system'),
(0, 'finance',      '财务人员',   '负责佣金结算和财务报表',                1, 'system'),
(0, 'compliance',   '合规人员',   '负责产品合规审核和监管报告',             1, 'system');
