package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.ProductDocumentEntity;
import com.covex.service.mapper.ProductDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 条款文档服务
 */
@Service
public class ProductDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ProductDocumentService.class);

    private final ProductDocumentMapper documentMapper;

    public ProductDocumentService(ProductDocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @Transactional
    public ProductDocumentEntity create(ProductDocumentEntity entity) {
        documentMapper.insert(entity);
        log.info("Document created: id={}, name={}", entity.getId(), entity.getDocumentName());
        return entity;
    }

    public ProductDocumentEntity getById(Long id) {
        ProductDocumentEntity entity = documentMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "文档不存在: " + id);
        }
        return entity;
    }

    public List<ProductDocumentEntity> listByProductId(Long productId) {
        LambdaQueryWrapper<ProductDocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDocumentEntity::getProductId, productId)
               .orderByAsc(ProductDocumentEntity::getDocumentType);
        return documentMapper.selectList(wrapper);
    }

    @Transactional
    public ProductDocumentEntity update(Long id, ProductDocumentEntity entity) {
        ProductDocumentEntity existing = documentMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "文档不存在: " + id);
        }
        entity.setId(id);
        entity.setProductId(existing.getProductId());
        entity.setTenantId(existing.getTenantId());
        documentMapper.updateById(entity);
        return documentMapper.selectById(id);
    }

    @Transactional
    public void delete(Long id) {
        ProductDocumentEntity existing = documentMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "文档不存在: " + id);
        }
        existing.setDeletedAt(LocalDateTime.now());
        documentMapper.updateById(existing);
        documentMapper.deleteById(id);
        log.info("Document deleted: id={}", id);
    }
}
