package com.covex.service.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.covex.common.util.TenantContext;
import com.covex.service.interceptor.DataPermissionInterceptor;
import com.covex.service.service.PermissionCacheService;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis-Plus 配置 — 多租户 + 数据权限 + 乐观锁 + 分页插件
 * 拦截器顺序：多租户 → 数据权限 → 乐观锁 → 分页
 */
@Configuration
public class MybatisPlusConfig {

    /** 不追加 tenant_id 条件的表 */
    private static final Set<String> IGNORE_TABLES = Set.of(
            "ins_dict",             // 全局字典，tenant_id 恒为 0，不做租户过滤
            "ins_rate_table_row"    // 无 tenant_id 列，通过父表 ins_rate_table 间接隔离
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(PermissionCacheService permissionCacheService) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户拦截器（必须在最前面）
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                return new LongValue(TenantContext.getCurrentTenantId());
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return IGNORE_TABLES.contains(tableName);
            }
        }));

        // 2. 数据权限拦截器（多租户之后）
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(permissionCacheService));

        // 3. 乐观锁拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 4. 分页拦截器（必须在最后）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
