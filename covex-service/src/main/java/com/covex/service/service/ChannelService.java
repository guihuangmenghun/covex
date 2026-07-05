package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.common.util.AesUtil;
import com.covex.service.entity.ChannelEntity;
import com.covex.service.entity.ChannelProductEntity;
import com.covex.service.mapper.ChannelMapper;
import com.covex.service.mapper.ChannelProductMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 渠道商服务
 */
@Service
public class ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelService.class);

    /**
     * 状态机：允许的流转方向
     * 1-待审核 → 2-已签约 → 3-已暂停 → 4-已终止
     * 3-已暂停 → 2-已签约（恢复）
     */
    private static final Map<Integer, Set<Integer>> VALID_TRANSITIONS = Map.of(
            1, Set.of(2),
            2, Set.of(3),
            3, Set.of(2, 4),
            4, Set.of()
    );

    private final ChannelMapper channelMapper;
    private final ChannelProductMapper channelProductMapper;
    private final AesUtil aesUtil;

    public ChannelService(ChannelMapper channelMapper,
                          ChannelProductMapper channelProductMapper,
                          AesUtil aesUtil) {
        this.channelMapper = channelMapper;
        this.channelProductMapper = channelProductMapper;
        this.aesUtil = aesUtil;
    }

    // ========== CRUD ==========

    @Transactional(rollbackFor = Exception.class)
    public ChannelEntity createChannel(ChannelEntity entity) {
        // Generate channel_code
        entity.setChannelCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        entity.setStatus(1); // 待审核

        // Encrypt contact phone
        if (StringUtils.isNotBlank(entity.getContactPhone())) {
            entity.setContactPhone(aesUtil.encrypt(entity.getContactPhone()));
        }

        channelMapper.insert(entity);
        log.info("Channel created: id={}, code={}", entity.getId(), entity.getChannelCode());
        return decryptChannel(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChannelEntity updateChannel(Long id, ChannelEntity entity) {
        ChannelEntity existing = channelMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "渠道商不存在: " + id);
        }

        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setChannelCode(existing.getChannelCode());

        // Re-encrypt contact phone if updated
        if (StringUtils.isNotBlank(entity.getContactPhone())) {
            entity.setContactPhone(aesUtil.encrypt(entity.getContactPhone()));
        }

        channelMapper.updateById(entity);
        return decryptChannel(channelMapper.selectById(id));
    }

    public ChannelEntity getChannelById(Long id) {
        ChannelEntity entity = channelMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "渠道商不存在: " + id);
        }
        return decryptChannel(entity);
    }

    public Page<ChannelEntity> listChannels(Long tenantId, String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<ChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelEntity::getTenantId, tenantId);

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ChannelEntity::getChannelName, keyword)
                              .or()
                              .like(ChannelEntity::getChannelCode, keyword));
        }
        if (status != null) {
            wrapper.eq(ChannelEntity::getStatus, status);
        }

        wrapper.orderByDesc(ChannelEntity::getCreatedAt);
        Page<ChannelEntity> result = channelMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(this::decryptChannel);
        return result;
    }

    // ========== Status Machine ==========

    @Transactional(rollbackFor = Exception.class)
    public ChannelEntity updateStatus(Long id, Integer newStatus) {
        ChannelEntity existing = channelMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "渠道商不存在: " + id);
        }

        Integer currentStatus = existing.getStatus();
        Set<Integer> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BizException("状态流转不合法: " + statusName(currentStatus) + " → " + statusName(newStatus));
        }

        ChannelEntity update = new ChannelEntity();
        update.setId(id);
        update.setStatus(newStatus);
        channelMapper.updateById(update);

        log.info("Channel status changed: id={}, {} → {}", id, currentStatus, newStatus);
        return decryptChannel(channelMapper.selectById(id));
    }

    // ========== Product Authorization ==========

    @Transactional(rollbackFor = Exception.class)
    public ChannelProductEntity authorizeProduct(Long channelId, Long productId,
                                                  java.math.BigDecimal firstYearRate,
                                                  java.math.BigDecimal renewalRate) {
        ChannelEntity channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw new BizException(404, "渠道商不存在: " + channelId);
        }
        if (channel.getStatus() != 2) {
            throw new BizException("渠道商非签约状态，不可授权产品");
        }

        // Check if already authorized
        LambdaQueryWrapper<ChannelProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelProductEntity::getTenantId, channel.getTenantId())
               .eq(ChannelProductEntity::getChannelId, channelId)
               .eq(ChannelProductEntity::getProductId, productId);
        ChannelProductEntity existing = channelProductMapper.selectOne(wrapper);

        if (existing != null) {
            // Update existing authorization
            existing.setFirstYearRate(firstYearRate);
            existing.setRenewalRate(renewalRate);
            existing.setIsActive(1);
            channelProductMapper.updateById(existing);
            log.info("Product authorization updated: channel={}, product={}", channelId, productId);
            return existing;
        }

        ChannelProductEntity cp = new ChannelProductEntity();
        cp.setTenantId(channel.getTenantId());
        cp.setChannelId(channelId);
        cp.setProductId(productId);
        cp.setFirstYearRate(firstYearRate);
        cp.setRenewalRate(renewalRate);
        cp.setIsActive(1);
        channelProductMapper.insert(cp);
        log.info("Product authorized: channel={}, product={}", channelId, productId);
        return cp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void revokeProduct(Long channelId, Long productId) {
        LambdaQueryWrapper<ChannelProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelProductEntity::getChannelId, channelId)
               .eq(ChannelProductEntity::getProductId, productId);
        ChannelProductEntity existing = channelProductMapper.selectOne(wrapper);
        if (existing == null) {
            throw new BizException("产品授权不存在");
        }

        ChannelProductEntity update = new ChannelProductEntity();
        update.setId(existing.getId());
        update.setIsActive(0);
        channelProductMapper.updateById(update);
        log.info("Product authorization revoked: channel={}, product={}", channelId, productId);
    }

    public List<ChannelProductEntity> listAuthorizedProducts(Long channelId) {
        LambdaQueryWrapper<ChannelProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelProductEntity::getChannelId, channelId)
               .eq(ChannelProductEntity::getIsActive, 1)
               .orderByAsc(ChannelProductEntity::getCreatedAt);
        return channelProductMapper.selectList(wrapper);
    }

    // ========== Helper ==========

    private ChannelEntity decryptChannel(ChannelEntity entity) {
        if (entity == null) {
            return null;
        }
        if (StringUtils.isNotBlank(entity.getContactPhone())) {
            entity.setContactPhone(aesUtil.decrypt(entity.getContactPhone()));
        }
        return entity;
    }

    private String statusName(Integer status) {
        if (status == null) return "null";
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已签约";
            case 3 -> "已暂停";
            case 4 -> "已终止";
            default -> "未知(" + status + ")";
        };
    }
}
