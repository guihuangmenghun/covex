-- ============================================================
-- V4: 角色和权限种子数据
-- 权限模型：R→read, W→edit（创建+修改合一）, ✓→具体动作
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
(6,0,'sub_admin','副管理员','协助管理员，可查看系统配置但不可创建用户',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(7,0,'actuary','精算师','负责费率表设计与规则配置',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(8,0,'agent','代理人','负责投保单录入和客户管理',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(9,0,'service_rep','客服录入员','负责客户信息录入，仅看自己的数据',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(10,0,'conservation','保全专员','负责保单保全和续期管理',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(11,0,'investigator','调查员','负责理赔案件调查取证',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(12,0,'finance','财务人员','负责佣金结算和财务报表',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44'),
(13,0,'compliance','合规人员','负责产品合规审核和监管报告',1,0,NULL,'system',NULL,'2026-07-09 02:19:53','2026-07-10 03:38:44');

-- ============================================================
-- ins_permission 数据（33 个权限，ID 顺序排列）
-- ============================================================
INSERT INTO `ins_permission` (`id`, `tenant_id`, `permission_code`, `permission_name`, `module`, `action`, `is_deleted`, `deleted_at`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES 
-- user（4）
(1,0,'user:read','查看用户','user','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(2,0,'user:edit','编辑用户','user','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(3,0,'user:delete','删除用户','user','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(4,0,'user:assign_role','分配角色','user','assign_role',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- role（4）
(5,0,'role:read','查看角色','role','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(6,0,'role:edit','编辑角色','role','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(7,0,'role:delete','删除角色','role','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(8,0,'role:assign_perm','分配权限','role','assign_perm',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- permission（1）
(9,0,'permission:read','查看权限','permission','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- dict（3）
(10,0,'dict:read','查看字典','dict','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(11,0,'dict:edit','编辑字典','dict','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(12,0,'dict:delete','删除字典','dict','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- product（4）
(13,0,'product:read','查看产品','product','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(14,0,'product:edit','编辑产品','product','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(15,0,'product:delete','删除产品','product','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(16,0,'product:publish','发布产品','product','publish',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- proposal（3）
(17,0,'proposal:read','查看投保单','proposal','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(18,0,'proposal:edit','编辑投保单','proposal','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(19,0,'proposal:delete','删除投保单','proposal','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- policy（2）
(20,0,'policy:read','查看保单','policy','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(21,0,'policy:edit','编辑保单','policy','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- claim（4）
(22,0,'claim:read','查看理赔','claim','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(23,0,'claim:edit','编辑理赔','claim','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(24,0,'claim:delete','删除理赔','claim','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(25,0,'claim:review','审核理赔','claim','review',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- channel（4）
(26,0,'channel:read','查看渠道','channel','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(27,0,'channel:edit','编辑渠道','channel','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(28,0,'channel:delete','删除渠道','channel','delete',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(29,0,'channel:approve','审批渠道','channel','approve',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- commission（3）
(30,0,'commission:read','查看佣金','commission','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(31,0,'commission:settle','结算佣金','commission','settle',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(32,0,'commission:pay','支付佣金','commission','pay',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
-- customer（3）
(33,0,'customer:read','查看客户','customer','read',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41'),
(34,0,'customer:edit','编辑客户','customer','edit',0,NULL,'system',NULL,'2026-07-03 03:10:41','2026-07-03 03:10:41');

-- ============================================================
-- ins_role_permission 数据（根据路由权限矩阵 v3.0）
-- ============================================================

-- admin (role_id=1): 全部 34 个权限
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,1,1,'system'),(0,1,2,'system'),(0,1,3,'system'),(0,1,4,'system'),
(0,1,5,'system'),(0,1,6,'system'),(0,1,7,'system'),(0,1,8,'system'),
(0,1,9,'system'),(0,1,10,'system'),(0,1,11,'system'),(0,1,12,'system'),
(0,1,13,'system'),(0,1,14,'system'),(0,1,15,'system'),(0,1,16,'system'),
(0,1,17,'system'),(0,1,18,'system'),(0,1,19,'system'),
(0,1,20,'system'),(0,1,21,'system'),
(0,1,22,'system'),(0,1,23,'system'),(0,1,24,'system'),(0,1,25,'system'),
(0,1,26,'system'),(0,1,27,'system'),(0,1,28,'system'),(0,1,29,'system'),
(0,1,30,'system'),(0,1,31,'system'),(0,1,32,'system'),
(0,1,33,'system'),(0,1,34,'system');

-- sub_admin (role_id=6): 全部业务 + 渠道 + 佣金 + 角色管理
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,6,1,'system'),  -- user:read
(0,6,5,'system'),(0,6,6,'system'),(0,6,8,'system'),  -- role:read/edit/assign_perm
(0,6,13,'system'),(0,6,14,'system'),  -- product:read/edit
(0,6,17,'system'),(0,6,18,'system'),  -- proposal:read/edit
(0,6,20,'system'),(0,6,21,'system'),  -- policy:read/edit
(0,6,22,'system'),(0,6,23,'system'),  -- claim:read/edit
(0,6,26,'system'),(0,6,27,'system'),(0,6,29,'system'),  -- channel:read/edit/approve
(0,6,30,'system'),(0,6,31,'system'),(0,6,32,'system'),  -- commission:read/settle/pay
(0,6,33,'system'),(0,6,34,'system');  -- customer:read/edit

-- product_mgr (role_id=2): 产品创建/编辑/发布
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,2,13,'system'),(0,2,14,'system'),(0,2,15,'system'),(0,2,16,'system'),  -- product RW+delete+publish
(0,2,17,'system'),  -- proposal:read
(0,2,20,'system'),  -- policy:read
(0,2,22,'system'),  -- claim:read
(0,2,26,'system'),  -- channel:read
(0,2,33,'system');  -- customer:read

-- underwriter (role_id=3): 核保工作台 + 核保规则
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,3,13,'system'),  -- product:read
(0,3,17,'system'),(0,3,18,'system'),  -- proposal:read/edit
(0,3,20,'system'),  -- policy:read
(0,3,22,'system'),  -- claim:read
(0,3,33,'system'),(0,3,34,'system');  -- customer:read/edit

-- claim_handler (role_id=4): 理赔工作台 + 理赔规则
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,4,13,'system'),  -- product:read
(0,4,17,'system'),  -- proposal:read
(0,4,20,'system'),  -- policy:read
(0,4,22,'system'),(0,4,23,'system'),(0,4,24,'system'),(0,4,25,'system'),  -- claim:read/edit/delete/review
(0,4,33,'system'),(0,4,34,'system');  -- customer:read/edit

-- channel_mgr (role_id=5): 渠道商管理
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,5,13,'system'),  -- product:read
(0,5,17,'system'),  -- proposal:read
(0,5,20,'system'),  -- policy:read
(0,5,22,'system'),  -- claim:read
(0,5,26,'system'),(0,5,27,'system'),(0,5,28,'system'),(0,5,29,'system'),  -- channel:read/edit/delete/approve
(0,5,30,'system'),  -- commission:read
(0,5,33,'system');  -- customer:read

-- actuary (role_id=7): 费率表管理
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,7,13,'system'),(0,7,14,'system'),  -- product:read/edit
(0,7,17,'system'),  -- proposal:read
(0,7,20,'system'),  -- policy:read
(0,7,33,'system');  -- customer:read

-- agent (role_id=8): 客户录入 + 投保单录入
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,8,13,'system'),  -- product:read
(0,8,17,'system'),(0,8,18,'system'),  -- proposal:read/edit
(0,8,20,'system'),  -- policy:read
(0,8,33,'system'),(0,8,34,'system');  -- customer:read/edit

-- service_rep (role_id=9): 客户咨询 + 保全代办 + 理赔报案
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,9,13,'system'),  -- product:read
(0,9,17,'system'),(0,9,18,'system'),  -- proposal:read/edit
(0,9,20,'system'),(0,9,21,'system'),  -- policy:read/edit
(0,9,22,'system'),(0,9,23,'system'),  -- claim:read/edit
(0,9,33,'system'),(0,9,34,'system');  -- customer:read/edit

-- conservation (role_id=10): 保单服务 + 保全规则
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,10,13,'system'),  -- product:read
(0,10,17,'system'),  -- proposal:read
(0,10,20,'system'),(0,10,21,'system'),  -- policy:read/edit
(0,10,22,'system'),  -- claim:read
(0,10,33,'system'),(0,10,34,'system');  -- customer:read/edit

-- investigator (role_id=11): 理赔调查
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,11,13,'system'),  -- product:read
(0,11,17,'system'),  -- proposal:read
(0,11,20,'system'),  -- policy:read
(0,11,22,'system'),(0,11,23,'system');  -- claim:read/edit

-- finance (role_id=12): 收付费确认 + 佣金结算
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,12,13,'system'),  -- product:read
(0,12,17,'system'),  -- proposal:read
(0,12,20,'system'),  -- policy:read
(0,12,22,'system'),  -- claim:read
(0,12,26,'system'),  -- channel:read
(0,12,30,'system'),(0,12,31,'system'),(0,12,32,'system'),  -- commission:read/settle/pay
(0,12,33,'system');  -- customer:read

-- compliance (role_id=13): 产品合规审核 + 监管报送
INSERT INTO `ins_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_by`) VALUES 
(0,13,13,'system'),(0,13,16,'system'),  -- product:read + publish(approve)
(0,13,17,'system'),  -- proposal:read
(0,13,20,'system'),  -- policy:read
(0,13,22,'system'),  -- claim:read
(0,13,26,'system'),  -- channel:read
(0,13,33,'system');  -- customer:read

SELECT 'V4 seed data loaded successfully' AS result;
