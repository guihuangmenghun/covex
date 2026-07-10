package com.covex.web.controller;

import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.dto.FromTemplateDTO;
import com.covex.service.entity.ProductTemplateEntity;
import com.covex.service.service.ProductTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "产品模板")
@RestController
@RequestMapping("/api/product-template")
public class ProductTemplateController {

    private final ProductTemplateService templateService;

    public ProductTemplateController(ProductTemplateService templateService) {
        this.templateService = templateService;
    }

    @Operation(summary = "查询可用模板列表")
    @GetMapping
    public Result<List<ProductTemplateEntity>> list(
            @RequestParam(defaultValue = "0") Long tenantId) {
        return Result.ok(templateService.listActive(tenantId));
    }

    @Operation(summary = "查询模板详情")
    @GetMapping("/{code}")
    public Result<ProductTemplateEntity> detail(@PathVariable String code) {
        return Result.ok(templateService.getByCode(code));
    }

    @RequiresPermission(code = "product:edit")
    @Operation(summary = "从模板创建产品")
    @PostMapping("/create-product")
    public Result<Map<String, Object>> createFromTemplate(@RequestBody FromTemplateDTO dto) {
        return Result.ok(templateService.createFromTemplate(dto));
    }
}
