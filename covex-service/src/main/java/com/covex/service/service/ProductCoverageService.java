package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.CoveragePremiumRelEntity;
import com.covex.service.entity.ProductCoverageEntity;
import com.covex.service.mapper.CoveragePremiumRelMapper;
import com.covex.service.mapper.ProductCoverageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 保障定义服务
 */
@Service
public class ProductCoverageService {

    private static final Logger log = LoggerFactory.getLogger(ProductCoverageService.class);

    private final ProductCoverageMapper coverageMapper;
    private final CoveragePremiumRelMapper coveragePremiumRelMapper;

    public ProductCoverageService(ProductCoverageMapper coverageMapper,
                                  CoveragePremiumRelMapper coveragePremiumRelMapper) {
        this.coverageMapper = coverageMapper;
        this.coveragePremiumRelMapper = coveragePremiumRelMapper;
    }

    @Transactional
    public ProductCoverageEntity create(ProductCoverageEntity entity) {
        coverageMapper.insert(entity);
        log.info("Coverage created: id={}, code={}", entity.getId(), entity.getCoverageCode());
        return entity;
    }

    public ProductCoverageEntity getById(Long id) {
        ProductCoverageEntity entity = coverageMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "保障不存在: " + id);
        }
        return entity;
    }

    public List<ProductCoverageEntity> listByProductId(Long productId) {
        LambdaQueryWrapper<ProductCoverageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCoverageEntity::getProductId, productId)
               .orderByAsc(ProductCoverageEntity::getSortOrder);
        return coverageMapper.selectList(wrapper);
    }

    @Transactional
    public ProductCoverageEntity update(Long id, ProductCoverageEntity entity) {
        ProductCoverageEntity existing = coverageMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "保障不存在: " + id);
        }
        entity.setId(id);
        entity.setProductId(existing.getProductId());
        entity.setTenantId(existing.getTenantId());
        coverageMapper.updateById(entity);
        return coverageMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        ProductCoverageEntity existing = coverageMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "保障不存在: " + id);
        }
        existing.setDeletedAt(LocalDateTime.now());
        coverageMapper.updateById(existing);
        coverageMapper.deleteById(id);

        // 删除关联
        LambdaQueryWrapper<CoveragePremiumRelEntity> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(CoveragePremiumRelEntity::getCoverageId, id);
        coveragePremiumRelMapper.delete(relWrapper);

        log.info("Coverage deleted: id={}", id);
    }

    // ========== 关联缴费计划 ==========

    @Transactional
    public CoveragePremiumRelEntity linkPremium(Long coverageId, Long premiumId, Long tenantId) {
        // 检查重复
        LambdaQueryWrapper<CoveragePremiumRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoveragePremiumRelEntity::getTenantId, tenantId)
               .eq(CoveragePremiumRelEntity::getCoverageId, coverageId)
               .eq(CoveragePremiumRelEntity::getPremiumId, premiumId);
        if (coveragePremiumRelMapper.selectCount(wrapper) > 0) {
            throw new BizException("保障-缴费关联已存在");
        }

        CoveragePremiumRelEntity rel = new CoveragePremiumRelEntity();
        rel.setTenantId(tenantId);
        rel.setCoverageId(coverageId);
        rel.setPremiumId(premiumId);
        coveragePremiumRelMapper.insert(rel);
        log.info("Coverage-Premium linked: coverage={}, premium={}", coverageId, premiumId);
        return rel;
    }

    @Transactional
    public void unlinkPremium(Long coverageId, Long premiumId) {
        LambdaQueryWrapper<CoveragePremiumRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoveragePremiumRelEntity::getCoverageId, coverageId)
               .eq(CoveragePremiumRelEntity::getPremiumId, premiumId);
        int deleted = coveragePremiumRelMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BizException("关联不存在");
        }
        log.info("Coverage-Premium unlinked: coverage={}, premium={}", coverageId, premiumId);
    }

    public List<CoveragePremiumRelEntity> listPremiumRels(Long coverageId) {
        LambdaQueryWrapper<CoveragePremiumRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoveragePremiumRelEntity::getCoverageId, coverageId);
        return coveragePremiumRelMapper.selectList(wrapper);
    }
}
