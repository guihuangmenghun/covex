package com.covex.service.aspect;

import com.covex.common.annotation.RequiresPermission;
import com.covex.common.exception.BizException;
import com.covex.service.service.PermissionCacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验 AOP 切面 — 拦截 @RequiresPermission 注解
 * 从 SecurityContext 获取 userId，通过 PermissionCacheService 校验权限码
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionCacheService permissionCacheService;

    public PermissionAspect(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                  RequiresPermission requiresPermission) throws Throwable {
        String requiredCode = requiresPermission.code();

        // 从 SecurityContext 获取 userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BizException(403, "未认证，无法校验权限");
        }
        Long userId = (Long) auth.getPrincipal();

        // 获取用户权限码集合
        Set<String> userPerms = permissionCacheService.getUserPermissions(userId);

        // 校验权限码
        if (!userPerms.contains(requiredCode)) {
            log.warn("Permission denied: userId={}, required={}, has={}", userId, requiredCode, userPerms);
            throw new BizException(403, "无权限: " + requiredCode);
        }

        return joinPoint.proceed();
    }
}
