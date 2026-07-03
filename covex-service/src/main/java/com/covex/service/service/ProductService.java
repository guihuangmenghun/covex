package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.service.entity.*;
import com.covex.service.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 产品服务 — 核心服务，管理产品全生命周期
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    /**
     * 版本状态机：允许的流转
     * 1-草稿 → 2-待审批
     * 2-待审批 → 3-已发布, 5-审批驳回
     * 3-已发布 → 4-已冻结
     * 5-审批驳回 → 1-草稿（重新编辑）
     */
    private static final Map<Integer, Set<Integer>> VALID_TRANSITIONS = Map.of(
            1, Set.of(2),
            2, Set.of(3, 5),
            3, Set.of(4),
            4, Set.of(),
            5, Set.of(1)
    );

    private final ProductMapper productMapper;
    private final ProductCoverageMapper coverageMapper;
    private final ProductPremiumMapper premiumMapper;
    private final CoveragePremiumRelMapper coveragePremiumRelMapper;
    private final ProductRuleMapper ruleMapper;
    private final ProductDocumentMapper documentMapper;
    private final ProductChangelogService changelogService;

    public ProductService(ProductMapper productMapper,
                          ProductCoverageMapper coverageMapper,
                          ProductPremiumMapper premiumMapper,
                          CoveragePremiumRelMapper coveragePremiumRelMapper,
                          ProductRuleMapper ruleMapper,
                          ProductDocumentMapper documentMapper,
                          ProductChangelogService changelogService) {
        this.productMapper = productMapper;
        this.coverageMapper = coverageMapper;
        this.premiumMapper = premiumMapper;
        this.coveragePremiumRelMapper = coveragePremiumRelMapper;
        this.ruleMapper = ruleMapper;
        this.documentMapper = documentMapper;
        this.changelogService = changelogService;
    }

    // ========== CRUD ==========

    /**
     * 创建产品（status=草稿）
     */
    @Transactional
    public ProductEntity createProduct(ProductEntity entity) {
        entity.setVersionStatus(1); // 草稿
        if (entity.getStatus() == null) {
            entity.setStatus(1); // 未上架
        }
        if (StringUtils.isBlank(entity.getVersion())) {
            entity.setVersion("1.0.0");
        }

        // 检查 product_code + version 唯一性
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductEntity::getTenantId, entity.getTenantId())
               .eq(ProductEntity::getProductCode, entity.getProductCode())
               .eq(ProductEntity::getVersion, entity.getVersion());
        if (productMapper.selectCount(wrapper) > 0) {
            throw new BizException("产品编码+版本已存在: " + entity.getProductCode() + " v" + entity.getVersion());
        }

        productMapper.insert(entity);
        log.info("Product created: id={}, code={}, version={}", entity.getId(), entity.getProductCode(), entity.getVersion());

        // 记录变更日志
        changelogService.logChange(entity.getTenantId(), entity.getId(), 1,
                "product", entity.getId(), null, null, null, "system", "创建产品");

        return entity;
    }

    /**
     * 查询产品详情
     */
    public ProductEntity getProductById(Long id) {
        ProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "产品不存在: " + id);
        }
        return entity;
    }

    /**
     * 分页查询产品列表
     */
    public Page<ProductEntity> listProducts(Long tenantId, Integer productType, Integer versionStatus,
                                            String keyword, int page, int size) {
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductEntity::getTenantId, tenantId);

        if (productType != null) {
            wrapper.eq(ProductEntity::getProductType, productType);
        }
        if (versionStatus != null) {
            wrapper.eq(ProductEntity::getVersionStatus, versionStatus);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ProductEntity::getProductName, keyword)
                              .or()
                              .like(ProductEntity::getProductCode, keyword)
                              .or()
                              .like(ProductEntity::getShortName, keyword));
        }

        wrapper.orderByDesc(ProductEntity::getCreatedAt);
        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 更新产品（仅草稿/审批驳回可修改）
     */
    @Transactional
    public ProductEntity updateProduct(Long id, ProductEntity entity) {
        ProductEntity existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "产品不存在: " + id);
        }
        if (existing.getVersionStatus() != 1 && existing.getVersionStatus() != 5) {
            throw new BizException("只有草稿或审批驳回状态的产品可以修改，当前状态: " + statusName(existing.getVersionStatus()));
        }

        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setProductCode(existing.getProductCode());
        entity.setVersion(existing.getVersion());
        entity.setVersionStatus(existing.getVersionStatus());
        productMapper.updateById(entity);

        log.info("Product updated: id={}", id);
        return productMapper.selectById(id);
    }

    /**
     * 克隆产品（深拷贝所有关联表）
     */
    @Transactional
    public ProductEntity cloneProduct(Long id) {
        ProductEntity source = productMapper.selectById(id);
        if (source == null) {
            throw new BizException(404, "产品不存在: " + id);
        }

        // 克隆产品主表
        ProductEntity cloned = new ProductEntity();
        cloned.setTenantId(source.getTenantId());
        cloned.setProductCode(source.getProductCode());
        cloned.setVersion(incrementVersion(source.getVersion()));
        cloned.setVersionStatus(1); // 草稿
        cloned.setProductName(source.getProductName());
        cloned.setShortName(source.getShortName());
        cloned.setProductType(source.getProductType());
        cloned.setProductNature(source.getProductNature());
        cloned.setTermType(source.getTermType());
        cloned.setMainRiderFlag(source.getMainRiderFlag());
        cloned.setSaleChannel(source.getSaleChannel());
        cloned.setStartDate(source.getStartDate());
        cloned.setEndDate(source.getEndDate());
        cloned.setStatus(1); // 未上架
        cloned.setCapabilities(source.getCapabilities());
        cloned.setAttributes(source.getAttributes());
        cloned.setParentVersionId(source.getId());

        // 检查新版本号唯一性
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductEntity::getTenantId, source.getTenantId())
               .eq(ProductEntity::getProductCode, source.getProductCode())
               .eq(ProductEntity::getVersion, cloned.getVersion());
        if (productMapper.selectCount(wrapper) > 0) {
            throw new BizException("克隆版本号冲突: " + cloned.getVersion());
        }

        productMapper.insert(cloned);
        Long newProductId = cloned.getId();

        // 克隆保障定义 + 缴费规则 + 关联
        cloneCoverages(source.getId(), newProductId, source.getTenantId());

        // 克隆规则
        cloneRules(source.getId(), newProductId, source.getTenantId());

        // 克隆文档
        cloneDocuments(source.getId(), newProductId, source.getTenantId());

        log.info("Product cloned: source={}, new={}, version={}", id, newProductId, cloned.getVersion());

        changelogService.logChange(source.getTenantId(), newProductId, 1,
                "product", newProductId, null, null, null, "system",
                "从产品 " + id + " 克隆");

        return cloned;
    }

    // ========== 状态机 ==========

    /**
     * 发布产品（草稿→待审批→已发布）
     * 简化实现：草稿直接发布（跳过审批）或按状态机流转
     */
    @Transactional
    public ProductEntity publishProduct(Long id) {
        ProductEntity existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "产品不存在: " + id);
        }

        Integer currentStatus = existing.getVersionStatus();

        // 草稿 → 待审批
        if (currentStatus == 1) {
            updateVersionStatus(id, 2);
            log.info("Product submitted for approval: id={}", id);
            changelogService.logChange(existing.getTenantId(), id, 3,
                    "product", id, "version_status", "1", "2", "system", "提交审批");

            // 自动审批通过：待审批 → 已发布
            updateVersionStatus(id, 3);
            log.info("Product published: id={}", id);
            changelogService.logChange(existing.getTenantId(), id, 3,
                    "product", id, "version_status", "2", "3", "system", "审批通过并发布");

            return productMapper.selectById(id);
        }

        // 审批驳回 → 草稿（需要先回到草稿再重新提交）
        if (currentStatus == 5) {
            throw new BizException("审批驳回的产品需要先编辑后重新提交");
        }

        // 尝试状态机流转
        Set<Integer> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(3)) {
            throw new BizException("当前状态不可发布: " + statusName(currentStatus));
        }

        updateVersionStatus(id, 3);
        changelogService.logChange(existing.getTenantId(), id, 3,
                "product", id, "version_status", String.valueOf(currentStatus), "3", "system", "发布");

        return productMapper.selectById(id);
    }

    /**
     * 冻结产品（已发布→已冻结）
     */
    @Transactional
    public ProductEntity freezeProduct(Long id) {
        ProductEntity existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "产品不存在: " + id);
        }

        Integer currentStatus = existing.getVersionStatus();
        Set<Integer> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(4)) {
            throw new BizException("当前状态不可冻结: " + statusName(currentStatus));
        }

        updateVersionStatus(id, 4);
        log.info("Product frozen: id={}", id);

        changelogService.logChange(existing.getTenantId(), id, 4,
                "product", id, "version_status", String.valueOf(currentStatus), "4", "system", "冻结");

        return productMapper.selectById(id);
    }

    /**
     * 查询产品关联的保障列表
     */
    public List<ProductCoverageEntity> listCoverages(Long productId) {
        LambdaQueryWrapper<ProductCoverageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCoverageEntity::getProductId, productId)
               .orderByAsc(ProductCoverageEntity::getSortOrder);
        return coverageMapper.selectList(wrapper);
    }

    /**
     * 查询产品关联的缴费计划列表
     */
    public List<ProductPremiumEntity> listPremiums(Long productId) {
        LambdaQueryWrapper<ProductPremiumEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPremiumEntity::getProductId, productId);
        return premiumMapper.selectList(wrapper);
    }

    /**
     * 查询产品关联的规则列表
     */
    public List<ProductRuleEntity> listRules(Long productId) {
        LambdaQueryWrapper<ProductRuleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRuleEntity::getProductId, productId)
               .orderByAsc(ProductRuleEntity::getSortOrder);
        return ruleMapper.selectList(wrapper);
    }

    // ========== 内部方法 ==========

    private void updateVersionStatus(Long id, Integer newStatus) {
        ProductEntity update = new ProductEntity();
        update.setId(id);
        update.setVersionStatus(newStatus);
        productMapper.updateById(update);
    }

    private void cloneCoverages(Long sourceProductId, Long targetProductId, Long tenantId) {
        List<ProductCoverageEntity> coverages = coverageMapper.selectList(
                new LambdaQueryWrapper<ProductCoverageEntity>()
                        .eq(ProductCoverageEntity::getProductId, sourceProductId));

        List<ProductPremiumEntity> premiums = premiumMapper.selectList(
                new LambdaQueryWrapper<ProductPremiumEntity>()
                        .eq(ProductPremiumEntity::getProductId, sourceProductId));

        // 克隆保障
        for (ProductCoverageEntity cov : coverages) {
            Long oldCovId = cov.getId();
            ProductCoverageEntity newCov = new ProductCoverageEntity();
            newCov.setTenantId(tenantId);
            newCov.setProductId(targetProductId);
            newCov.setCoverageCode(cov.getCoverageCode());
            newCov.setCoverageName(cov.getCoverageName());
            newCov.setSelectionMode(cov.getSelectionMode());
            newCov.setBenefitType(cov.getBenefitType());
            newCov.setCoverageDetail(cov.getCoverageDetail());
            newCov.setSortOrder(cov.getSortOrder());
            coverageMapper.insert(newCov);

            // 克隆该保障的缴费关联
            for (ProductPremiumEntity prem : premiums) {
                LambdaQueryWrapper<CoveragePremiumRelEntity> relWrapper = new LambdaQueryWrapper<>();
                relWrapper.eq(CoveragePremiumRelEntity::getCoverageId, oldCovId)
                          .eq(CoveragePremiumRelEntity::getPremiumId, prem.getId());
                CoveragePremiumRelEntity oldRel = coveragePremiumRelMapper.selectOne(relWrapper);
                if (oldRel != null) {
                    // Find the new premium id by matching plan code
                    ProductPremiumEntity newPrem = premiums.stream()
                            .filter(p -> p.getPremiumPlanCode().equals(prem.getPremiumPlanCode()))
                            .findFirst().orElse(null);
                    // Note: We need to re-query the new premium IDs after insert
                    // For simplicity, we'll handle this in a second pass
                }
            }
        }

        // 克隆缴费计划
        for (ProductPremiumEntity prem : premiums) {
            ProductPremiumEntity newPrem = new ProductPremiumEntity();
            newPrem.setTenantId(tenantId);
            newPrem.setProductId(targetProductId);
            newPrem.setPremiumPlanCode(prem.getPremiumPlanCode());
            newPrem.setPremiumPlanName(prem.getPremiumPlanName());
            newPrem.setPaymentFrequency(prem.getPaymentFrequency());
            newPrem.setPaymentTerm(prem.getPaymentTerm());
            newPrem.setPaymentTermUnit(prem.getPaymentTermUnit());
            newPrem.setGracePeriod(prem.getGracePeriod());
            newPrem.setRoundingMode(prem.getRoundingMode());
            newPrem.setPremiumDetail(prem.getPremiumDetail());
            premiumMapper.insert(newPrem);
        }

        // 克隆关联表（通过 code 匹配新旧 ID）
        List<ProductCoverageEntity> newCoverages = coverageMapper.selectList(
                new LambdaQueryWrapper<ProductCoverageEntity>()
                        .eq(ProductCoverageEntity::getProductId, targetProductId));
        List<ProductPremiumEntity> newPremiums = premiumMapper.selectList(
                new LambdaQueryWrapper<ProductPremiumEntity>()
                        .eq(ProductPremiumEntity::getProductId, targetProductId));

        for (ProductCoverageEntity oldCov : coverages) {
            ProductCoverageEntity newCov = newCoverages.stream()
                    .filter(c -> c.getCoverageCode().equals(oldCov.getCoverageCode()))
                    .findFirst().orElse(null);
            if (newCov == null) continue;

            List<CoveragePremiumRelEntity> oldRels = coveragePremiumRelMapper.selectList(
                    new LambdaQueryWrapper<CoveragePremiumRelEntity>()
                            .eq(CoveragePremiumRelEntity::getCoverageId, oldCov.getId()));

            for (ProductPremiumEntity oldPrem : premiums) {
                boolean hasRel = oldRels.stream()
                        .anyMatch(r -> r.getPremiumId().equals(oldPrem.getId()));
                if (hasRel) {
                    ProductPremiumEntity newPrem = newPremiums.stream()
                            .filter(p -> p.getPremiumPlanCode().equals(oldPrem.getPremiumPlanCode()))
                            .findFirst().orElse(null);
                    if (newPrem != null) {
                        CoveragePremiumRelEntity newRel = new CoveragePremiumRelEntity();
                        newRel.setTenantId(tenantId);
                        newRel.setCoverageId(newCov.getId());
                        newRel.setPremiumId(newPrem.getId());
                        coveragePremiumRelMapper.insert(newRel);
                    }
                }
            }
        }
    }

    private void cloneRules(Long sourceProductId, Long targetProductId, Long tenantId) {
        List<ProductRuleEntity> rules = ruleMapper.selectList(
                new LambdaQueryWrapper<ProductRuleEntity>()
                        .eq(ProductRuleEntity::getProductId, sourceProductId));
        for (ProductRuleEntity rule : rules) {
            ProductRuleEntity newRule = new ProductRuleEntity();
            newRule.setTenantId(tenantId);
            newRule.setProductId(targetProductId);
            // coverageId 映射暂不处理（克隆时按产品级规则）
            newRule.setRuleType(rule.getRuleType());
            newRule.setRuleEngine(rule.getRuleEngine());
            newRule.setRuleCode(rule.getRuleCode());
            newRule.setRuleName(rule.getRuleName());
            newRule.setSortOrder(rule.getSortOrder());
            newRule.setIsActive(rule.getIsActive());
            ruleMapper.insert(newRule);
        }
    }

    private void cloneDocuments(Long sourceProductId, Long targetProductId, Long tenantId) {
        List<ProductDocumentEntity> docs = documentMapper.selectList(
                new LambdaQueryWrapper<ProductDocumentEntity>()
                        .eq(ProductDocumentEntity::getProductId, sourceProductId));
        for (ProductDocumentEntity doc : docs) {
            ProductDocumentEntity newDoc = new ProductDocumentEntity();
            newDoc.setTenantId(tenantId);
            newDoc.setProductId(targetProductId);
            newDoc.setDocumentType(doc.getDocumentType());
            newDoc.setDocumentName(doc.getDocumentName());
            newDoc.setFileUrl(doc.getFileUrl());
            newDoc.setVersion(doc.getVersion());
            newDoc.setEffectiveDate(doc.getEffectiveDate());
            newDoc.setExpiryDate(doc.getExpiryDate());
            documentMapper.insert(newDoc);
        }
    }

    /**
     * 版本号递增：1.0.0 → 1.1.0
     */
    private String incrementVersion(String version) {
        if (StringUtils.isBlank(version)) return "1.0.0";
        String[] parts = version.split("\\.");
        if (parts.length == 3) {
            int minor = Integer.parseInt(parts[1]) + 1;
            return parts[0] + "." + minor + ".0";
        }
        return version + ".1";
    }

    private String statusName(Integer status) {
        if (status == null) return "null";
        return switch (status) {
            case 1 -> "草稿";
            case 2 -> "待审批";
            case 3 -> "已发布";
            case 4 -> "已冻结";
            case 5 -> "审批驳回";
            default -> "未知(" + status + ")";
        };
    }
}
