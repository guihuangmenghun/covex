package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.ProductRiderRelEntity;
import com.covex.service.service.ProductRiderRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "主附险关联管理")
@RestController
@RequestMapping("/api/product/{productId}/rider")
public class ProductRiderRelController {

    private final ProductRiderRelService riderRelService;

    public ProductRiderRelController(ProductRiderRelService riderRelService) {
        this.riderRelService = riderRelService;
    }

    @Operation(summary = "创建主附险关联")
    @PostMapping
    public Result<ProductRiderRelEntity> create(
            @PathVariable Long productId,
            @RequestBody ProductRiderRelEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(riderRelService.create(entity));
    }

    @Operation(summary = "查询主险下的附加险关联")
    @GetMapping
    public Result<List<ProductRiderRelEntity>> list(
            @PathVariable Long productId,
            @RequestParam String mainProductCode,
            @RequestParam(defaultValue = "0") Long tenantId) {
        return Result.ok(riderRelService.listByMainProductCode(mainProductCode, tenantId));
    }

    @Operation(summary = "删除主附险关联")
    @DeleteMapping("/{relId}")
    public Result<Void> delete(@PathVariable Long relId) {
        riderRelService.delete(relId);
        return Result.ok();
    }
}
