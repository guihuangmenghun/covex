package com.covex.service.aspect;

import com.covex.service.entity.ProductEntity;
import com.covex.service.mapper.ProductMapper;
import com.covex.service.service.ProductChangelogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 产品变更 AOP — 拦截 updateProduct 方法，比较变更前后差异，自动记录变更日志
 */
@Aspect
@Component
public class ProductChangeAspect {

    private static final Logger log = LoggerFactory.getLogger(ProductChangeAspect.class);

    /**
     * 需要比对的字段列表
     */
    private static final List<String> TRACKED_FIELDS = List.of(
            "productName", "shortName", "productType", "productNature",
            "termType", "mainRiderFlag", "saleChannel", "startDate", "endDate",
            "status", "capabilities", "attributes", "versionStatus"
    );

    private final ProductMapper productMapper;
    private final ProductChangelogService changelogService;

    public ProductChangeAspect(ProductMapper productMapper,
                               ProductChangelogService changelogService) {
        this.productMapper = productMapper;
        this.changelogService = changelogService;
    }

    /**
     * 拦截 ProductService.updateProduct(Long id, ProductEntity entity)
     */
    @Around("execution(* com.covex.service.service.ProductService.updateProduct(..))")
    public Object trackChanges(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length < 2) {
            return joinPoint.proceed();
        }

        Long productId = (Long) args[0];
        ProductEntity newEntity = (ProductEntity) args[1];

        // 获取变更前快照
        ProductEntity before = productMapper.selectById(productId);
        if (before == null) {
            return joinPoint.proceed();
        }

        // 执行实际更新
        Object result = joinPoint.proceed();

        // 获取变更后快照
        ProductEntity after = productMapper.selectById(productId);
        if (after == null) {
            return result;
        }

        // 比较差异并记录日志
        try {
            compareAndLog(before, after, newEntity);
        } catch (Exception e) {
            log.warn("Failed to log product changes for product {}: {}", productId, e.getMessage());
        }

        return result;
    }

    private void compareAndLog(ProductEntity before, ProductEntity after, ProductEntity requested) {
        Long tenantId = before.getTenantId();
        Long productId = before.getId();

        // 检查请求中哪些字段非空（即用户想更新的字段）
        Map<String, FieldUpdate> changes = new LinkedHashMap<>();

        for (String fieldName : TRACKED_FIELDS) {
            try {
                Field field = ProductEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);

                Object requestedValue = field.get(requested);
                if (requestedValue == null) {
                    continue; // 未提交此字段，跳过
                }

                Object beforeValue = field.get(before);
                Object afterValue = field.get(after);

                // 比较值是否变化
                if (!Objects.equals(stringify(beforeValue), stringify(afterValue))) {
                    changes.put(fieldName, new FieldUpdate(
                            stringify(beforeValue), stringify(afterValue)));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.debug("Field comparison skipped for {}: {}", fieldName, e.getMessage());
            }
        }

        // 批量记录变更
        for (Map.Entry<String, FieldUpdate> entry : changes.entrySet()) {
            FieldUpdate update = entry.getValue();
            changelogService.logChange(tenantId, productId, 2,
                    "product", productId, entry.getKey(),
                    update.oldValue, update.newValue, "system", null);
            log.info("Product change: id={}, field={}, {} -> {}", productId,
                    entry.getKey(), update.oldValue, update.newValue);
        }
    }

    private String stringify(Object value) {
        if (value == null) return "null";
        return value.toString();
    }

    private record FieldUpdate(String oldValue, String newValue) {}
}
