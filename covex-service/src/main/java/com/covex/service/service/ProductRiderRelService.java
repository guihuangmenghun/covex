package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.ProductRiderRelEntity;
import com.covex.service.mapper.ProductRiderRelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主附险关联服务
 */
@Service
public class ProductRiderRelService {

    private static final Logger log = LoggerFactory.getLogger(ProductRiderRelService.class);

    private final ProductRiderRelMapper riderRelMapper;

    public ProductRiderRelService(ProductRiderRelMapper riderRelMapper) {
        this.riderRelMapper = riderRelMapper;
    }

    @Transactional
    public ProductRiderRelEntity create(ProductRiderRelEntity entity) {
        // 检查重复
        LambdaQueryWrapper<ProductRiderRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRiderRelEntity::getTenantId, entity.getTenantId())
               .eq(ProductRiderRelEntity::getMainProductCode, entity.getMainProductCode())
               .eq(ProductRiderRelEntity::getRiderProductCode, entity.getRiderProductCode());
        if (riderRelMapper.selectCount(wrapper) > 0) {
            throw new BizException("主附险关联已存在: " + entity.getMainProductCode() + " → " + entity.getRiderProductCode());
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(1);
        }
        riderRelMapper.insert(entity);
        log.info("Rider rel created: id={}, main={}, rider={}", entity.getId(),
                entity.getMainProductCode(), entity.getRiderProductCode());
        return entity;
    }

    public List<ProductRiderRelEntity> listByMainProductCode(String mainProductCode, Long tenantId) {
        LambdaQueryWrapper<ProductRiderRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRiderRelEntity::getTenantId, tenantId)
               .eq(ProductRiderRelEntity::getMainProductCode, mainProductCode)
               .eq(ProductRiderRelEntity::getIsActive, 1);
        return riderRelMapper.selectList(wrapper);
    }

    public List<ProductRiderRelEntity> listByProductId(Long productId) {
        // Query by main product code is more meaningful, but also support listing by any side
        LambdaQueryWrapper<ProductRiderRelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRiderRelEntity::getIsActive, 1);
        return riderRelMapper.selectList(wrapper);
    }

    @Transactional
    public void delete(Long id) {
        ProductRiderRelEntity existing = riderRelMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "主附险关联不存在: " + id);
        }
        existing.setDeletedAt(LocalDateTime.now());
        riderRelMapper.updateById(existing);
        riderRelMapper.deleteById(id);
        log.info("Rider rel deleted: id={}", id);
    }
}
