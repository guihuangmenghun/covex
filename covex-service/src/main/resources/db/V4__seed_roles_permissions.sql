-- =============================================
-- S3: Seed data - System roles & base permissions
-- =============================================

-- ========== System Roles ==========
INSERT INTO ins_role (tenant_id, role_code, role_name, description, is_system, created_by)
VALUES
(0, 'ADMIN',             '管理员',     '系统管理员，拥有全部权限',      1, 'system'),
(0, 'PRODUCT_MANAGER',   '产品经理',   '负责产品配置与规则管理',      1, 'system'),
(0, 'UNDERWRITER',       '核保员',     '负责投保单核保审批',          1, 'system'),
(0, 'CLAIM_HANDLER',     '理赔员',     '负责理赔案件处理',            1, 'system'),
(0, 'CHANNEL_ADMIN',     '渠道管理员', '负责渠道配置与管理',          1, 'system');

-- ========== Base Permissions ==========
-- User management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'user:create',     '创建用户',   'user',    'create', 'system'),
(0, 'user:read',       '查看用户',   'user',    'read',   'system'),
(0, 'user:update',     '编辑用户',   'user',    'update', 'system'),
(0, 'user:delete',     '删除用户',   'user',    'delete', 'system');

-- Role management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'role:create',     '创建角色',   'role',    'create', 'system'),
(0, 'role:read',       '查看角色',   'role',    'read',   'system'),
(0, 'role:update',     '编辑角色',   'role',    'update', 'system'),
(0, 'role:delete',     '删除角色',   'role',    'delete', 'system');

-- Permission management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'permission:read', '查看权限',   'permission', 'read', 'system');

-- Dictionary management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'dict:create',     '创建字典',   'dict',    'create', 'system'),
(0, 'dict:read',       '查看字典',   'dict',    'read',   'system'),
(0, 'dict:update',     '编辑字典',   'dict',    'update', 'system'),
(0, 'dict:delete',     '删除字典',   'dict',    'delete', 'system');

-- Product management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'product:create',  '创建产品',   'product', 'create', 'system'),
(0, 'product:read',    '查看产品',   'product', 'read',   'system'),
(0, 'product:update',  '编辑产品',   'product', 'update', 'system'),
(0, 'product:delete',  '删除产品',   'product', 'delete', 'system');

-- Proposal management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'proposal:create', '创建投保单', 'proposal', 'create', 'system'),
(0, 'proposal:read',   '查看投保单', 'proposal', 'read',   'system'),
(0, 'proposal:update', '编辑投保单', 'proposal', 'update', 'system'),
(0, 'proposal:delete', '删除投保单', 'proposal', 'delete', 'system');

-- Policy management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'policy:read',     '查看保单',   'policy',  'read',   'system');

-- Claim management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'claim:create',    '创建理赔',   'claim',   'create', 'system'),
(0, 'claim:read',      '查看理赔',   'claim',   'read',   'system'),
(0, 'claim:update',    '编辑理赔',   'claim',   'update', 'system'),
(0, 'claim:delete',    '删除理赔',   'claim',   'delete', 'system');

-- Channel management
INSERT INTO ins_permission (tenant_id, permission_code, permission_name, module, action, created_by)
VALUES
(0, 'channel:create',  '创建渠道',   'channel', 'create', 'system'),
(0, 'channel:read',    '查看渠道',   'channel', 'read',   'system'),
(0, 'channel:update',  '编辑渠道',   'channel', 'update', 'system'),
(0, 'channel:delete',  '删除渠道',   'channel', 'delete', 'system');

-- ========== Admin role gets ALL permissions ==========
INSERT INTO ins_role_permission (tenant_id, role_id, permission_id, created_by)
SELECT 0,
       (SELECT id FROM ins_role WHERE role_code = 'ADMIN'),
       id,
       'system'
FROM ins_permission;
