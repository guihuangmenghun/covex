package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.ProductChangelogEntity;
import com.covex.service.mapper.ProductChangelogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品变更日志服务
 */
@Service
public class ProductChangelogService {

    private static final Logger log = LoggerFactory.getLogger(ProductChangelogService.class);

    private final ProductChangelogMapper changelogMapper;

    public ProductChangelogService(ProductChangelogMapper changelogMapper) {
        this.changelogMapper = changelogMapper;
    }

    /**
     * 记录变更
     */
    public void logChange(Long tenantId, Long productId, Integer changeType,
                          String changeTarget, Long changeTargetId,
                          String fieldName, String oldValue, String newValue,
                          String operator, String remark) {
        ProductChangelogEntity entry = new ProductChangelogEntity();
        entry.setTenantId(tenantId);
        entry.setProductId(productId);
        entry.setChangeType(changeType);
        entry.setChangeTarget(changeTarget);
        entry.setChangeTargetId(changeTargetId);
        entry.setFieldName(fieldName);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        entry.setOperator(operator);
        entry.setOperatedAt(LocalDateTime.now());
        entry.setRemark(remark);
        changelogMapper.insert(entry);
        log.debug("Changelog recorded: product={}, type={}, field={}", productId, changeType, fieldName);
    }

    /**
     * 查询变更历史
     */
    public List<ProductChangelogEntity> listChanges(Long productId) {
        LambdaQueryWrapper<ProductChangelogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductChangelogEntity::getProductId, productId)
               .orderByDesc(ProductChangelogEntity::getOperatedAt);
        return changelogMapper.selectList(wrapper);
    }

    /**
     * 查询指定产品的变更历史（分页）
     */
    public List<ProductChangelogEntity> listChanges(Long productId, Integer changeType) {
        LambdaQueryWrapper<ProductChangelogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductChangelogEntity::getProductId, productId);
        if (changeType != null) {
            wrapper.eq(ProductChangelogEntity::getChangeType, changeType);
        }
        wrapper.orderByDesc(ProductChangelogEntity::getOperatedAt);
        return changelogMapper.selectList(wrapper);
    }
}
