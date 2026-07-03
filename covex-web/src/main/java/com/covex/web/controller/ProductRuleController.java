package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.ProductRuleEntity;
import com.covex.service.service.ProductRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "规则引用管理")
@RestController
@RequestMapping("/api/product/{productId}/rule")
public class ProductRuleController {

    private final ProductRuleService ruleService;

    public ProductRuleController(ProductRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Operation(summary = "创建规则引用")
    @PostMapping
    public Result<ProductRuleEntity> create(
            @PathVariable Long productId,
            @RequestBody ProductRuleEntity entity) {
        entity.setProductId(productId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(ruleService.create(entity));
    }

    @Operation(summary = "查询产品下所有规则")
    @GetMapping
    public Result<List<ProductRuleEntity>> list(@PathVariable Long productId) {
        return Result.ok(ruleService.listByProductId(productId));
    }

    @Operation(summary = "查询规则详情")
    @GetMapping("/{ruleId}")
    public Result<ProductRuleEntity> getById(@PathVariable Long ruleId) {
        return Result.ok(ruleService.getById(ruleId));
    }

    @Operation(summary = "更新规则")
    @PutMapping("/{ruleId}")
    public Result<ProductRuleEntity> update(
            @PathVariable Long ruleId,
            @RequestBody ProductRuleEntity entity) {
        return Result.ok(ruleService.update(ruleId, entity));
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/{ruleId}")
    public Result<Void> delete(@PathVariable Long ruleId) {
        ruleService.delete(ruleId);
        return Result.ok();
    }
}
