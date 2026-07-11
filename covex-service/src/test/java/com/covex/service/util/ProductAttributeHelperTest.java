package com.covex.service.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductAttributeHelper 单元测试
 */
class ProductAttributeHelperTest {

    // ===== getIntAttribute =====

    @Test
    void getIntAttribute_firstKeyFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("max_insured_age", 65);
        map.put("maxAge", 60);
        assertEquals(65, ProductAttributeHelper.getIntAttribute(map, 70, "max_insured_age", "maxAge"));
    }

    @Test
    void getIntAttribute_fallbackToSecondKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxAge", 60);
        assertEquals(60, ProductAttributeHelper.getIntAttribute(map, 70, "max_insured_age", "maxAge"));
    }

    @Test
    void getIntAttribute_defaultWhenNotFound() {
        Map<String, Object> map = new HashMap<>();
        assertEquals(70, ProductAttributeHelper.getIntAttribute(map, 70, "max_insured_age", "maxAge"));
    }

    @Test
    void getIntAttribute_nullMap() {
        assertEquals(70, ProductAttributeHelper.getIntAttribute(null, 70, "max_insured_age"));
    }

    @Test
    void getIntAttribute_stringValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxAge", "65");
        assertEquals(65, ProductAttributeHelper.getIntAttribute(map, 70, "maxAge"));
    }

    // ===== getBigDecimalAttribute =====

    @Test
    void getBigDecimalAttribute_found() {
        Map<String, Object> map = new HashMap<>();
        map.put("min_sum_insured", 10000);
        assertEquals(new BigDecimal("10000"),
                ProductAttributeHelper.getBigDecimalAttribute(map, BigDecimal.ZERO, "min_sum_insured"));
    }

    @Test
    void getBigDecimalAttribute_stringValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("max_sum_insured", "500000.50");
        assertEquals(new BigDecimal("500000.50"),
                ProductAttributeHelper.getBigDecimalAttribute(map, BigDecimal.ZERO, "max_sum_insured"));
    }

    // ===== getBooleanCapability =====

    @Test
    void getBooleanCapability_true() {
        Map<String, Object> map = new HashMap<>();
        map.put("shared_amount", true);
        assertTrue(ProductAttributeHelper.getBooleanCapability(map, false, "shared_amount"));
    }

    @Test
    void getBooleanCapability_stringTrue() {
        Map<String, Object> map = new HashMap<>();
        map.put("shared_amount", "true");
        assertTrue(ProductAttributeHelper.getBooleanCapability(map, false, "shared_amount"));
    }

    @Test
    void getBooleanCapability_defaultFalse() {
        Map<String, Object> map = new HashMap<>();
        assertFalse(ProductAttributeHelper.getBooleanCapability(map, false, "shared_amount"));
    }

    // ===== 便捷方法 =====

    @Test
    void getMaxInsuredAge_standardKey() {
        Map<String, Object> attrs = Map.of("max_insured_age", 60);
        assertEquals(60, ProductAttributeHelper.getMaxInsuredAge(attrs));
    }

    @Test
    void getMaxInsuredAge_legacyKey() {
        Map<String, Object> attrs = Map.of("maxAge", 55);
        assertEquals(55, ProductAttributeHelper.getMaxInsuredAge(attrs));
    }

    @Test
    void getMaxInsuredAge_default() {
        assertEquals(70, ProductAttributeHelper.getMaxInsuredAge(Map.of()));
    }

    @Test
    void getMinSumInsured_standardKey() {
        Map<String, Object> attrs = Map.of("min_sum_insured", 50000);
        assertEquals(new BigDecimal("50000"), ProductAttributeHelper.getMinSumInsured(attrs));
    }

    // ===== extractAttributes / extractCapabilities =====

    @Test
    void extractAttributes_fromSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        Map<String, Object> attrs = Map.of("max_insured_age", 60);
        snapshot.put("attributes", attrs);
        assertNotNull(ProductAttributeHelper.extractAttributes(snapshot));
        assertEquals(60, ProductAttributeHelper.extractAttributes(snapshot).get("max_insured_age"));
    }

    @Test
    void extractAttributes_nullSnapshot() {
        assertNull(ProductAttributeHelper.extractAttributes(null));
    }

    @Test
    void extractAttributes_nonMapValue() {
        Map<String, Object> snapshot = Map.of("attributes", "not-a-map");
        assertNull(ProductAttributeHelper.extractAttributes(snapshot));
    }

    @Test
    void extractCapabilities_fromSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        Map<String, Object> caps = Map.of("shared_amount", true);
        snapshot.put("capabilities", caps);
        assertNotNull(ProductAttributeHelper.extractCapabilities(snapshot));
    }
}
