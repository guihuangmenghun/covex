package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.common.util.AesUtil;
import com.covex.service.entity.ChannelUserEntity;
import com.covex.service.mapper.ChannelMapper;
import com.covex.service.mapper.ChannelUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 渠道商账号服务
 */
@Service
public class ChannelUserService {

    private static final Logger log = LoggerFactory.getLogger(ChannelUserService.class);
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final ChannelUserMapper channelUserMapper;
    private final ChannelMapper channelMapper;
    private final AesUtil aesUtil;

    public ChannelUserService(ChannelUserMapper channelUserMapper,
                              ChannelMapper channelMapper,
                              AesUtil aesUtil) {
        this.channelUserMapper = channelUserMapper;
        this.channelMapper = channelMapper;
        this.aesUtil = aesUtil;
    }

    @Transactional
    public ChannelUserEntity createUser(ChannelUserEntity entity) {
        // Validate channel exists
        if (channelMapper.selectById(entity.getChannelId()) == null) {
            throw new BizException(404, "渠道商不存在: " + entity.getChannelId());
        }

        // Check username uniqueness
        LambdaQueryWrapper<ChannelUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelUserEntity::getTenantId, entity.getTenantId())
               .eq(ChannelUserEntity::getUsername, entity.getUsername());
        if (channelUserMapper.selectCount(wrapper) > 0) {
            throw new BizException("用户名已存在: " + entity.getUsername());
        }

        // BCrypt password
        entity.setPasswordHash(PASSWORD_ENCODER.encode(entity.getPasswordHash()));
        entity.setStatus(1); // 正常

        // Encrypt phone
        if (StringUtils.isNotBlank(entity.getPhone())) {
            entity.setPhone(aesUtil.encrypt(entity.getPhone()));
        }

        channelUserMapper.insert(entity);
        log.info("Channel user created: id={}, username={}", entity.getId(), entity.getUsername());
        return decryptUser(entity);
    }

    @Transactional
    public ChannelUserEntity updateUser(Long id, ChannelUserEntity entity) {
        ChannelUserEntity existing = channelUserMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "渠道商账号不存在: " + id);
        }

        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setChannelId(existing.getChannelId());
        entity.setUsername(existing.getUsername());
        // Do not allow password update through this method
        entity.setPasswordHash(null);

        // Re-encrypt phone if updated
        if (StringUtils.isNotBlank(entity.getPhone())) {
            entity.setPhone(aesUtil.encrypt(entity.getPhone()));
        }

        channelUserMapper.updateById(entity);
        return decryptUser(channelUserMapper.selectById(id));
    }

    public List<ChannelUserEntity> listByChannel(Long channelId) {
        LambdaQueryWrapper<ChannelUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelUserEntity::getChannelId, channelId)
               .orderByDesc(ChannelUserEntity::getCreatedAt);
        List<ChannelUserEntity> list = channelUserMapper.selectList(wrapper);
        list.forEach(this::decryptUser);
        return list;
    }

    @Transactional
    public ChannelUserEntity toggleStatus(Long id, Integer newStatus) {
        ChannelUserEntity existing = channelUserMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "渠道商账号不存在: " + id);
        }

        if (newStatus < 1 || newStatus > 3) {
            throw new BizException("无效的状态值: " + newStatus);
        }

        ChannelUserEntity update = new ChannelUserEntity();
        update.setId(id);
        update.setStatus(newStatus);
        channelUserMapper.updateById(update);

        log.info("Channel user status changed: id={}, {} → {}", id, existing.getStatus(), newStatus);
        return decryptUser(channelUserMapper.selectById(id));
    }

    private ChannelUserEntity decryptUser(ChannelUserEntity entity) {
        if (entity == null) {
            return null;
        }
        // Never return password hash
        entity.setPasswordHash(null);
        if (StringUtils.isNotBlank(entity.getPhone())) {
            entity.setPhone(aesUtil.decrypt(entity.getPhone()));
        }
        return entity;
    }
}
