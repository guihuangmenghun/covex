-- Covex 预置字典数据 (39组)
-- S2: 数据字典服务

-- ========== 合并：共享字典类型 (16组) ==========

-- period_unit（期间单位）
SET NAMES utf8mb4;

INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'period_unit', '1', '年', 1),
(0, 'period_unit', '2', '月', 2),
(0, 'period_unit', '3', '日', 3),
(0, 'period_unit', '4', '年龄', 4);

-- product_type（产品分类）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'product_type', '1', '寿险', 1),
(0, 'product_type', '2', '意外险', 2),
(0, 'product_type', '3', '健康险', 3),
(0, 'product_type', '4', '车险', 4),
(0, 'product_type', '5', '财产险', 5),
(0, 'product_type', '6', '责任险', 6),
(0, 'product_type', '7', '乘务险', 7);

-- term_type（期限类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'term_type', '1', '长期险', 1),
(0, 'term_type', '2', '一年期险', 2),
(0, 'term_type', '3', '极短期险', 3);

-- main_rider_flag（主附险）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'main_rider_flag', '1', '主险', 1),
(0, 'main_rider_flag', '2', '附加险', 2),
(0, 'main_rider_flag', '3', '两者皆可', 3);

-- product_nature（产品性质）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'product_nature', '1', '个人险', 1),
(0, 'product_nature', '2', '团体险', 2),
(0, 'product_nature', '3', '银行代理', 3),
(0, 'product_nature', '4', '综合', 4);

-- payment_frequency（缴费频率）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'payment_frequency', '0', '趸交', 1),
(0, 'payment_frequency', '1', '月交', 2),
(0, 'payment_frequency', '3', '季交', 3),
(0, 'payment_frequency', '6', '半年交', 4),
(0, 'payment_frequency', '12', '年交', 5),
(0, 'payment_frequency', '99', '不定期', 6);

-- rounding_mode（取整方式）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'rounding_mode', '1', '四舍五入', 1),
(0, 'rounding_mode', '2', '截断', 2),
(0, 'rounding_mode', '3', '进位', 3);

-- selection_mode（选择方式）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'selection_mode', '1', '必选', 1),
(0, 'selection_mode', '2', '可选', 2);

-- beneficiary_type（受益人类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'beneficiary_type', '0', '无限制', 1),
(0, 'beneficiary_type', '1', '被保险人', 2),
(0, 'beneficiary_type', '2', '投保人', 3);

-- calc_direction（计算方向）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'calc_direction', '1', '保费算保额', 1),
(0, 'calc_direction', '2', '保额算保费', 2),
(0, 'calc_direction', '3', '保费保额互算', 3),
(0, 'calc_direction', '4', '录入保费保额', 4),
(0, 'calc_direction', '5', '其他因素算保费保额', 5);

-- dividend_mode（红利领取方式）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'dividend_mode', '1', '领取现金', 1),
(0, 'dividend_mode', '2', '抵缴保费', 2),
(0, 'dividend_mode', '3', '增额缴清', 3),
(0, 'dividend_mode', '4', '累积生息', 4);

-- life_category（寿险分类）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'life_category', '1', '传统险', 1),
(0, 'life_category', '2', '分红险', 2),
(0, 'life_category', '3', '投连险', 3),
(0, 'life_category', '4', '万能险', 4),
(0, 'life_category', '5', '其他', 5);

-- health_category（健康险细分）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'health_category', '1', '医疗保险', 1),
(0, 'health_category', '2', '普通疾病保险', 2),
(0, 'health_category', '3', '重大疾病保险', 3),
(0, 'health_category', '4', '失能保险', 4),
(0, 'health_category', '5', '护理保险', 5);

-- liability_type（事故责任分类）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'liability_type', '1', '终身', 1),
(0, 'liability_type', '2', '两全及生存', 2),
(0, 'liability_type', '3', '定期', 3),
(0, 'liability_type', '4', '年金', 4);

-- ========== 新增：新系统需要的枚举 (15组) ==========

-- policy_type（保单类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'policy_type', '1', '标准保单', 1),
(0, 'policy_type', '2', '卡单', 2),
(0, 'policy_type', '3', '团单', 3);

-- underwriting_type（核保类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'underwriting_type', '1', '承保核保', 1),
(0, 'underwriting_type', '2', '保全核保', 2),
(0, 'underwriting_type', '3', '续保核保', 3),
(0, 'underwriting_type', '4', '核赔', 4);

-- underwriting_result（核保结论）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'underwriting_result', '1', '标准体通过', 1),
(0, 'underwriting_result', '2', '次标准体加费', 2),
(0, 'underwriting_result', '3', '除外责任', 3),
(0, 'underwriting_result', '4', '延期', 4),
(0, 'underwriting_result', '5', '拒保', 5),
(0, 'underwriting_result', '6', '转人工', 6);

-- benefit_type（给付类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'benefit_type', '1', '生存金', 1),
(0, 'benefit_type', '2', '满期金', 2),
(0, 'benefit_type', '3', '年金', 3),
(0, 'benefit_type', '4', '理赔金', 4),
(0, 'benefit_type', '5', '津贴', 5);

-- claim_category（理赔统计类别）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'claim_category', '1', '医疗', 1),
(0, 'claim_category', '2', '伤残', 2),
(0, 'claim_category', '3', '身故', 3),
(0, 'claim_category', '4', '返还', 4);

-- endorsement_type（保全类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'endorsement_type', '1', '退保', 1),
(0, 'endorsement_type', '2', '犹豫期退保', 2),
(0, 'endorsement_type', '3', '保全变更', 3),
(0, 'endorsement_type', '4', '借款', 4),
(0, 'endorsement_type', '5', '还款', 5);

-- amount_sales_mode（保额销售方式）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'amount_sales_mode', '1', '按保额销售', 1),
(0, 'amount_sales_mode', '2', '按份数销售', 2),
(0, 'amount_sales_mode', '3', '按档次销售', 3);

-- treatment_type（给付方式）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'treatment_type', '1', '费用补偿型', 1),
(0, 'treatment_type', '2', '定额给付型', 2);

-- term_length_type（期限长度分类）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'term_length_type', '1', '一年期以内', 1),
(0, 'term_length_type', '2', '一年期', 2),
(0, 'term_length_type', '3', '一年期以上', 3);

-- gender_linked（性别关联）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'gender_linked', '0', '无关联', 1),
(0, 'gender_linked', '1', '男性', 2),
(0, 'gender_linked', '2', '女性', 3);

-- effective_date_mode（生效日算法）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'effective_date_mode', '1', '签单当天生效', 1),
(0, 'effective_date_mode', '2', '签单次日生效', 2),
(0, 'effective_date_mode', '3', '首期交费次日生效', 3),
(0, 'effective_date_mode', '4', '财务到帐日次日生效', 4);

-- group_flag（团个标志）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'group_flag', '1', '个人', 1),
(0, 'group_flag', '2', '团体', 2);

-- channel_type（渠道类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'channel_type', '1', '个人代理人', 1),
(0, 'channel_type', '2', '公司直销', 2),
(0, 'channel_type', '3', '专业代理公司', 3),
(0, 'channel_type', '4', '银行/邮政', 4),
(0, 'channel_type', '5', '经纪公司', 5),
(0, 'channel_type', '6', '互联网平台', 6);

-- channel_status（渠道状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'channel_status', '1', '待审核', 1),
(0, 'channel_status', '2', '已签约', 2),
(0, 'channel_status', '3', '已暂停', 3),
(0, 'channel_status', '4', '已终止', 4);

-- proposal_status（投保单状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'proposal_status', '1', '待校验', 1),
(0, 'proposal_status', '2', '待核保', 2),
(0, 'proposal_status', '3', '核保中', 3),
(0, 'proposal_status', '4', '待支付', 4),
(0, 'proposal_status', '5', '已支付(待出单)', 5),
(0, 'proposal_status', '6', '已出单', 6),
(0, 'proposal_status', '7', '已拒保', 7),
(0, 'proposal_status', '8', '已撤销', 8);

-- policy_status（保单状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'policy_status', '1', '有效', 1),
(0, 'policy_status', '2', '中止', 2),
(0, 'policy_status', '3', '终止', 3);

-- termination_reason（终止原因）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'termination_reason', '1', '满期终止', 1),
(0, 'termination_reason', '2', '退保终止', 2),
(0, 'termination_reason', '3', '犹豫期退保', 3),
(0, 'termination_reason', '4', '理赔终止', 4),
(0, 'termination_reason', '5', '身故终止', 5),
(0, 'termination_reason', '6', '复效超期', 6),
(0, 'termination_reason', '7', '拒保终止', 7);

-- claim_status（理赔案件状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'claim_status', '1', '已报案', 1),
(0, 'claim_status', '2', '审核中', 2),
(0, 'claim_status', '3', '需调查', 3),
(0, 'claim_status', '4', '已赔付', 4),
(0, 'claim_status', '5', '已拒赔', 5),
(0, 'claim_status', '6', '已结案', 6);

-- payment_type（支付类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'payment_type', '1', '首期保费', 1),
(0, 'payment_type', '2', '续期保费', 2),
(0, 'payment_type', '3', '退保金', 3),
(0, 'payment_type', '4', '理赔金', 4),
(0, 'payment_type', '5', '保单借款', 5);

-- payment_status（支付状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'payment_status', '1', '待支付', 1),
(0, 'payment_status', '2', '已支付', 2),
(0, 'payment_status', '3', '已退款', 3),
(0, 'payment_status', '4', '支付失败', 5);

-- id_type（证件类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'id_type', '1', '身份证', 1),
(0, 'id_type', '2', '护照', 2),
(0, 'id_type', '3', '军官证', 3),
(0, 'id_type', '4', '港澳通行证', 4),
(0, 'id_type', '5', '统一社会信用代码', 5),
(0, 'id_type', '6', '其他', 6);

-- address_type（地址类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'address_type', '1', '户籍地址', 1),
(0, 'address_type', '2', '常住地址', 2),
(0, 'address_type', '3', '工作地址', 3),
(0, 'address_type', '4', '通讯地址', 4);

-- account_usage_type（银行账户用途）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'account_usage_type', '1', '缴费扣款', 1),
(0, 'account_usage_type', '2', '理赔收款', 2),
(0, 'account_usage_type', '3', '两者皆可', 3);

-- rule_engine_type（规则引擎类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'rule_engine_type', '1', 'LiteFlow', 1),
(0, 'rule_engine_type', '2', 'Aviator', 2),
(0, 'rule_engine_type', '3', 'Java', 3);

-- version_status（版本状态）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'version_status', '1', '草稿', 1),
(0, 'version_status', '2', '待审批', 2),
(0, 'version_status', '3', '已发布', 3),
(0, 'version_status', '4', '已冻结', 5);

-- document_type（文档类型）
INSERT INTO ins_dict (tenant_id, dict_type, dict_code, dict_name, sort_order) VALUES
(0, 'document_type', '1', '产品条款', 1),
(0, 'document_type', '2', '费率表', 2),
(0, 'document_type', '3', '投保须知', 3),
(0, 'document_type', '4', '产品说明书', 4);
