package com.covex.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户上下文（ThreadLocal）— 供 MybatisPlus TenantLineInnerInterceptor 获取当前租户 ID
 */
public class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getCurrentTenantId() {
        Long id = CURRENT_TENANT.get();
        if (id == null) {
            log.warn("TenantContext not set, defaulting to tenant_id=0. Thread={}", Thread.currentThread().getName());
            return 0L;
        }
        return id;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
