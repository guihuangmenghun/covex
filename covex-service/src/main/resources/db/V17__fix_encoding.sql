-- 修复 V15 种子数据导入时因 GBK→UTF8 编码问题导致的中文丢失
-- 执行: mysql -u root -p covex < fix_encoding.sql

SET NAMES utf8mb4;

-- 1. 修复 ins_product_coverage 保障名称
UPDATE ins_product_coverage SET coverage_name = '身故保险金' WHERE coverage_code = 'COV_DEATH' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '残疾保险金' WHERE coverage_code = 'COV_DISABILITY' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '全残保险金' WHERE coverage_code = 'COV_DISABILITY' AND coverage_name = '残疾保险金' AND id IN (2, 4, 24, 26, 46, 48);
UPDATE ins_product_coverage SET coverage_name = '重疾保险金' WHERE coverage_code = 'COV_CI' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '中症保险金' WHERE coverage_code = 'COV_MI' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '轻症保险金' WHERE coverage_code = 'COV_LI' AND (coverage_name = '???' OR coverage_name = '?????');
UPDATE ins_product_coverage SET coverage_name = '豁免保费' WHERE coverage_code = 'COV_WAIVE' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '车损险' WHERE coverage_code = 'COV_CT' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '三者险' WHERE coverage_code = 'COV_VD' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '盗抢险' WHERE coverage_code = 'COV_TP' AND coverage_name = '???';
UPDATE ins_product_coverage SET coverage_name = '车上人员责任险' WHERE coverage_code = 'COV_OP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外身故' WHERE coverage_code = 'COV_ACC_DEATH' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外残疾' WHERE coverage_code = 'COV_ACC_DIS' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外医疗' WHERE coverage_code = 'COV_ACC_MED' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '意外住院津贴' WHERE coverage_code = 'COV_ACC_HOSP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '火灾爆炸险' WHERE coverage_code = 'COV_FIRE' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '洪水风暴险' WHERE coverage_code = 'COV_FLOOD' AND (coverage_name = '????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '盗窃险' WHERE coverage_code = 'COV_THEFT' AND (coverage_name = '??' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '管道破裂险' WHERE coverage_code = 'COV_PIPE' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产基本险' WHERE coverage_code = 'COV_PROP_BASIC' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产综合险' WHERE coverage_code = 'COV_PROP_COMP' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '财产一切险' WHERE coverage_code = 'COV_PROP_ALL' AND (coverage_name = '?????' OR coverage_name = '???');
UPDATE ins_product_coverage SET coverage_name = '附加险' WHERE coverage_code = 'COV_RIDER' AND (coverage_name = '???' OR coverage_name = '?????');

-- 1.1 修复 ins_product_coverage 保障详情 description
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '被保人身故，给付基本保额') WHERE coverage_code = 'COV_DEATH' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '被保人全残，给付基本保额') WHERE coverage_code = 'COV_DISABILITY' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '确诊重大疾病，一次性给付基本保额') WHERE coverage_code = 'COV_CI' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '确诊中症疾病，给付50%基本保额') WHERE coverage_code = 'COV_MI' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '确诊轻症疾病，给付30%基本保额') WHERE coverage_code = 'COV_LI' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '确诊轻/中症后豁免后续保费') WHERE coverage_code = 'COV_WAIVE' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '车辆因碰撞、倾覆、火灾等造成的损失') WHERE coverage_code = 'COV_CT' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '第三者人身伤亡或财产损失') WHERE coverage_code = 'COV_VD' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '整车被盗抢、抢劫、抢夺造成的损失') WHERE coverage_code = 'COV_TP' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '车上人员因意外事故造成的人身伤亡') WHERE coverage_code = 'COV_OP' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因意外事故导致身故，给付基本保额') WHERE coverage_code = 'COV_ACC_DEATH' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因意外事故导致残疾，按残疾比例给付') WHERE coverage_code = 'COV_ACC_DIS' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因意外事故产生的医疗费用，扣除免赔额后按比例报销') WHERE coverage_code = 'COV_ACC_MED' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因意外事故住院，按日给付住院津贴') WHERE coverage_code = 'COV_ACC_HOSP' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因火灾、爆炸造成的财产损失') WHERE coverage_code = 'COV_FIRE' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因洪水、风暴等自然灾害造成的财产损失') WHERE coverage_code = 'COV_FLOOD' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因盗窃造成的财产损失') WHERE coverage_code = 'COV_THEFT' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';
UPDATE ins_product_coverage SET coverage_detail = JSON_SET(coverage_detail, '$.description', '因管道破裂造成的财产损失') WHERE coverage_code = 'COV_PIPE' AND JSON_EXTRACT(coverage_detail, '$.description') LIKE '%?%';

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
UPDATE ins_product_premium SET premium_plan_name = '趸交' WHERE premium_plan_code = 'PP_LUMP' AND premium_plan_name LIKE '%?%';
UPDATE ins_product_premium SET premium_plan_name = '年交' WHERE premium_plan_code = 'PP_ANNUAL' AND premium_plan_name LIKE '%?%';
UPDATE ins_product_premium SET premium_plan_name = '年交10年' WHERE premium_plan_code = 'PP_Y10' AND premium_plan_name LIKE '%?%';
UPDATE ins_product_premium SET premium_plan_name = '年交20年' WHERE premium_plan_code = 'PP_Y20' AND premium_plan_name LIKE '%?%';
UPDATE ins_product_premium SET premium_plan_name = '年交30年' WHERE premium_plan_code = 'PP_Y30' AND premium_plan_name LIKE '%?%';

-- 6. 修复 ins_product_rule 规则名称
UPDATE ins_product_rule SET rule_name = '投保年龄校验' WHERE rule_code = 'uw_age_check' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '健康告知校验' WHERE rule_code = 'uw_health_decl' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '职业类别校验' WHERE rule_code = 'uw_occupation' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '累计保额校验' WHERE rule_code = 'uw_cumulative_si' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '财务核保' WHERE rule_code = 'uw_financial' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '健康告知（严格）' WHERE rule_code = 'uw_health_strict' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '等待期校验' WHERE rule_code = 'uw_waiting' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '车龄校验' WHERE rule_code = 'uw_vehicle_age' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = 'NCD系数校验' WHERE rule_code = 'uw_ncd' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '车辆类型校验' WHERE rule_code = 'uw_vehicle_type' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '财产类型校验' WHERE rule_code = 'uw_property_type' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '估值校验' WHERE rule_code = 'uw_valuation' AND rule_name LIKE '%?%';
UPDATE ins_product_rule SET rule_name = '地区风险校验' WHERE rule_code = 'uw_region_risk' AND rule_name LIKE '%?%';

SELECT 'Encoding fix completed' AS result;
