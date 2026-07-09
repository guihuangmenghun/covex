-- ============================================================
-- V4: 角色和权限种子数据（从数据库导出，2026-07-10）
-- ============================================================
SET NAMES utf8mb4;

-- 清理现有数据
DELETE FROM ins_role_permission;
DELETE FROM ins_permission;
DELETE FROM ins_role;

-- ============================================================
-- ins_role 数据（13 个角色）
-- ============================================================
INSERT INTO `ins_role` (`id`, `tenant_id`, `role_code`, `role_name`, `description`, `is_system`, `is_deleted`, `deleted_at`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES 
(1,0,'admin','管理员','超级管理员，拥有全部权限',1,0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-10 03:38:44'),
(2,0,'product_mgr','产品经理','负责产品创建与配置',1,0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-10 03:38:44'),
(3,0,'underwriter','核保员','负责投保单核保审批',1,0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-10 03:38:44'),
(4,0,'claim_handler','理赔员','负责理赔案件处理与审核',1,0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-10 03:38:44'),
(5,0,'channel_mgr','渠道经理','负责渠道商管理与佣金',1,0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-10 03:38:44'),
(9,0,'sub_admin','副管理员','协助管理员，可查看系统配置但不可创建用户',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(10,0,'actuary','精算师','负责费率表设计与规则配置',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(11,0,'agent','代理人','负责投保单录入和客户管理',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(12,0,'service_rep','客服录入员','负责客户信息录入，仅看自己的数据',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(13,0,'conservation','保全专员','负责保单保全和续期管理',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(14,0,'investigator','调查员','负责理赔案件调查取证',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(15,0,'finance','财务人员','负责佣金结算和财务报表',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(16,0,'compliance','合规人员','负责产品合规审核和监管报告',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44');

-- ============================================================
-- ins_permission 数据（41 个权限）
-- ============================================================
INSERT INTO `ins_permission` (`id`, `tenant_id`, `permission_code`, `permission_name`, `module`, `action`, `is_deleted`, `deleted_at`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES 
(1,0,'user:create','创建用户','user','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(2,0,'user:read','查看用户','user','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(3,0,'user:update','编辑用户','user','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(4,0,'user:delete','删除用户','user','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(5,0,'role:create','创建角色','role','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(6,0,'role:read','查看角色','role','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(7,0,'role:update','编辑角色','role','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(8,0,'role:delete','删除角色','role','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(9,0,'permission:read','查看权限','permission','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(10,0,'dict:create','创建字典','dict','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(11,0,'dict:read','查看字典','dict','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(12,0,'dict:update','编辑字典','dict','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(13,0,'dict:delete','删除字典','dict','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(14,0,'product:create','创建产品','product','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(15,0,'product:read','查看产品','product','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(16,0,'product:update','编辑产品','product','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(17,0,'product:delete','删除产品','product','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(18,0,'proposal:create','创建投保单','proposal','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(19,0,'proposal:read','查看投保单','proposal','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(20,0,'proposal:update','编辑投保单','proposal','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(21,0,'proposal:delete','删除投保单','proposal','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(22,0,'policy:read','查看保单','policy','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(23,0,'claim:create','创建理赔','claim','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(24,0,'claim:read','查看理赔','claim','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(25,0,'claim:update','编辑理赔','claim','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(26,0,'claim:delete','删除理赔','claim','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(27,0,'channel:create','创建渠道','channel','create',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(28,0,'channel:read','查看渠道','channel','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(29,0,'channel:update','编辑渠道','channel','update',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(30,0,'channel:delete','删除渠道','channel','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(31,0,'user:edit','编辑用户','user','edit',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(32,0,'user:assign_role','分配角色','user','assign_role',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(33,0,'role:assign_perm','分配权限','role','assign_perm',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(34,0,'product:publish','发布产品','product','publish',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(35,0,'product:approve','审批产品','product','approve',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(36,0,'channel:approve','审批渠道','channel','approve',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(37,0,'claim:review','审核理赔','claim','review',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(38,0,'claim:approve','审批理赔','claim','approve',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(39,0,'claim:pay','支付理赔','claim','pay',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(40,0,'commission:settle','结算佣金','commission','settle',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37'),
(41,0,'commission:pay','支付佣金','commission','pay',0,NULL,NULL,NULL,'2026-07-09 02:28:55','2026-07-10 03:43:37');

-- ============================================================
-- ins_role_permission 数据（148 条，根据路由权限矩阵生成）
-- ============================================================
-- admin (role_id=1): 41 个权限（全部）
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,1,1,'system'),(0,1,2,'system'),(0,1,3,'system'),(0,1,4,'system'),
(0,1,5,'system'),(0,1,6,'system'),(0,1,7,'system'),(0,1,8,'system'),
(0,1,9,'system'),(0,1,10,'system'),(0,1,11,'system'),(0,1,12,'system'),
(0,1,13,'system'),(0,1,14,'system'),(0,1,15,'system'),(0,1,16,'system'),
(0,1,17,'system'),(0,1,18,'system'),(0,1,19,'system'),(0,1,20,'system'),
(0,1,21,'system'),(0,1,22,'system'),(0,1,23,'system'),(0,1,24,'system'),
(0,1,25,'system'),(0,1,26,'system'),(0,1,27,'system'),(0,1,28,'system'),
(0,1,29,'system'),(0,1,30,'system'),(0,1,31,'system'),(0,1,32,'system'),
(0,1,33,'system'),(0,1,34,'system'),(0,1,35,'system'),(0,1,36,'system'),
(0,1,37,'system'),(0,1,38,'system'),(0,1,39,'system'),(0,1,40,'system'),
(0,1,41,'system');

-- sub_admin (role_id=9): 20 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,9,2,'system'),(0,9,5,'system'),(0,9,6,'system'),(0,9,7,'system'),
(0,9,15,'system'),(0,9,16,'system'),(0,9,18,'system'),(0,9,19,'system'),
(0,9,20,'system'),(0,9,22,'system'),(0,9,23,'system'),(0,9,24,'system'),
(0,9,25,'system'),(0,9,27,'system'),(0,9,28,'system'),(0,9,29,'system'),
(0,9,33,'system'),(0,9,36,'system'),(0,9,40,'system'),(0,9,41,'system');

-- product_mgr (role_id=2): 9 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,2,14,'system'),(0,2,15,'system'),(0,2,16,'system'),(0,2,17,'system'),
(0,2,34,'system'),(0,2,35,'system'),(0,2,19,'system'),(0,2,22,'system'),
(0,2,24,'system');

-- underwriter (role_id=3): 5 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,3,19,'system'),(0,3,20,'system'),(0,3,22,'system'),(0,3,24,'system'),
(0,3,15,'system');

-- claim_handler (role_id=4): 9 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,4,23,'system'),(0,4,24,'system'),(0,4,25,'system'),(0,4,37,'system'),
(0,4,38,'system'),(0,4,39,'system'),(0,4,22,'system'),(0,4,15,'system'),
(0,4,19,'system');

-- channel_mgr (role_id=5): 9 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,5,27,'system'),(0,5,28,'system'),(0,5,29,'system'),(0,5,30,'system'),
(0,5,36,'system'),(0,5,22,'system'),(0,5,24,'system'),(0,5,15,'system'),
(0,5,19,'system');

-- actuary (role_id=10): 4 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,10,15,'system'),(0,10,16,'system'),(0,10,22,'system'),(0,10,19,'system');

-- agent (role_id=11): 5 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,11,18,'system'),(0,11,19,'system'),(0,11,20,'system'),(0,11,22,'system'),
(0,11,15,'system');

-- service_rep (role_id=12): 7 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,12,18,'system'),(0,12,19,'system'),(0,12,20,'system'),(0,12,22,'system'),
(0,12,23,'system'),(0,12,24,'system'),(0,12,15,'system');

-- conservation (role_id=13): 4 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,13,22,'system'),(0,13,15,'system'),(0,13,19,'system'),(0,13,24,'system');

-- investigator (role_id=14): 5 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,14,22,'system'),(0,14,24,'system'),(0,14,25,'system'),(0,14,15,'system'),
(0,14,19,'system');

-- finance (role_id=15): 7 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,15,22,'system'),(0,15,24,'system'),(0,15,40,'system'),(0,15,41,'system'),
(0,15,15,'system'),(0,15,19,'system'),(0,15,28,'system');

-- compliance (role_id=16): 6 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,16,22,'system'),(0,16,24,'system'),(0,16,35,'system'),(0,16,15,'system'),
(0,16,19,'system'),(0,16,28,'system');

SELECT 'V4 seed data loaded successfully' AS result;
