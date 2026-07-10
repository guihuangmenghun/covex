-- ============================================================
-- V23: 权限编码统一为 edit，清除 :create 和 :update
-- 权限模型：R→read, W→edit（创建+修改合一）, ✓→具体动作
-- 与 V4 种子数据对齐（ID 顺序 1-34）
-- ============================================================
SET NAMES utf8mb4;

-- 清理权限相关数据（保留角色表）
DELETE FROM ins_role_permission;
DELETE FROM ins_permission;

-- ============================================================
-- ins_permission（34 个权限，ID 顺序排列）
-- ============================================================
INSERT INTO `ins_permission` (`id`, `tenant_id`, `permission_code`, `permission_name`, `module`, `action`, `is_deleted`, `deleted_at`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES 
-- user（4）
(1,0,'user:read','查看用户','user','read',0,NULL,'system',NULL,NOW(),NOW()),
(2,0,'user:edit','编辑用户','user','edit',0,NULL,'system',NULL,NOW(),NOW()),
(3,0,'user:delete','删除用户','user','delete',0,NULL,'system',NULL,NOW(),NOW()),
(4,0,'user:assign_role','分配角色','user','assign_role',0,NULL,'system',NULL,NOW(),NOW()),
-- role（4）
(5,0,'role:read','查看角色','role','read',0,NULL,'system',NULL,NOW(),NOW()),
(6,0,'role:edit','编辑角色','role','edit',0,NULL,'system',NULL,NOW(),NOW()),
(7,0,'role:delete','删除角色','role','delete',0,NULL,'system',NULL,NOW(),NOW()),
(8,0,'role:assign_perm','分配权限','role','assign_perm',0,NULL,'system',NULL,NOW(),NOW()),
-- permission（1）
(9,0,'permission:read','查看权限','permission','read',0,NULL,'system',NULL,NOW(),NOW()),
-- dict（3）
(10,0,'dict:read','查看字典','dict','read',0,NULL,'system',NULL,NOW(),NOW()),
(11,0,'dict:edit','编辑字典','dict','edit',0,NULL,'system',NULL,NOW(),NOW()),
(12,0,'dict:delete','删除字典','dict','delete',0,NULL,'system',NULL,NOW(),NOW()),
-- product（4）
(13,0,'product:read','查看产品','product','read',0,NULL,'system',NULL,NOW(),NOW()),
(14,0,'product:edit','编辑产品','product','edit',0,NULL,'system',NULL,NOW(),NOW()),
(15,0,'product:delete','删除产品','product','delete',0,NULL,'system',NULL,NOW(),NOW()),
(16,0,'product:publish','发布产品','product','publish',0,NULL,'system',NULL,NOW(),NOW()),
-- proposal（3）
(17,0,'proposal:read','查看投保单','proposal','read',0,NULL,'system',NULL,NOW(),NOW()),
(18,0,'proposal:edit','编辑投保单','proposal','edit',0,NULL,'system',NULL,NOW(),NOW()),
(19,0,'proposal:delete','删除投保单','proposal','delete',0,NULL,'system',NULL,NOW(),NOW()),
-- policy（2）
(20,0,'policy:read','查看保单','policy','read',0,NULL,'system',NULL,NOW(),NOW()),
(21,0,'policy:edit','编辑保单','policy','edit',0,NULL,'system',NULL,NOW(),NOW()),
-- claim（4）
(22,0,'claim:read','查看理赔','claim','read',0,NULL,'system',NULL,NOW(),NOW()),
(23,0,'claim:edit','编辑理赔','claim','edit',0,NULL,'system',NULL,NOW(),NOW()),
(24,0,'claim:delete','删除理赔','claim','delete',0,NULL,'system',NULL,NOW(),NOW()),
(25,0,'claim:review','审核理赔','claim','review',0,NULL,'system',NULL,NOW(),NOW()),
-- channel（4）
(26,0,'channel:read','查看渠道','channel','read',0,NULL,'system',NULL,NOW(),NOW()),
(27,0,'channel:edit','编辑渠道','channel','edit',0,NULL,'system',NULL,NOW(),NOW()),
(28,0,'channel:delete','删除渠道','channel','delete',0,NULL,'system',NULL,NOW(),NOW()),
(29,0,'channel:approve','审批渠道','channel','approve',0,NULL,'system',NULL,NOW(),NOW()),
-- commission（3）
(30,0,'commission:read','查看佣金','commission','read',0,NULL,'system',NULL,NOW(),NOW()),
(31,0,'commission:settle','结算佣金','commission','settle',0,NULL,'system',NULL,NOW(),NOW()),
(32,0,'commission:pay','支付佣金','commission','pay',0,NULL,'system',NULL,NOW(),NOW()),
-- customer（2）
(33,0,'customer:read','查看客户','customer','read',0,NULL,'system',NULL,NOW(),NOW()),
(34,0,'customer:edit','编辑客户','customer','edit',0,NULL,'system',NULL,NOW(),NOW());

-- ============================================================
-- ins_role_permission（根据路由权限矩阵 v3.0，V20 同步）
-- ============================================================

-- admin (id=1): 全部 34 个权限
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p WHERE r.role_code = 'admin';

-- sub_admin (id=6): 全部业务 + 渠道 + 佣金 + 角色管理
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'sub_admin' AND p.permission_code IN (
    'user:read',
    'role:read', 'role:edit', 'role:assign_perm',
    'product:read', 'product:edit',
    'proposal:read', 'proposal:edit',
    'policy:read', 'policy:edit',
    'claim:read', 'claim:edit',
    'channel:read', 'channel:edit', 'channel:approve',
    'commission:read', 'commission:settle', 'commission:pay',
    'customer:read', 'customer:edit'
);

-- product_mgr (id=2): 产品编辑/发布
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'product_mgr' AND p.permission_code IN (
    'product:read', 'product:edit', 'product:delete', 'product:publish',
    'proposal:read', 'policy:read', 'claim:read', 'channel:read', 'customer:read'
);

-- underwriter (id=3): 核保 + 客户编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'underwriter' AND p.permission_code IN (
    'product:read', 'proposal:read', 'proposal:edit',
    'policy:read', 'claim:read', 'customer:read', 'customer:edit'
);

-- claim_handler (id=4): 理赔工作台
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'claim_handler' AND p.permission_code IN (
    'product:read', 'proposal:read', 'policy:read',
    'claim:read', 'claim:edit', 'claim:delete', 'claim:review',
    'customer:read', 'customer:edit'
);

-- channel_mgr (id=5): 渠道管理
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'channel_mgr' AND p.permission_code IN (
    'product:read', 'proposal:read', 'policy:read', 'claim:read',
    'channel:read', 'channel:edit', 'channel:delete', 'channel:approve',
    'commission:read', 'customer:read'
);

-- actuary (id=7): 产品编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'actuary' AND p.permission_code IN (
    'product:read', 'product:edit', 'proposal:read', 'policy:read', 'customer:read'
);

-- agent (id=8): 投保单 + 客户编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'agent' AND p.permission_code IN (
    'product:read', 'proposal:read', 'proposal:edit',
    'policy:read', 'customer:read', 'customer:edit'
);

-- service_rep (id=9): 投保单 + 保单 + 理赔 + 客户编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'service_rep' AND p.permission_code IN (
    'product:read', 'proposal:read', 'proposal:edit',
    'policy:read', 'policy:edit', 'claim:read', 'claim:edit',
    'customer:read', 'customer:edit'
);

-- conservation (id=10): 保单编辑 + 客户编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'conservation' AND p.permission_code IN (
    'product:read', 'proposal:read',
    'policy:read', 'policy:edit', 'claim:read',
    'customer:read', 'customer:edit'
);

-- investigator (id=11): 理赔编辑
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'investigator' AND p.permission_code IN (
    'product:read', 'proposal:read', 'policy:read',
    'claim:read', 'claim:edit', 'customer:read'
);

-- finance (id=12): 佣金结算
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'finance' AND p.permission_code IN (
    'product:read', 'proposal:read', 'policy:read', 'claim:read',
    'channel:read', 'commission:read', 'commission:settle', 'commission:pay',
    'customer:read'
);

-- compliance (id=13): 产品审批 + 合规审核
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0, r.id, p.id, 'system' FROM ins_role r, ins_permission p
WHERE r.role_code = 'compliance' AND p.permission_code IN (
    'product:read', 'product:publish',
    'proposal:read', 'policy:read', 'claim:read',
    'channel:read', 'customer:read'
);

SELECT 'V23 permission edit unified successfully' AS result;
