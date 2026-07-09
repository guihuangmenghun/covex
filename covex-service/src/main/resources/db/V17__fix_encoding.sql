-- 修复 V15 种子数据导入时因 GBK→UTF8 编码问题导致的中文丢失
-- 执行: mysql -u root -p covex < fix_encoding.sql

SET NAMES utf8mb4;

-- 1. 修复 ins_product_coverage 保障名称
UPDATE ins_product_coverage SET coverage_name = '身故保险金' WHERE coverage_code = 'COV_DEATH' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '残疾保险金' WHERE coverage_code = 'COV_DISABILITY' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '重疾保险金' WHERE coverage_code = 'COV_CI' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '轻症保险金' WHERE coverage_code = 'COV_LI' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '车损险' WHERE coverage_code = 'COV_CT' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '三者险' WHERE coverage_code = 'COV_VD' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '盗抢险' WHERE coverage_code = 'COV_TP' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '车上人员责任险' WHERE coverage_code = 'COV_OP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外身故' WHERE coverage_code = 'COV_ACC_DEATH' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外残疾' WHERE coverage_code = 'COV_ACC_DIS' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外医疗' WHERE coverage_code = 'COV_ACC_MED' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外住院津贴' WHERE coverage_code = 'COV_ACC_HOSP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产基本险' WHERE coverage_code = 'COV_PROP_BASIC' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产综合险' WHERE coverage_code = 'COV_PROP_COMP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产一切险' WHERE coverage_code = 'COV_PROP_ALL' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '附加险' WHERE coverage_code = 'COV_RIDER' AND (coverage_name = '???' OR coverage_name = '?????');

-- 2. 修复 ins_product 产品名称
UPDATE ins_product SET product_name = '定期寿险测试产品' WHERE product_code = 'TS058TL01' AND product_name LIKE 'TS058%';
UPDATE ins_product SET product_name = '终身寿险测试产品' WHERE product_code = 'TS058WL01' AND product_name LIKE 'TS058%';
UPDATE ins_product SET product_name = '重疾险测试产品' WHERE product_code = 'TS058CI01' AND product_name LIKE 'TS058%';
UPDATE ins_product SET product_name = '车险测试产品' WHERE product_code = 'TS058AU01' AND product_name LIKE 'TS058%';
UPDATE ins_product SET product_name = '财产险测试产品' WHERE product_code = 'TS058PI01' AND product_name LIKE 'TS058%';
UPDATE ins_product SET product_name = '意外险测试产品' WHERE product_code = 'TS058AI01' AND product_name LIKE 'TS058%';

-- 3. 修复 ins_product_template 模板名称和描述
UPDATE ins_product_template SET template_name = '定期寿险', template_desc = '保障期间可选，到期未发生保险责任则合同终止，适合家庭经济支柱' WHERE template_code = 'TERM_LIFE';
UPDATE ins_product_template SET template_name = '终身寿险', template_desc = '保障终身，必然赔付，兼具保障与财富传承功能' WHERE template_code = 'WHOLE_LIFE';
UPDATE ins_product_template SET template_name = '重疾险', template_desc = '确诊合同约定重大疾病即赔付，弥补收入损失和医疗费用' WHERE template_code = 'CRITICAL_ILLNESS';
UPDATE ins_product_template SET template_name = '车险', template_desc = '机动车辆保险，包含车损险、三者险、盗抢险等主附险组合' WHERE template_code = 'AUTO_INSURANCE';
UPDATE ins_product_template SET template_name = '财产险', template_desc = '企业财产保险，保障火灾、爆炸等自然灾害和意外事故造成的财产损失' WHERE template_code = 'PROPERTY_INSURANCE';
UPDATE ins_product_template SET template_name = '意外险', template_desc = '人身意外伤害保险，保障意外身故、残疾、医疗及住院津贴' WHERE template_code = 'ACCIDENT_INSURANCE';

-- 4. 修复 ins_product_rule 规则名称
UPDATE ins_product_rule SET rule_name = '投保年龄校验' WHERE rule_code = 'uw_age_check' AND (rule_name = '??????' OR rule_name = '???');
UPDATE ins_product_rule SET rule_name = '健康告知' WHERE rule_code = 'uw_health_decl' AND (rule_name = '??????' OR rule_name = '???');
UPDATE ins_product_rule SET rule_name = '职业类别校验' WHERE rule_code = 'uw_occupation' AND (rule_name = '??????' OR rule_name = '???');
UPDATE ins_product_rule SET rule_name = '累计保额校验' WHERE rule_code = 'uw_cumulative_si' AND (rule_name = '??????' OR rule_name = '???');
UPDATE ins_product_rule SET rule_name = '职业类别校验(1-4类)' WHERE rule_code = 'uw_occupation_class' AND (rule_name = '??????(1-4?)' OR rule_name = '???');

-- 5. 修复 ins_product_premium 缴费计划名称
UPDATE ins_product_premium SET premium_plan_name = '趸交' WHERE premium_plan_code = 'PP_SINGLE' AND (premium_plan_name = '??' OR premium_plan_name = '?');
UPDATE ins_product_premium SET premium_plan_name = '年交' WHERE premium_plan_code = 'PP_ANNUAL' AND (premium_plan_name = '??' OR premium_plan_name = '?');
UPDATE ins_product_premium SET premium_plan_name = '半年交' WHERE premium_plan_code = 'PP_SEMI' AND (premium_plan_name = '????' OR premium_plan_name = '???');
UPDATE ins_product_premium SET premium_plan_name = '季交' WHERE premium_plan_code = 'PP_QUARTERLY' AND (premium_plan_name = '??' OR premium_plan_name = '?');
UPDATE ins_product_premium SET premium_plan_name = '月交' WHERE premium_plan_code = 'PP_MONTHLY' AND (premium_plan_name = '??' OR premium_plan_name = '?');
UPDATE ins_product_premium SET premium_plan_name = '20年交' WHERE premium_plan_code LIKE '%20%' AND premium_plan_name = '20???';

SELECT 'Encoding fix completed' AS result;
