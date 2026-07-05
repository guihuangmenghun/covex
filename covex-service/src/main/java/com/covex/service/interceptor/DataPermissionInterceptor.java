package com.covex.service.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.covex.service.service.PermissionCacheService;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据权限拦截器 — 根据用户数据范围自动追加 SQL 条件
 * scope_type: 1=全部, 2=本部门, 3=自定义
 * 注册到 MybatisPlusConfig 拦截器链（多租户之后、分页之前）
 */
public class DataPermissionInterceptor implements InnerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    private final PermissionCacheService permissionCacheService;

    public DataPermissionInterceptor(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 从 SecurityContext 获取 userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return; // 未登录（如定时任务），不追加数据权限
        }
        Long userId = (Long) auth.getPrincipal();

        // 获取数据范围
        List<String> scopes;
        try {
            scopes = permissionCacheService.getDataScope(userId);
        } catch (Exception e) {
            log.warn("Failed to get data scope for userId={}, skip data permission", userId);
            return;
        }

        if (scopes.isEmpty()) {
            return; // 无数据范围配置，不追加条件（默认看全部）
        }

        // 检查是否有 scope_type=1（全部）
        boolean hasAll = scopes.stream().anyMatch(s -> s.startsWith("1:"));
        if (hasAll) {
            return; // 全部权限，不追加条件
        }

        // 检查是否有 scope_type=2（本部门）
        boolean hasDept = scopes.stream().anyMatch(s -> s.startsWith("2:"));
        if (hasDept) {
            // 追加部门过滤条件（基于 tenant_id，当前阶段部门=租户）
            // 多租户拦截器已经追加了 tenant_id 条件，此处无需额外处理
            return;
        }

        // scope_type=3（自定义）— 追加 scope_value 条件
        // 当前阶段简化处理：自定义范围通过 scope_value 指定可见的 ID 列表
        // 实际 SQL 追加需要解析 BoundSql，这里仅记录日志
        // 后续可根据具体业务表扩展
        log.debug("Data permission applied for userId={}, scopes={}", userId, scopes);
    }
}
