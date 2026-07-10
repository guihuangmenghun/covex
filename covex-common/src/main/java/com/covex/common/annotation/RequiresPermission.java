package com.covex.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级权限校验注解
 * 标注在 Controller 方法上，AOP 拦截并校验当前用户是否拥有指定权限码
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 权限码，如 "product:edit", "claim:review"
     */
    String code();
}
