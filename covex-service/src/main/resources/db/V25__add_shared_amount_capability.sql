-- V25: 为产品 capabilities 补充 shared_amount 字段
-- 对应计划：Covex补充计划20260709.md → Task 0.6 #9 核实修复
-- 根因：产品模板种子数据（V15）的 capabilities 中缺少 shared_amount 字段，
--       导致 ClaimSharedAmountComponent 无法判断是否启用共享保额
-- 执行方式：mysql -u root -p covex --default-character-set=utf8mb4 < V25__add_shared_amount_capability.sql

SET NAMES utf8mb4;

-- 1. 为所有已有产品的 capabilities 补充 shared_amount = false（默认不共享）
UPDATE ins_product
SET capabilities = JSON_SET(capabilities, '$.shared_amount', false)
WHERE capabilities IS NOT NULL
  AND JSON_EXTRACT(capabilities, '$.shared_amount') IS NULL
  AND is_deleted = 0;

-- 2. 为 capabilities 为 NULL 的产品初始化基础结构
UPDATE ins_product
SET capabilities = JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'shared_amount', false)
WHERE capabilities IS NULL
  AND is_deleted = 0;

-- 3. 为重疾险+寿险组合的产品启用共享保额（product_type = 1 寿险 且 含重疾保障）
UPDATE ins_product p
SET p.capabilities = JSON_SET(p.capabilities, '$.shared_amount', true)
WHERE p.product_type = 1
  AND p.is_deleted = 0
  AND EXISTS (
    SELECT 1 FROM ins_product_coverage c
    WHERE c.product_id = p.id
      AND c.coverage_code LIKE '%CI%'
      AND c.is_deleted = 0
  );
