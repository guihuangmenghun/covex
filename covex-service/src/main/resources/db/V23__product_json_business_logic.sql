-- V23: 产品 JSON 业务逻辑增强 — 为 coverage_detail 补充 use_shared_amount / count_toward_amount 默认值
-- 对应 Task 0.6: 产品JSON定义业务逻辑实现

SET NAMES utf8mb4;

-- 1. 所有 coverage_detail 非空的保障定义，补充 count_toward_amount=true（默认计入总保额）
UPDATE ins_product_coverage
SET coverage_detail = JSON_SET(coverage_detail, '$.count_toward_amount', true)
WHERE coverage_detail IS NOT NULL
  AND JSON_EXTRACT(coverage_detail, '$.count_toward_amount') IS NULL
  AND is_deleted = 0;

-- 2. 所有 coverage_detail 非空的保障定义，补充 use_shared_amount=false（默认不共享保额）
UPDATE ins_product_coverage
SET coverage_detail = JSON_SET(coverage_detail, '$.use_shared_amount', false)
WHERE coverage_detail IS NOT NULL
  AND JSON_EXTRACT(coverage_detail, '$.use_shared_amount') IS NULL
  AND is_deleted = 0;

-- 3. 所有 coverage_detail 为 NULL 的保障定义，初始化为基础结构
UPDATE ins_product_coverage
SET coverage_detail = JSON_OBJECT('count_toward_amount', true, 'use_shared_amount', false)
WHERE coverage_detail IS NULL
  AND is_deleted = 0;

-- 4. 同步到已有的保单保障快照
UPDATE ins_policy_coverage
SET coverage_detail = JSON_SET(coverage_detail, '$.count_toward_amount', true)
WHERE coverage_detail IS NOT NULL
  AND JSON_EXTRACT(coverage_detail, '$.count_toward_amount') IS NULL
  AND is_deleted = 0;

UPDATE ins_policy_coverage
SET coverage_detail = JSON_SET(coverage_detail, '$.use_shared_amount', false)
WHERE coverage_detail IS NOT NULL
  AND JSON_EXTRACT(coverage_detail, '$.use_shared_amount') IS NULL
  AND is_deleted = 0;
