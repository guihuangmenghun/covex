package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.ProductRuleEntity;
import com.covex.service.mapper.ProductRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则引用服务
 */
@Service
public class ProductRuleService {

    private static final Logger log = LoggerFactory.getLogger(ProductRuleService.class);

    private final ProductRuleMapper ruleMapper;

    public ProductRuleService(ProductRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Transactional
    public ProductRuleEntity create(ProductRuleEntity entity) {
        if (entity.getIsActive() == null) {
            entity.setIsActive(1);
        }
        ruleMapper.insert(entity);
        log.info("Rule created: id={}, engine={}, code={}", entity.getId(), entity.getRuleEngine(), entity.getRuleCode());
        return entity;
    }

    public ProductRuleEntity getById(Long id) {
        ProductRuleEntity entity = ruleMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "规则不存在: " + id);
        }
        return entity;
    }

    public List<ProductRuleEntity> listByProductId(Long productId) {
        LambdaQueryWrapper<ProductRuleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRuleEntity::getProductId, productId)
               .orderByAsc(ProductRuleEntity::getSortOrder);
        return ruleMapper.selectList(wrapper);
    }

    @Transactional
    public ProductRuleEntity update(Long id, ProductRuleEntity entity) {
        ProductRuleEntity existing = ruleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "规则不存在: " + id);
        }
        entity.setId(id);
        entity.setProductId(existing.getProductId());
        entity.setTenantId(existing.getTenantId());
        ruleMapper.updateById(entity);
        return ruleMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        ProductRuleEntity existing = ruleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "规则不存在: " + id);
        }
        existing.setDeletedAt(LocalDateTime.now());
        ruleMapper.updateById(existing);
        ruleMapper.deleteById(id);
        log.info("Rule deleted: id={}", id);
    }
}
