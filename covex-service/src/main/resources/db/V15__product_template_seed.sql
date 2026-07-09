-- ============================================================
-- V15: 预置 6 个系统产品模板种子数据
-- tenant_id = 0 表示系统模板，所有租户可见
-- ============================================================

-- 通用 param_schema（所有模板共用，PM 填写的表单定义）
SET NAMES utf8mb4;

SET @param_schema = JSON_OBJECT(
  'sections', JSON_ARRAY(
    JSON_OBJECT(
      'title', '基本信息',
      'fields', JSON_ARRAY(
        JSON_OBJECT('key', 'product_name', 'label', '产品名称', 'type', 'string', 'required', true, 'placeholder', '如：XX定期寿险', 'default', ''),
        JSON_OBJECT('key', 'short_name', 'label', '产品简称', 'type', 'string', 'required', true, 'placeholder', '如：XX定寿', 'default', ''),
        JSON_OBJECT('key', 'product_code', 'label', '产品编码', 'type', 'string', 'required', true, 'placeholder', '英文+数字，如 SL001', 'default', '')
      )
    ),
    JSON_OBJECT(
      'title', '核心参数',
      'fields', JSON_ARRAY(
        JSON_OBJECT('key', 'max_insured_age', 'label', '最高投保年龄', 'type', 'number', 'required', true, 'default', 60, 'unit', '岁'),
        JSON_OBJECT('key', 'max_maturity_age', 'label', '最高满期年龄', 'type', 'number', 'required', true, 'default', 70, 'unit', '岁'),
        JSON_OBJECT('key', 'min_sum_insured', 'label', '最低保额', 'type', 'number', 'required', true, 'default', 100000, 'unit', '元'),
        JSON_OBJECT('key', 'max_sum_insured', 'label', '最高保额', 'type', 'number', 'required', true, 'default', 1000000, 'unit', '元')
      )
    ),
    JSON_OBJECT(
      'title', '缴费设置',
      'fields', JSON_ARRAY(
        JSON_OBJECT('key', 'payment_term', 'label', '缴费期间', 'type', 'select', 'required', true,
          'options', JSON_ARRAY(5, 10, 15, 20, 30), 'default', 20, 'unit', '年'),
        JSON_OBJECT('key', 'payment_frequency', 'label', '缴费频率', 'type', 'select', 'required', true,
          'options', JSON_ARRAY(
            JSON_OBJECT('value', 12, 'label', '年交'),
            JSON_OBJECT('value', 6, 'label', '半年交'),
            JSON_OBJECT('value', 1, 'label', '月交')
          ), 'default', 12)
      )
    )
  )
);

-- ===================== 1. 定期寿险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'TERM_LIFE', '定期寿险模板',
  '适合家庭经济支柱的身故保障，保到60/70岁。覆盖身故和全残两大责任，费率低、保额高。',
  1, 'Timer', 1, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 1, 'term_type', 1, 'main_rider_flag', 1,
      'sale_channel', JSON_ARRAY('1', '2', '3'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'policy_loan', false),
      'attributes', JSON_OBJECT('max_insured_age', 60, 'max_maturity_age', 70, 'min_sum_insured', 100000, 'max_sum_insured', 1000000, 'waiting_period', 0)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'coverage_name', '身故保险金', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '被保人身故，给付基本保额', 'trigger', 'death'), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'coverage_name', '全残保险金', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '被保人全残，给付基本保额', 'trigger', 'total_disability'), 'sort_order', 2)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_Y20', 'premium_plan_name', '年交20年', 'payment_frequency', 12, 'payment_term', 20, 'payment_term_unit', 1, 'grace_period', 60),
      JSON_OBJECT('premium_plan_code', 'PP_Y30', 'premium_plan_name', '年交30年', 'payment_frequency', 12, 'payment_term', 30, 'payment_term_unit', 1, 'grace_period', 60)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'premium_plan_code', 'PP_Y30'),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'premium_plan_code', 'PP_Y30')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_age_check', 'rule_name', '投保年龄校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_health_decl', 'rule_name', '健康告知校验', 'sort_order', 2, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_occupation', 'rule_name', '职业类别校验', 'sort_order', 3, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_cumulative_si', 'rule_name', '累计保额校验', 'sort_order', 4, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));

-- ===================== 2. 终身寿险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'WHOLE_LIFE', '终身寿险模板',
  '终身保障+现金价值积累，兼具保障和储蓄功能。适合有资产传承需求的高净值客户。',
  1, 'Infinity', 2, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 1, 'term_type', 2, 'main_rider_flag', 1,
      'sale_channel', JSON_ARRAY('1', '2'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'policy_loan', true),
      'attributes', JSON_OBJECT('max_insured_age', 65, 'max_maturity_age', 105, 'min_sum_insured', 200000, 'max_sum_insured', 5000000, 'has_cash_value', true)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'coverage_name', '身故保险金', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '被保人身故，给付基本保额或现金价值取大', 'trigger', 'death'), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'coverage_name', '全残保险金', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '被保人全残，给付基本保额', 'trigger', 'total_disability'), 'sort_order', 2)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_LUMP', 'premium_plan_name', '趸交', 'payment_frequency', 1, 'payment_term', 1, 'payment_term_unit', 1, 'grace_period', 0),
      JSON_OBJECT('premium_plan_code', 'PP_Y10', 'premium_plan_name', '年交10年', 'payment_frequency', 12, 'payment_term', 10, 'payment_term_unit', 1, 'grace_period', 60),
      JSON_OBJECT('premium_plan_code', 'PP_Y20', 'premium_plan_name', '年交20年', 'payment_frequency', 12, 'payment_term', 20, 'payment_term_unit', 1, 'grace_period', 60)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'premium_plan_code', 'PP_LUMP'),
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'premium_plan_code', 'PP_Y10'),
      JSON_OBJECT('coverage_code', 'COV_DEATH', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'premium_plan_code', 'PP_LUMP'),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'premium_plan_code', 'PP_Y10'),
      JSON_OBJECT('coverage_code', 'COV_DISABILITY', 'premium_plan_code', 'PP_Y20')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_age_check', 'rule_name', '投保年龄校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_health_decl', 'rule_name', '健康告知校验', 'sort_order', 2, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_occupation', 'rule_name', '职业类别校验', 'sort_order', 3, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_financial', 'rule_name', '财务核保', 'sort_order', 4, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));

-- ===================== 3. 重疾险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'CRITICAL_ILLNESS', '重疾险模板',
  '确诊即赔的重大疾病保障。可选附加中症、轻症和豁免保费，覆盖120种重疾+20种中症+40种轻症。',
  3, 'Heart', 3, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 1, 'term_type', 1, 'main_rider_flag', 1,
      'sale_channel', JSON_ARRAY('1', '2', '3'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', false, 'policy_loan', false),
      'attributes', JSON_OBJECT('max_insured_age', 55, 'max_maturity_age', 70, 'min_sum_insured', 100000, 'max_sum_insured', 500000, 'waiting_period', 90, 'critical_illness_count', 120)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_CI', 'coverage_name', '重疾保险金', 'selection_mode', 1, 'benefit_type', 1,
        'coverage_detail', JSON_OBJECT('description', '确诊重大疾病，一次性给付基本保额', 'illness_count', 120), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_MI', 'coverage_name', '中症保险金', 'selection_mode', 2, 'benefit_type', 1,
        'coverage_detail', JSON_OBJECT('description', '确诊中症疾病，给付50%基本保额', 'illness_count', 20, 'payout_ratio', 0.5), 'sort_order', 2),
      JSON_OBJECT('coverage_code', 'COV_LI', 'coverage_name', '轻症保险金', 'selection_mode', 2, 'benefit_type', 1,
        'coverage_detail', JSON_OBJECT('description', '确诊轻症疾病，给付30%基本保额', 'illness_count', 40, 'payout_ratio', 0.3), 'sort_order', 3),
      JSON_OBJECT('coverage_code', 'COV_WAIVE', 'coverage_name', '豁免保费', 'selection_mode', 2, 'benefit_type', 3,
        'coverage_detail', JSON_OBJECT('description', '确诊轻/中症后豁免后续保费', 'trigger', 'mild_or_moderate'), 'sort_order', 4)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_Y20', 'premium_plan_name', '年交20年', 'payment_frequency', 12, 'payment_term', 20, 'payment_term_unit', 1, 'grace_period', 60),
      JSON_OBJECT('premium_plan_code', 'PP_Y30', 'premium_plan_name', '年交30年', 'payment_frequency', 12, 'payment_term', 30, 'payment_term_unit', 1, 'grace_period', 60)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_CI', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_CI', 'premium_plan_code', 'PP_Y30'),
      JSON_OBJECT('coverage_code', 'COV_MI', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_MI', 'premium_plan_code', 'PP_Y30'),
      JSON_OBJECT('coverage_code', 'COV_LI', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_LI', 'premium_plan_code', 'PP_Y30'),
      JSON_OBJECT('coverage_code', 'COV_WAIVE', 'premium_plan_code', 'PP_Y20'),
      JSON_OBJECT('coverage_code', 'COV_WAIVE', 'premium_plan_code', 'PP_Y30')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_age_check', 'rule_name', '投保年龄校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_health_strict', 'rule_name', '严格健康告知', 'sort_order', 2, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_occupation', 'rule_name', '职业类别校验', 'sort_order', 3, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_waiting', 'rule_name', '等待期校验', 'sort_order', 4, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));

-- ===================== 4. 车险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'AUTO_INSURANCE', '车险模板',
  '机动车辆保险，覆盖交强险和商业险。支持NCD无赔优待系数，按车型/车龄/地区定价。',
  4, 'Van', 4, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 2, 'term_type', 3, 'main_rider_flag', 0,
      'sale_channel', JSON_ARRAY('1', '2', '4'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'policy_loan', false),
      'attributes', JSON_OBJECT('max_insured_age', 99, 'vehicle_age_max', 15, 'ncd_max_discount', 0.3)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_CT', 'coverage_name', '交强险', 'selection_mode', 1, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '机动车交通事故责任强制保险', 'mandatory', true), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_VD', 'coverage_name', '车损险', 'selection_mode', 2, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '被保险车辆损失赔偿', 'trigger', 'vehicle_damage'), 'sort_order', 2),
      JSON_OBJECT('coverage_code', 'COV_TP', 'coverage_name', '三者险', 'selection_mode', 2, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '第三者责任赔偿', 'trigger', 'third_party_liability'), 'sort_order', 3),
      JSON_OBJECT('coverage_code', 'COV_OP', 'coverage_name', '车上人员险', 'selection_mode', 2, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '车上人员伤亡赔偿', 'trigger', 'passenger_injury'), 'sort_order', 4)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_ANNUAL', 'premium_plan_name', '年度趸交', 'payment_frequency', 1, 'payment_term', 1, 'payment_term_unit', 1, 'grace_period', 0)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_CT', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_VD', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_TP', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_OP', 'premium_plan_code', 'PP_ANNUAL')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_vehicle_age', 'rule_name', '车辆年限校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_ncd', 'rule_name', 'NCD无赔优待', 'sort_order', 2, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_vehicle_type', 'rule_name', '车型校验', 'sort_order', 3, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));

-- ===================== 5. 财产险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'PROPERTY_INSURANCE', '财产险模板',
  '企业/家庭财产保障，覆盖火灾爆炸、暴雨洪水、盗窃等灾因。可按财产类型和地区差异化定价。',
  5, 'House', 5, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 2, 'term_type', 3, 'main_rider_flag', 0,
      'sale_channel', JSON_ARRAY('1', '3'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'policy_loan', false),
      'attributes', JSON_OBJECT('max_insured_age', 99, 'deductible_rate', 0.05, 'max_sum_insured', 10000000)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_FIRE', 'coverage_name', '火灾爆炸', 'selection_mode', 1, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '火灾、爆炸导致的财产损失', 'trigger', 'fire_explosion'), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_FLOOD', 'coverage_name', '暴雨洪水', 'selection_mode', 2, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '暴雨、洪水导致的财产损失', 'trigger', 'flood_storm'), 'sort_order', 2),
      JSON_OBJECT('coverage_code', 'COV_THEFT', 'coverage_name', '盗窃', 'selection_mode', 2, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '入室盗窃导致的财产损失', 'trigger', 'theft'), 'sort_order', 3),
      JSON_OBJECT('coverage_code', 'COV_PIPE', 'coverage_name', '水暖管爆裂', 'selection_mode', 2, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '水暖管道爆裂导致的财产损失', 'trigger', 'pipe_burst'), 'sort_order', 4)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_ANNUAL', 'premium_plan_name', '年度趸交', 'payment_frequency', 1, 'payment_term', 1, 'payment_term_unit', 1, 'grace_period', 0)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_FIRE', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_FLOOD', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_THEFT', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_PIPE', 'premium_plan_code', 'PP_ANNUAL')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_property_type', 'rule_name', '财产类型校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_valuation', 'rule_name', '标的估值校验', 'sort_order', 2, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_region_risk', 'rule_name', '地区风险校验', 'sort_order', 3, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));

-- ===================== 6. 意外险模板 =====================
INSERT INTO ins_product_template
  (tenant_id, template_code, template_name, template_desc, product_type, icon, sort_order, is_active, template_data, param_schema)
VALUES (0, 'ACCIDENT_INSURANCE', '意外险模板',
  '意外伤害保障（个人/团体），覆盖意外身故、伤残、医疗和住院津贴。投保门槛低，费率按职业类别差异化。',
  2, 'Shield', 6, 1,
  JSON_OBJECT(
    'product', JSON_OBJECT(
      'product_nature', 2, 'term_type', 3, 'main_rider_flag', 0,
      'sale_channel', JSON_ARRAY('1', '2', '3'),
      'capabilities', JSON_OBJECT('online_sale', true, 'auto_underwrite', true, 'policy_loan', false),
      'attributes', JSON_OBJECT('max_insured_age', 65, 'min_sum_insured', 10000, 'max_sum_insured', 500000, 'max_occupation_class', 4)
    ),
    'coverages', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_ACC_DEATH', 'coverage_name', '意外身故', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '因意外事故身故，给付基本保额', 'trigger', 'accidental_death'), 'sort_order', 1),
      JSON_OBJECT('coverage_code', 'COV_ACC_DIS', 'coverage_name', '意外伤残', 'selection_mode', 1, 'benefit_type', 4,
        'coverage_detail', JSON_OBJECT('description', '因意外事故伤残，按伤残等级比例给付', 'trigger', 'accidental_disability'), 'sort_order', 2),
      JSON_OBJECT('coverage_code', 'COV_ACC_MED', 'coverage_name', '意外医疗', 'selection_mode', 2, 'benefit_type', 2,
        'coverage_detail', JSON_OBJECT('description', '因意外事故产生的医疗费用报销', 'trigger', 'accidental_medical', 'deductible', 100, 'reimburse_ratio', 0.8), 'sort_order', 3),
      JSON_OBJECT('coverage_code', 'COV_ACC_HOSP', 'coverage_name', '意外住院津贴', 'selection_mode', 2, 'benefit_type', 5,
        'coverage_detail', JSON_OBJECT('description', '因意外住院按日给付津贴', 'trigger', 'accidental_hospitalization', 'daily_amount', 200, 'max_days', 180), 'sort_order', 4)
    ),
    'premium_plans', JSON_ARRAY(
      JSON_OBJECT('premium_plan_code', 'PP_ANNUAL', 'premium_plan_name', '年度趸交', 'payment_frequency', 1, 'payment_term', 1, 'payment_term_unit', 1, 'grace_period', 0)
    ),
    'coverage_premium_rels', JSON_ARRAY(
      JSON_OBJECT('coverage_code', 'COV_ACC_DEATH', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_ACC_DIS', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_ACC_MED', 'premium_plan_code', 'PP_ANNUAL'),
      JSON_OBJECT('coverage_code', 'COV_ACC_HOSP', 'premium_plan_code', 'PP_ANNUAL')
    ),
    'rules', JSON_ARRAY(
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_age_check', 'rule_name', '投保年龄校验', 'sort_order', 1, 'is_active', 1),
      JSON_OBJECT('rule_type', 1, 'rule_engine', 'aviator', 'rule_code', 'uw_occupation_class', 'rule_name', '职业类别校验(1-4类)', 'sort_order', 2, 'is_active', 1)
    )
  ),
  CAST(@param_schema AS JSON));
