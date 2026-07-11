package com.covex.service.util;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 产品 JSON 属性读取工具类。
 * <p>
 * 解决数据库中已有产品字段命名不一致问题（如 maxAge / max_applicant_age / max_insured_age），
 * 提供多 key 降级查找能力。
 */
public final class ProductAttributeHelper {

    private ProductAttributeHelper() {}

    // ===== 标准属性 key 常量 =====

    /** 被保人最大投保年龄 */
    public static final String MAX_INSURED_AGE = "max_insured_age";
    /** 被保人最小投保年龄 */
    public static final String MIN_INSURED_AGE = "min_insured_age";
    /** 最大保额 */
    public static final String MAX_SUM_INSURED = "max_sum_insured";
    /** 最小保额 */
    public static final String MIN_SUM_INSURED = "min_sum_insured";
    /** 最大满期年龄 */
    public static final String MAX_MATURITY_AGE = "max_maturity_age";

    // ===== capabilities key 常量 =====

    /** 共享保额开关 */
    public static final String CAP_SHARED_AMOUNT = "shared_amount";
    /** 共享保额上限（可选，默认使用 max_sum_insured） */
    public static final String CAP_SHARED_AMOUNT_LIMIT = "shared_amount_limit";

    // ===== coverage_detail key 常量 =====

    /** 保障是否使用共享保额池 */
    public static final String COV_USE_SHARED_AMOUNT = "use_shared_amount";
    /** 保障是否计入总保额校验 */
    public static final String COV_COUNT_TOWARD_AMOUNT = "count_toward_amount";

    // ===== 多 key 降级读取方法 =====

    /**
     * 从 Map 中按顺序尝试多个 key，返回第一个找到的 Integer 值。
     * 所有 key 均不存在时返回 defaultValue。
     */
    public static Integer getIntAttribute(Map<String, Object> map, int defaultValue, String... keys) {
        if (map == null) return defaultValue;
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                try {
                    return Integer.parseInt(val.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return defaultValue;
    }

    /**
     * 从 Map 中按顺序尝试多个 key，返回第一个找到的 BigDecimal 值。
     */
    public static BigDecimal getBigDecimalAttribute(Map<String, Object> map, BigDecimal defaultValue, String... keys) {
        if (map == null) return defaultValue;
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                try {
                    return new BigDecimal(val.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return defaultValue;
    }

    /**
     * 从 Map 中按顺序尝试多个 key，返回第一个找到的 Boolean 值。
     */
    public static Boolean getBooleanCapability(Map<String, Object> map, Boolean defaultValue, String... keys) {
        if (map == null) return defaultValue;
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                if (val instanceof Boolean) return (Boolean) val;
                return Boolean.parseBoolean(val.toString());
            }
        }
        return defaultValue;
    }

    /**
     * 从 Map 中按顺序尝试多个 key，返回第一个找到的原始 Object 值。
     */
    public static Object getAttribute(Map<String, Object> map, String... keys) {
        if (map == null) return null;
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) return val;
        }
        return null;
    }

    // ===== 便捷方法（带标准降级 key） =====

    /** 读取被保人最大投保年龄，默认 70 */
    public static int getMaxInsuredAge(Map<String, Object> attributes) {
        return getIntAttribute(attributes, 70,
                MAX_INSURED_AGE, "max_insured_age", "maxAge", "max_applicant_age");
    }

    /** 读取被保人最小投保年龄，默认 0 */
    public static int getMinInsuredAge(Map<String, Object> attributes) {
        return getIntAttribute(attributes, 0,
                MIN_INSURED_AGE, "min_insured_age", "minAge", "min_applicant_age");
    }

    /** 读取最大保额，默认 1000 万 */
    public static BigDecimal getMaxSumInsured(Map<String, Object> attributes) {
        return getBigDecimalAttribute(attributes, new BigDecimal("10000000"),
                MAX_SUM_INSURED, "max_sum_insured", "maxSumInsured");
    }

    /** 读取最小保额，默认 0（不限制） */
    public static BigDecimal getMinSumInsured(Map<String, Object> attributes) {
        return getBigDecimalAttribute(attributes, BigDecimal.ZERO,
                MIN_SUM_INSURED, "min_sum_insured", "minSumInsured");
    }

    /** 读取最大满期年龄，默认 999（不限制） */
    public static int getMaxMaturityAge(Map<String, Object> attributes) {
        return getIntAttribute(attributes, 999,
                MAX_MATURITY_AGE, "max_maturity_age", "maxMaturityAge");
    }

    /**
     * 从投保单/保单的产品快照中提取 attributes Map。
     * 快照中 attributes 可能是 Map 或 toString 结果，需要安全转换。
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractAttributes(Map<String, Object> snapshot) {
        if (snapshot == null) return null;
        Object attrs = snapshot.get("attributes");
        if (attrs instanceof Map) {
            return (Map<String, Object>) attrs;
        }
        return null;
    }

    /**
     * 从投保单/保单的产品快照中提取 capabilities Map。
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractCapabilities(Map<String, Object> snapshot) {
        if (snapshot == null) return null;
        Object caps = snapshot.get("capabilities");
        if (caps instanceof Map) {
            return (Map<String, Object>) caps;
        }
        return null;
    }
}
