package com.covex.web.filter;

import com.covex.service.interceptor.DataPermissionInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求级 ThreadLocal 清理过滤器
 * 确保每次 HTTP 请求结束后，DataPermissionInterceptor 中的 SCOPE_CACHE
 * 被彻底清理，防止线程池复用导致的数据串扰和内存泄漏。
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataPermissionCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            DataPermissionInterceptor.cleanup();
        }
    }
}
