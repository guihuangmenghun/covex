package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.ProductDocumentEntity;
import com.covex.service.service.ProductDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "条款文档管理")
@RestController
@RequestMapping("/api/product/{productId}/document")
public class ProductDocumentController {

    private final ProductDocumentService documentService;

    public ProductDocumentController(ProductDocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "创建文档")
    @PostMapping
    public Result<ProductDocumentEntity> create(
            @PathVariable Long productId,
            @RequestBody ProductDocumentEntity entity) {
        entity.setProductId(productId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(documentService.create(entity));
    }

    @Operation(summary = "查询产品下所有文档")
    @GetMapping
    public Result<List<ProductDocumentEntity>> list(@PathVariable Long productId) {
        return Result.ok(documentService.listByProductId(productId));
    }

    @Operation(summary = "查询文档详情")
    @GetMapping("/{documentId}")
    public Result<ProductDocumentEntity> getById(@PathVariable Long documentId) {
        return Result.ok(documentService.getById(documentId));
    }

    @Operation(summary = "更新文档")
    @PutMapping("/{documentId}")
    public Result<ProductDocumentEntity> update(
            @PathVariable Long documentId,
            @RequestBody ProductDocumentEntity entity) {
        return Result.ok(documentService.update(documentId, entity));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{documentId}")
    public Result<Void> delete(@PathVariable Long documentId) {
        documentService.delete(documentId);
        return Result.ok();
    }
}
