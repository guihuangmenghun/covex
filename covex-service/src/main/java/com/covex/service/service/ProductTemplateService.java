package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.dto.FromTemplateDTO;
import com.covex.service.entity.*;
import com.covex.service.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 产品模板服务 — 模板查询 + 从模板创建产品
 */
@Service
public class ProductTemplateService {

    private static final Logger log = LoggerFactory.getLogger(ProductTemplateService.class);

    private final ProductTemplateMapper templateMapper;
    private final ProductMapper productMapper;
    private final ProductCoverageMapper coverageMapper;
    private final ProductPremiumMapper premiumMapper;
    private final CoveragePremiumRelMapper coveragePremiumRelMapper;
    private final ProductRuleMapper ruleMapper;
    private final ProductChangelogService changelogService;

    public ProductTemplateService(ProductTemplateMapper templateMapper,
                                  ProductMapper productMapper,
                                  ProductCoverageMapper coverageMapper,
                                  ProductPremiumMapper premiumMapper,
                                  CoveragePremiumRelMapper coveragePremiumRelMapper,
                                  ProductRuleMapper ruleMapper,
                                  ProductChangelogService changelogService) {
        this.templateMapper = templateMapper;
        this.productMapper = productMapper;
        this.coverageMapper = coverageMapper;
        this.premiumMapper = premiumMapper;
        this.coveragePremiumRelMapper = coveragePremiumRelMapper;
        this.ruleMapper = ruleMapper;
        this.changelogService = changelogService;
    }

    /**
     * 查询可用模板列表（系统模板 tenant_id=0 + 当前租户的公司模板）
     */
    public List<ProductTemplateEntity> listActive(Long tenantId) {
        LambdaQueryWrapper<ProductTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ProductTemplateEntity::getTenantId, 0L)
                          .or()
                          .eq(ProductTemplateEntity::getTenantId, tenantId != null ? tenantId : 0L));
        wrapper.eq(ProductTemplateEntity::getIsActive, 1);
        wrapper.orderByAsc(ProductTemplateEntity::getSortOrder);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 根据模板编码查询详情
     */
    public ProductTemplateEntity getByCode(String templateCode) {
        LambdaQueryWrapper<ProductTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductTemplateEntity::getTemplateCode, templateCode);
        ProductTemplateEntity entity = templateMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException(404, "模板不存在: " + templateCode);
        }
        return entity;
    }

    /**
     * 从模板创建完整产品配置
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createFromTemplate(FromTemplateDTO dto) {
        ProductTemplateEntity template = getByCode(dto.getTemplateCode());
        Map<String, Object> td = (Map<String, Object>) template.getTemplateData();
        if (td == null) {
            throw new BizException("模板数据为空: " + template.getTemplateCode());
        }

        Map<String, Object> params = dto.getParams() != null ? dto.getParams() : Collections.emptyMap();
        Long tenantId = dto.getTenantId() != null ? dto.getTenantId() : 0L;

        // ====== 1. 创建产品主表 ======
        Map<String, Object> productTpl = (Map<String, Object>) td.getOrDefault("product", Collections.emptyMap());
        ProductEntity product = new ProductEntity();
        product.setTenantId(tenantId);
        product.setProductCode(getStringParam(params, "product_code", "TPL_" + System.currentTimeMillis()));
        product.setProductName(getStringParam(params, "product_name", template.getTemplateName()));
        product.setShortName(getStringParam(params, "short_name", ""));
        product.setVersion("1.0.0");
        product.setVersionStatus(1); // 草稿
        product.setStatus(1);
        product.setProductType(template.getProductType());
        product.setProductNature(getIntFromMap(productTpl, "product_nature"));
        product.setTermType(getIntFromMap(productTpl, "term_type"));
        product.setMainRiderFlag(getIntFromMap(productTpl, "main_rider_flag"));
        product.setSaleChannel(productTpl.get("sale_channel"));

        Object capObj = productTpl.get("capabilities");
        product.setCapabilities(capObj instanceof Map ? (Map<String, Object>) capObj : null);

        // 合并 attributes：模板预置 + PM 参数覆盖
        Map<String, Object> attributes = new HashMap<>();
        Object tplAttrs = productTpl.get("attributes");
        if (tplAttrs instanceof Map) {
            attributes.putAll((Map<String, Object>) tplAttrs);
        }
        // PM 填写的核心参数写入 attributes
        putIfPresent(params, attributes, "max_insured_age");
        putIfPresent(params, attributes, "max_maturity_age");
        putIfPresent(params, attributes, "min_sum_insured");
        putIfPresent(params, attributes, "max_sum_insured");
        putIfPresent(params, attributes, "term_options");
        putIfPresent(params, attributes, "waiting_period");
        product.setAttributes(attributes);

        product.setTemplateSource(template.getTenantId() == 0 ? 2 : 3);
        product.setTemplateRefId(template.getId());

        // 检查 product_code + version 唯一性
        LambdaQueryWrapper<ProductEntity> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ProductEntity::getTenantId, tenantId)
                    .eq(ProductEntity::getProductCode, product.getProductCode())
                    .eq(ProductEntity::getVersion, "1.0.0");
        if (productMapper.selectCount(checkWrapper) > 0) {
            throw new BizException("产品编码已存在: " + product.getProductCode());
        }

        productMapper.insert(product);
        Long productId = product.getId();

        // ====== 2. 创建保障定义 ======
        List<Map<String, Object>> covList = (List<Map<String, Object>>) td.getOrDefault("coverages", Collections.emptyList());
        Map<String, Long> covCodeToId = new LinkedHashMap<>();

        int covSort = 0;
        for (Map<String, Object> covTpl : covList) {
            ProductCoverageEntity cov = new ProductCoverageEntity();
            cov.setTenantId(tenantId);
            cov.setProductId(productId);
            cov.setCoverageCode((String) covTpl.get("coverage_code"));
            cov.setCoverageName((String) covTpl.get("coverage_name"));
            cov.setSelectionMode(getIntFromMap(covTpl, "selection_mode"));
            cov.setBenefitType(getIntFromMap(covTpl, "benefit_type"));
            Object covDetail = covTpl.get("coverage_detail");
            cov.setCoverageDetail(covDetail instanceof Map ? (Map<String, Object>) covDetail : null);
            cov.setSortOrder(getIntFromMap(covTpl, "sort_order", covSort++));
            coverageMapper.insert(cov);
            covCodeToId.put(cov.getCoverageCode(), cov.getId());
        }

        // ====== 3. 创建缴费计划 ======
        List<Map<String, Object>> premList = (List<Map<String, Object>>) td.getOrDefault("premium_plans", Collections.emptyList());
        Map<String, Long> premCodeToId = new LinkedHashMap<>();

        // PM 缴费参数覆盖：如果 PM 指定了 payment_term，用 PM 的值
        Object pmPaymentTerm = params.get("payment_term");
        Object pmPaymentFreq = params.get("payment_frequency");

        for (Map<String, Object> premTpl : premList) {
            ProductPremiumEntity prem = new ProductPremiumEntity();
            prem.setTenantId(tenantId);
            prem.setProductId(productId);
            prem.setPremiumPlanCode((String) premTpl.get("premium_plan_code"));
            prem.setPremiumPlanName((String) premTpl.get("premium_plan_name"));
            prem.setPaymentFrequency(pmPaymentFreq != null
                    ? ((Number) pmPaymentFreq).intValue()
                    : getIntFromMap(premTpl, "payment_frequency"));
            prem.setPaymentTerm(pmPaymentTerm != null
                    ? ((Number) pmPaymentTerm).intValue()
                    : getIntFromMap(premTpl, "payment_term"));
            prem.setPaymentTermUnit(getIntFromMap(premTpl, "payment_term_unit"));
            prem.setGracePeriod(getIntFromMap(premTpl, "grace_period", 60));
            prem.setRoundingMode(getIntFromMap(premTpl, "rounding_mode"));
            Object premDetail = premTpl.get("premium_detail");
            prem.setPremiumDetail(premDetail instanceof Map ? (Map<String, Object>) premDetail : null);
            premiumMapper.insert(prem);
            premCodeToId.put(prem.getPremiumPlanCode(), prem.getId());
        }

        // ====== 4. 创建保障-缴费关联 ======
        List<Map<String, Object>> rels = (List<Map<String, Object>>) td.getOrDefault("coverage_premium_rels", Collections.emptyList());
        int relCount = 0;
        for (Map<String, Object> rel : rels) {
            Long covId = covCodeToId.get(rel.get("coverage_code"));
            Long premId = premCodeToId.get(rel.get("premium_plan_code"));
            if (covId != null && premId != null) {
                CoveragePremiumRelEntity relEntity = new CoveragePremiumRelEntity();
                relEntity.setTenantId(tenantId);
                relEntity.setCoverageId(covId);
                relEntity.setPremiumId(premId);
                coveragePremiumRelMapper.insert(relEntity);
                relCount++;
            }
        }

        // ====== 5. 创建规则引用 ======
        List<Map<String, Object>> ruleList = (List<Map<String, Object>>) td.getOrDefault("rules", Collections.emptyList());
        for (Map<String, Object> ruleTpl : ruleList) {
            ProductRuleEntity rule = new ProductRuleEntity();
            rule.setTenantId(tenantId);
            rule.setProductId(productId);
            rule.setRuleType(getIntFromMap(ruleTpl, "rule_type"));
            rule.setRuleEngine((String) ruleTpl.get("rule_engine"));
            rule.setRuleCode((String) ruleTpl.get("rule_code"));
            rule.setRuleName((String) ruleTpl.get("rule_name"));
            rule.setSortOrder(getIntFromMap(ruleTpl, "sort_order"));
            rule.setIsActive(getIntFromMap(ruleTpl, "is_active", 1));
            ruleMapper.insert(rule);
        }

        // ====== 6. 记录变更日志 ======
        changelogService.logChange(tenantId, productId, 1,
                "product", productId, null, null, null, "system",
                "从模板 [" + template.getTemplateCode() + "] 创建产品");

        log.info("Product created from template: productId={}, template={}, coverages={}, premiums={}, rules={}, rels={}",
                productId, template.getTemplateCode(), covList.size(), premList.size(), ruleList.size(), relCount);

        // ====== 返回摘要 ======
        Map<String, Object> summary = new HashMap<>();
        summary.put("productId", productId);
        summary.put("productCode", product.getProductCode());
        summary.put("productName", product.getProductName());
        summary.put("versionStatus", product.getVersionStatus());
        summary.put("coverageCount", covList.size());
        summary.put("premiumPlanCount", premList.size());
        summary.put("ruleCount", ruleList.size());
        summary.put("relCount", relCount);
        return summary;
    }

    // ========== 工具方法 ==========

    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private Integer getIntFromMap(Map<String, Object> map, String key) {
        return getIntFromMap(map, key, null);
    }

    private Integer getIntFromMap(Map<String, Object> map, String key, Integer defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    private void putIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        Object val = source.get(key);
        if (val != null) {
            target.put(key, val);
        }
    }
}
