package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.entity.*;
import com.covex.service.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final ProductChangelogService changelogService;

    public ProductController(ProductService productService,
                             ProductChangelogService changelogService) {
        this.productService = productService;
        this.changelogService = changelogService;
    }

    @RequiresPermission(code = "product:create")
    @Operation(summary = "创建产品")
    @PostMapping
    public Result<ProductEntity> create(@RequestBody ProductEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(productService.createProduct(entity));
    }

    @Operation(summary = "查询产品详情（含关联的责任/缴费/规则）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        ProductEntity product = productService.getProductById(id);
        List<ProductCoverageEntity> coverages = productService.listCoverages(id);
        List<ProductPremiumEntity> premiums = productService.listPremiums(id);
        List<ProductRuleEntity> rules = productService.listRules(id);

        Map<String, Object> detail = new HashMap<>();
        detail.put("product", product);
        detail.put("coverages", coverages);
        detail.put("premiums", premiums);
        detail.put("rules", rules);
        return Result.ok(detail);
    }

    @Operation(summary = "分页查询产品")
    @GetMapping
    public Result<Page<ProductEntity>> list(
            @RequestParam(defaultValue = "0") Long tenantId,
            @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) Integer versionStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.listProducts(tenantId, productType, versionStatus, keyword, page, size));
    }

    @RequiresPermission(code = "product:update")
    @Operation(summary = "更新产品（仅草稿/审批驳回）")
    @PutMapping("/{id}")
    public Result<ProductEntity> update(@PathVariable Long id, @RequestBody ProductEntity entity) {
        return Result.ok(productService.updateProduct(id, entity));
    }

    @Operation(summary = "克隆产品")
    @PostMapping("/{id}/clone")
    public Result<ProductEntity> clone(@PathVariable Long id) {
        return Result.ok(productService.cloneProduct(id));
    }

    @RequiresPermission(code = "product:publish")
    @Operation(summary = "提交审批")
    @PutMapping("/{id}/publish")
    public Result<ProductEntity> publish(@PathVariable Long id) {
        return Result.ok(productService.publishProduct(id));
    }

    @RequiresPermission(code = "product:approve")
    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    public Result<ProductEntity> approve(@PathVariable Long id) {
        return Result.ok(productService.approveProduct(id));
    }

    @RequiresPermission(code = "product:approve")
    @Operation(summary = "审批驳回")
    @PutMapping("/{id}/reject")
    public Result<ProductEntity> reject(@PathVariable Long id) {
        return Result.ok(productService.rejectProduct(id));
    }

    @Operation(summary = "冻结产品")
    @PutMapping("/{id}/freeze")
    public Result<ProductEntity> freeze(@PathVariable Long id) {
        return Result.ok(productService.freezeProduct(id));
    }

    @Operation(summary = "查询变更历史")
    @GetMapping("/{id}/changelog")
    public Result<List<ProductChangelogEntity>> changelog(@PathVariable Long id) {
        return Result.ok(changelogService.listChanges(id));
    }
}
