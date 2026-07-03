package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.ProductPremiumEntity;
import com.covex.service.service.ProductPremiumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "缴费规则管理")
@RestController
@RequestMapping("/api/product/{productId}/premium")
public class ProductPremiumController {

    private final ProductPremiumService premiumService;

    public ProductPremiumController(ProductPremiumService premiumService) {
        this.premiumService = premiumService;
    }

    @Operation(summary = "创建缴费计划")
    @PostMapping
    public Result<ProductPremiumEntity> create(
            @PathVariable Long productId,
            @RequestBody ProductPremiumEntity entity) {
        entity.setProductId(productId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(premiumService.create(entity));
    }

    @Operation(summary = "查询产品下所有缴费计划")
    @GetMapping
    public Result<List<ProductPremiumEntity>> list(@PathVariable Long productId) {
        return Result.ok(premiumService.listByProductId(productId));
    }

    @Operation(summary = "查询缴费计划详情")
    @GetMapping("/{premiumId}")
    public Result<ProductPremiumEntity> getById(@PathVariable Long premiumId) {
        return Result.ok(premiumService.getById(premiumId));
    }

    @Operation(summary = "更新缴费计划")
    @PutMapping("/{premiumId}")
    public Result<ProductPremiumEntity> update(
            @PathVariable Long premiumId,
            @RequestBody ProductPremiumEntity entity) {
        return Result.ok(premiumService.update(premiumId, entity));
    }

    @Operation(summary = "删除缴费计划")
    @DeleteMapping("/{premiumId}")
    public Result<Void> delete(@PathVariable Long premiumId) {
        premiumService.delete(premiumId);
        return Result.ok();
    }
}
