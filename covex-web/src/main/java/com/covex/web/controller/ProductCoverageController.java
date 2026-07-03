package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.CoveragePremiumRelEntity;
import com.covex.service.entity.ProductCoverageEntity;
import com.covex.service.service.ProductCoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "保障定义管理")
@RestController
@RequestMapping("/api/product/{productId}/coverage")
public class ProductCoverageController {

    private final ProductCoverageService coverageService;

    public ProductCoverageController(ProductCoverageService coverageService) {
        this.coverageService = coverageService;
    }

    @Operation(summary = "创建保障")
    @PostMapping
    public Result<ProductCoverageEntity> create(
            @PathVariable Long productId,
            @RequestBody ProductCoverageEntity entity) {
        entity.setProductId(productId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(coverageService.create(entity));
    }

    @Operation(summary = "查询产品下所有保障")
    @GetMapping
    public Result<List<ProductCoverageEntity>> list(@PathVariable Long productId) {
        return Result.ok(coverageService.listByProductId(productId));
    }

    @Operation(summary = "查询保障详情")
    @GetMapping("/{coverageId}")
    public Result<ProductCoverageEntity> getById(@PathVariable Long coverageId) {
        return Result.ok(coverageService.getById(coverageId));
    }

    @Operation(summary = "更新保障")
    @PutMapping("/{coverageId}")
    public Result<ProductCoverageEntity> update(
            @PathVariable Long coverageId,
            @RequestBody ProductCoverageEntity entity) {
        return Result.ok(coverageService.update(coverageId, entity));
    }

    @Operation(summary = "删除保障")
    @DeleteMapping("/{coverageId}")
    public Result<Void> delete(@PathVariable Long coverageId) {
        coverageService.delete(coverageId);
        return Result.ok();
    }

    @Operation(summary = "关联缴费计划")
    @PostMapping("/{coverageId}/link-premium")
    public Result<CoveragePremiumRelEntity> linkPremium(
            @PathVariable Long coverageId,
            @RequestBody Map<String, Object> body) {
        Long premiumId = Long.valueOf(body.get("premiumId").toString());
        Long tenantId = body.containsKey("tenantId") ? Long.valueOf(body.get("tenantId").toString()) : 0L;
        return Result.ok(coverageService.linkPremium(coverageId, premiumId, tenantId));
    }

    @Operation(summary = "取消关联缴费计划")
    @DeleteMapping("/{coverageId}/unlink-premium/{premiumId}")
    public Result<Void> unlinkPremium(
            @PathVariable Long coverageId,
            @PathVariable Long premiumId) {
        coverageService.unlinkPremium(coverageId, premiumId);
        return Result.ok();
    }

    @Operation(summary = "查询保障关联的缴费计划")
    @GetMapping("/{coverageId}/premiums")
    public Result<List<CoveragePremiumRelEntity>> listPremiumRels(@PathVariable Long coverageId) {
        return Result.ok(coverageService.listPremiumRels(coverageId));
    }
}
