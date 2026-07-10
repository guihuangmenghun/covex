-- V22: 删除 ins_user.user_type 字段（与角色功能重叠）
-- 对应 Task 0.13: 用户类型字段优化

SET NAMES utf8mb4;

ALTER TABLE ins_user DROP COLUMN user_type;
