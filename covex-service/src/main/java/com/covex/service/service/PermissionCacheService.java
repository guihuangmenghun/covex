package com.covex.service.service;

import com.covex.service.entity.DataScopeEntity;
import com.covex.service.entity.PermissionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务 — Redis 缓存用户权限和数据范围，避免每次 API 请求都查 DB
 * Key 格式：auth:perms:{userId}, auth:scope:{userId}
 * TTL：30 分钟
 */
@Service
public class PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);
    private static final String PERMS_KEY_PREFIX = "auth:perms:";
    private static final String SCOPE_KEY_PREFIX = "auth:scope:";
    private static final long TTL_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final DataScopeService dataScopeService;

    public PermissionCacheService(StringRedisTemplate redisTemplate,
                                  @Lazy UserService userService,
                                  @Lazy DataScopeService dataScopeService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.dataScopeService = dataScopeService;
    }

    /**
     * 获取用户权限码集合（先查 Redis，未命中查 DB 并缓存）
     */
    public Set<String> getUserPermissions(Long userId) {
        String key = PERMS_KEY_PREFIX + userId;

        // 先查 Redis
        Set<String> cached = redisTemplate.opsForSet().members(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 未命中 → 查 DB
        List<PermissionEntity> permissions = userService.getUserPermissions(userId);
        Set<String> permCodes = permissions.stream()
                .map(PermissionEntity::getPermissionCode)
                .collect(Collectors.toSet());

        // 写 Redis
        if (!permCodes.isEmpty()) {
            redisTemplate.opsForSet().add(key, permCodes.toArray(new String[0]));
            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        }

        log.debug("User permissions loaded from DB and cached: userId={}, count={}", userId, permCodes.size());
        return permCodes;
    }

    /**
     * 获取用户数据范围（先查 Redis，未命中查 DB 并缓存）
     * 返回格式：scopeType:scopeValue 的列表
     */
    public List<String> getDataScope(Long userId) {
        String key = SCOPE_KEY_PREFIX + userId;

        // 先查 Redis
        Set<String> cached = redisTemplate.opsForSet().members(key);
        if (cached != null && !cached.isEmpty()) {
            return List.copyOf(cached);
        }

        // 未命中 → 查 DB（通过用户角色获取数据范围）
        List<Long> roleIds = userService.getUserRoles(userId).stream()
                .map(role -> role.getId())
                .collect(Collectors.toList());

        List<String> scopeEntries = new java.util.ArrayList<>();
        for (Long roleId : roleIds) {
            List<DataScopeEntity> scopes = dataScopeService.getDataScopes(roleId);
            for (DataScopeEntity scope : scopes) {
                scopeEntries.add(scope.getScopeType() + ":" + scope.getScopeValue());
            }
        }

        // 写 Redis
        if (!scopeEntries.isEmpty()) {
            redisTemplate.opsForSet().add(key, scopeEntries.toArray(new String[0]));
            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        }

        log.debug("User data scope loaded from DB and cached: userId={}, count={}", userId, scopeEntries.size());
        return scopeEntries;
    }

    /**
     * 清除单个用户的权限缓存
     */
    public void evictUserPermissions(Long userId) {
        redisTemplate.delete(PERMS_KEY_PREFIX + userId);
        redisTemplate.delete(SCOPE_KEY_PREFIX + userId);
        log.info("User permission cache evicted: userId={}", userId);
    }

    /**
     * 清除所有用户的权限缓存（角色权限变更时调用）
     */
    public void evictAllPermissions() {
        Set<String> permKeys = redisTemplate.keys(PERMS_KEY_PREFIX + "*");
        Set<String> scopeKeys = redisTemplate.keys(SCOPE_KEY_PREFIX + "*");
        if (permKeys != null && !permKeys.isEmpty()) {
            redisTemplate.delete(permKeys);
        }
        if (scopeKeys != null && !scopeKeys.isEmpty()) {
            redisTemplate.delete(scopeKeys);
        }
        log.info("All permission caches evicted");
    }
}
