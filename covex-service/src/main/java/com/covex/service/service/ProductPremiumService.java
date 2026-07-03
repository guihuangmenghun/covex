package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.CoveragePremiumRelEntity;
import com.covex.service.entity.ProductPremiumEntity;
import com.covex.service.mapper.CoveragePremiumRelMapper;
import com.covex.service.mapper.ProductPremiumMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缴费规则服务
 */
@Service
public class ProductPremiumService {

    private static final Logger log = LoggerFactory.getLogger(ProductPremiumService.class);

    private final ProductPremiumMapper premiumMapper;
    private final CoveragePremiumRelMapper coveragePremiumRelMapper;

    public ProductPremiumService(ProductPremiumMapper premiumMapper,
                                 CoveragePremiumRelMapper coveragePremiumRelMapper) {
        this.premiumMapper = premiumMapper;
        this.coveragePremiumRelMapper = coveragePremiumRelMapper;
    }

    @Transactional
    public ProductPremiumEntity create(ProductPremiumEntity entity) {
        premiumMapper.insert(entity);
        log.info("Premium plan created: id={}, code={}", entity.getId(), entity.getPremiumPlanCode());
        return entity;
    }

    public ProductPremiumEntity getById(Long id) {
        ProductPremiumEntity entity = premiumMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "缴费计划不存在: " + id);
        }
        return entity;
    }

    public List<ProductPremiumEntity> listByProductId(Long productId) {
        LambdaQueryWrapper<ProductPremiumEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductPremiumEntity::getProductId, productId);
        return premiumMapper.selectList(wrapper);
    }

    @Transactional
    public ProductPremiumEntity update(Long id, ProductPremiumEntity entity) {
        ProductPremiumEntity existing = premiumMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "缴费计划不存在: " + id);
        }
        entity.setId(id);
        entity.setProductId(existing.getProductId());
        entity.setTenantId(existing.getTenantId());
        premiumMapper.updateById(entity);
        return premiumMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        ProductPremiumEntity existing = premiumMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "缴费计划不存在: " + id);
        }
        existing.setDeletedAt(LocalDateTime.now());
        premiumMapper.updateById(existing);
        premiumMapper.deleteById(id);

        // 删除关联
        LambdaQueryWrapper<CoveragePremiumRelEntity> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(CoveragePremiumRelEntity::getPremiumId, id);
        coveragePremiumRelMapper.delete(relWrapper);

        log.info("Premium plan deleted: id={}", id);
    }
}
