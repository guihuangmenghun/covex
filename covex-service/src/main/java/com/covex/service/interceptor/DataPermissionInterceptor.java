package com.covex.service.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.covex.service.service.PermissionCacheService;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
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

    /** 防止递归：拦截器内部查询不再触发数据权限 */
    private static final ThreadLocal<Boolean> SKIP = ThreadLocal.withInitial(() -> false);

    private final PermissionCacheService permissionCacheService;

    public DataPermissionInterceptor(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 防重入：如果当前线程正在查询数据范围，跳过
        if (SKIP.get()) {
            return;
        }

        // 从 SecurityContext 获取 userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return; // 未登录（如定时任务），不追加数据权限
        }
        Long userId = (Long) auth.getPrincipal();

        // 获取数据范围（标记跳过，防止递归）
        List<String> scopes;
        try {
            SKIP.set(true);
            scopes = permissionCacheService.getDataScope(userId);
        } catch (Exception e) {
            log.warn("Failed to get data scope for userId={}, skip data permission", userId);
            return;
        } finally {
            SKIP.set(false);
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
            // 追加 created_by 过滤条件（当前阶段部门=租户，只看自己创建的数据）
            String username = auth.getName();
            String originalSql = boundSql.getSql();
            if (!originalSql.contains("created_by")) {
                String newSql = originalSql + " AND created_by = '" + username + "'";
                try {
                    java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
                    sqlField.setAccessible(true);
                    sqlField.set(boundSql, newSql);
                    log.debug("Data permission (scope_type=2) applied: created_by='{}'", username);
                } catch (Exception e) {
                    log.warn("Failed to modify SQL for data permission: {}", e.getMessage());
                }
            }
            return;
        }

        // scope_type=3（自定义）— 追加 scope_value 条件
        // 当前阶段简化处理：自定义范围通过 scope_value 指定可见的 ID 列表
        // 实际 SQL 追加需要解析 BoundSql，这里仅记录日志
        // 后续可根据具体业务表扩展
        log.debug("Data permission applied for userId={}, scopes={}", userId, scopes);
    }
}
