package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.DictEntity;
import com.covex.service.mapper.DictMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DictService {

    private static final Logger log = LoggerFactory.getLogger(DictService.class);
    private static final String CACHE_PREFIX = "dict:";
    private static final long CACHE_TTL_MINUTES = 60;

    private final DictMapper dictMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Local Guava cache as L1
    private final LoadingCache<String, List<DictEntity>> localCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(CacheLoader.from(this::loadFromRedisOrDb));

    public DictService(DictMapper dictMapper, RedisTemplate<String, Object> redisTemplate) {
        this.dictMapper = dictMapper;
        this.redisTemplate = redisTemplate;
    }

    // ========== CRUD ==========

    public DictEntity create(DictEntity entity) {
        // Check duplicate
        LambdaQueryWrapper<DictEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictEntity::getTenantId, entity.getTenantId())
               .eq(DictEntity::getDictType, entity.getDictType())
               .eq(DictEntity::getDictCode, entity.getDictCode());
        if (dictMapper.selectCount(wrapper) > 0) {
            throw new BizException("字典项已存在: " + entity.getDictType() + "/" + entity.getDictCode());
        }
        dictMapper.insert(entity);
        evictCache(entity.getDictType());
        return entity;
    }

    public DictEntity update(Long id, DictEntity entity) {
        DictEntity existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "字典项不存在: " + id);
        }
        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        dictMapper.updateById(entity);
        evictCache(existing.getDictType());
        return dictMapper.selectById(id);
    }

    public void delete(Long id) {
        DictEntity existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "字典项不存在: " + id);
        }
        dictMapper.deleteById(id);
        evictCache(existing.getDictType());
    }

    // ========== Query ==========

    public List<DictEntity> listByType(String dictType) {
        try {
            return localCache.get(dictType);
        } catch (Exception e) {
            log.warn("Cache miss for dict type: {}", dictType);
            return loadFromDb(dictType);
        }
    }

    public List<DictEntity> listByTypeWithChildren(String dictType, String parentCode) {
        List<DictEntity> all = listByType(dictType);
        if (StringUtils.isBlank(parentCode)) {
            return all.stream()
                    .filter(d -> StringUtils.isBlank(d.getParentCode()))
                    .collect(Collectors.toList());
        }
        return all.stream()
                .filter(d -> parentCode.equals(d.getParentCode()))
                .collect(Collectors.toList());
    }

    public Map<String, List<DictEntity>> listAllGrouped() {
        List<DictEntity> all = dictMapper.selectList(
                new LambdaQueryWrapper<DictEntity>()
                        .eq(DictEntity::getIsActive, 1)
                        .orderByAsc(DictEntity::getDictType)
                        .orderByAsc(DictEntity::getSortOrder)
        );
        return all.stream().collect(Collectors.groupingBy(DictEntity::getDictType));
    }

    // ========== Cache ==========

    @SuppressWarnings("unchecked")
    private List<DictEntity> loadFromRedisOrDb(String dictType) {
        String key = CACHE_PREFIX + dictType;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List) {
            return (List<DictEntity>) cached;
        }
        List<DictEntity> list = loadFromDb(dictType);
        redisTemplate.opsForValue().set(key, list, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return list;
    }

    private List<DictEntity> loadFromDb(String dictType) {
        return dictMapper.selectList(
                new LambdaQueryWrapper<DictEntity>()
                        .eq(DictEntity::getDictType, dictType)
                        .eq(DictEntity::getIsActive, 1)
                        .orderByAsc(DictEntity::getSortOrder)
        );
    }

    private void evictCache(String dictType) {
        localCache.invalidate(dictType);
        redisTemplate.delete(CACHE_PREFIX + dictType);
        log.debug("Dict cache evicted: {}", dictType);
    }

    public void evictAllCache() {
        localCache.invalidateAll();
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("All dict cache evicted");
    }
}
