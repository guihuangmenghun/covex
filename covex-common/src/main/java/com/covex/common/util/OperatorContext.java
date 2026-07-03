package com.covex.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 操作人上下文工具类
 * 从 Spring Security 上下文中获取当前登录用户作为操作人；
 * 无登录态（MQ 消费、定时任务、支付回调等）时返回 "SYSTEM"。
 */
public class OperatorContext {

    private static final String SYSTEM_OPERATOR = "SYSTEM";

    public static String getCurrentOperator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            // JwtAuthenticationFilter stores username in details
            Object details = auth.getDetails();
            if (details instanceof String s && !s.isEmpty()) {
                return s;
            }
            return auth.getName();
        }
        return SYSTEM_OPERATOR;
    }
}
