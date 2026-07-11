-- V24: 修复 ins_product.sale_channel JSON 格式脏数据
-- 对应计划：Covex补充计划20260709.md → 第三轮测试 P0-5 修复
-- 根因：部分早期产品的 sale_channel 存为 "1,2"（逗号分隔字符串）
--       而 ProductEntity.saleChannel 声明为 List<String>，
--       JacksonTypeHandler 反序列化时 Jackson 无法将 "1,2" 转为 ArrayList
-- 影响：GET /api/product?size>=N 查询到脏数据行时返回 500
-- 执行方式：mysql -u root -p covex --default-character-set=utf8mb4 < V24__fix_sale_channel_json.sql

SET NAMES utf8mb4;

-- 1. 修复逗号分隔字符串 → JSON 数组
--    "1,2" (hex: 22312C3222) → ["1","2"]
--    注意：脏数据含内嵌双引号，普通字符串比较不可靠，需用 HEX 匹配
UPDATE ins_product
SET sale_channel = '["1","2"]'
WHERE sale_channel = x'22312C3222';

UPDATE ins_product
SET sale_channel = '["1","2","3"]'
WHERE sale_channel = x'22312C322C3322';

-- 2. 通用修复：sale_channel 不以 [ 开头的非空值
UPDATE ins_product
SET sale_channel = CONCAT('["', REPLACE(TRIM(BOTH '"' FROM sale_channel), ',', '","'), '"]')
WHERE sale_channel IS NOT NULL
  AND sale_channel != ''
  AND sale_channel NOT LIKE '[%';
