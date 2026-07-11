-- ============================================================
-- V20: 角色权限预设配置（根据 Covex路由权限矩阵.md v3.0 重新生成）
-- 权限映射规则：
--   R (可见可读) → read
--   W (可创建/编辑) → edit
--   ✓ (可执行操作) → approve/pay/settle 等
--   — (不可见) → 无权限
-- ============================================================
SET NAMES utf8mb4;

-- 先清理现有角色权限（保留 admin）
DELETE FROM ins_role_permission WHERE role_id IN (
    SELECT id FROM ins_role WHERE role_code != 'admin'
);

-- ============================================================
-- sub_admin (次级管理员): 全部业务 + 渠道 + 佣金 + 角色管理
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'sub_admin'
AND p.permission_code IN (
    -- 产品配置（RW = read + edit）
    'product:read', 'product:edit',
    -- 费率表（RW）
    'rate_table:read', 'rate_table:edit',
    -- 承保管理（RW）
    'proposal:read', 'proposal:edit',
    -- 核保（R）
    'underwriting:read',
    -- 支付（RW✓ = read + edit + confirm）
    'payment:read', 'payment:edit', 'payment:confirm',
    -- 保单管理（RW）
    'policy:read', 'policy:edit',
    -- 理赔管理（RW）
    'claim:read', 'claim:edit',
    -- 佣金管理（RW✓）
    'commission:read', 'commission:settle', 'commission:pay',
    -- 渠道管理（RW）
    'channel:read', 'channel:edit', 'channel:approve',
    -- 客户管理（RW）
    'customer:read', 'customer:edit',
    -- 规则配置（RW）
    'rule:read', 'rule:edit',
    -- 财务管理（RW✓）
    'finance:read', 'finance:settle',
    -- 合规管理（R）
    'compliance:read',
    -- 系统管理（RW + 角色 RW）
    'user:read', 'user:edit', 'user:assign_role',
    'role:read', 'role:edit', 'role:assign_perm'
);

-- ============================================================
-- product_mgr (产品经理): 产品创建/编辑/发布 + 产品规则配置
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'product_mgr'
AND p.permission_code IN (
    -- 产品配置（RW✓ = 主人）
    'product:read', 'product:edit', 'product:delete', 'product:publish',
    -- 费率表（R）
    'rate_table:read',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R）
    'claim:read',
    -- 渠道管理（R）
    'channel:read',
    -- 客户管理（R）
    'customer:read',
    -- 规则配置（费率 RW）
    'rule:read', 'rule:edit'
);

-- ============================================================
-- actuary (精算师): 费率表管理 + 预定利率审核 + 产品定价
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'actuary'
AND p.permission_code IN (
    -- 产品配置（RW）
    'product:read', 'product:edit',
    -- 费率表（RW✓ = 主人）
    'rate_table:read', 'rate_table:edit', 'rate_table:delete',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 客户管理（R）
    'customer:read',
    -- 规则配置（费率 RW）
    'rule:read', 'rule:edit'
);

-- ============================================================
-- channel_mgr (渠道经理): 渠道商管理 + 产品授权 + 佣金设置
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'channel_mgr'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R）
    'claim:read',
    -- 佣金管理（R）
    'commission:read',
    -- 渠道管理（RW✓ = 主人）
    'channel:read', 'channel:edit', 'channel:delete', 'channel:approve',
    -- 客户管理（R）
    'customer:read',
    -- 规则配置（佣金 R）
    'rule:read'
);

-- ============================================================
-- agent (录入员): 客户录入 + 投保单录入 + 保费试算
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'agent'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（RW✓ = 主人）
    'proposal:read', 'proposal:edit',
    -- 保单管理（R）
    'policy:read',
    -- 客户管理（RW✓ = 主人）
    'customer:read', 'customer:edit',
    -- 规则配置（R）
    'rule:read'
);

-- ============================================================
-- service_rep (客服专员): 客户咨询 + 保全代办 + 理赔报案
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'service_rep'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（RW✓ = 主人）
    'proposal:read', 'proposal:edit',
    -- 保单管理（RW✓ = 主人）
    'policy:read', 'policy:edit',
    -- 理赔管理（R + 报案 RW）
    'claim:read', 'claim:edit',
    -- 客户管理（RW✓ = 主人）
    'customer:read', 'customer:edit',
    -- 规则配置（R）
    'rule:read'
);

-- ============================================================
-- underwriter (核保老师): 核保工作台 + 核保规则配置
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'underwriter'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（RW✓ = 主人）
    'proposal:read', 'proposal:edit',
    'underwriting:read', 'underwriting:approve',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R）
    'claim:read',
    -- 客户管理（RW）
    'customer:read', 'customer:edit',
    -- 规则配置（核保 RW）
    'rule:read', 'rule:edit'
);

-- ============================================================
-- conservation (保全老师): 保单服务 + 保全规则配置
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'conservation'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（RW✓ = 主人）
    'policy:read', 'policy:edit',
    -- 理赔管理（R）
    'claim:read',
    -- 客户管理（RW）
    'customer:read', 'customer:edit',
    -- 规则配置（保全 RW）
    'rule:read', 'rule:edit'
);

-- ============================================================
-- claim_handler (理赔老师): 理赔工作台 + 理赔规则配置
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'claim_handler'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（RW✓ = 主人）
    'claim:read', 'claim:edit', 'claim:approve', 'claim:review', 'claim:pay',
    -- 客户管理（RW）
    'customer:read', 'customer:edit',
    -- 规则配置（理赔 RW）
    'rule:read', 'rule:edit'
);

-- ============================================================
-- investigator (调查员): 理赔调查 + 反欺诈 + 调查报告
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'investigator'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R + 调查 RW）
    'claim:read', 'claim:edit',
    -- 客户管理（R）
    'customer:read',
    -- 规则配置（理赔 R）
    'rule:read'
);

-- ============================================================
-- finance (财务结算): 收付费确认 + 佣金结算 + 财务报表
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'finance'
AND p.permission_code IN (
    -- 产品配置（R）
    'product:read',
    -- 承保管理（支付 RW✓）
    'proposal:read', 'payment:read', 'payment:confirm',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R）
    'claim:read',
    -- 佣金管理（RW✓ = 主人）
    'commission:read', 'commission:settle', 'commission:pay',
    -- 渠道管理（R）
    'channel:read',
    -- 客户管理（R）
    'customer:read',
    -- 财务报表（RW）
    'finance:read', 'finance:report'
);

-- ============================================================
-- compliance (合规法务): 产品合规审核 + 条款审查 + 监管报送
-- ============================================================
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system'
FROM ins_role r, ins_permission p
WHERE r.role_code = 'compliance'
AND p.permission_code IN (
    -- 产品配置（R + 审核✓）
    'product:read', 'product:approve',
    -- 费率表（R + 审核✓）
    'rate_table:read', 'rate_table:approve',
    -- 承保管理（R）
    'proposal:read',
    -- 保单管理（R）
    'policy:read',
    -- 理赔管理（R）
    'claim:read',
    -- 渠道管理（R）
    'channel:read',
    -- 客户管理（R）
    'customer:read',
    -- 合规审核（RW✓ = 主人）
    'compliance:read', 'compliance:review', 'compliance:regulatory'
);

-- 完成提示
SELECT 'V20 role permissions configured successfully' AS result;
