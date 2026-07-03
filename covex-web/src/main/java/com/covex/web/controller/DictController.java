package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.DictEntity;
import com.covex.service.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据字典")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @Operation(summary = "按类型查询字典")
    @GetMapping("/{dictType}")
    public Result<List<DictEntity>> listByType(@PathVariable String dictType) {
        return Result.ok(dictService.listByType(dictType));
    }

    @Operation(summary = "按类型+父编码查询（层级字典）")
    @GetMapping("/{dictType}/children")
    public Result<List<DictEntity>> listChildren(
            @PathVariable String dictType,
            @RequestParam(required = false) String parentCode) {
        return Result.ok(dictService.listByTypeWithChildren(dictType, parentCode));
    }

    @Operation(summary = "查询所有字典（按类型分组）")
    @GetMapping
    public Result<Map<String, List<DictEntity>>> listAll() {
        return Result.ok(dictService.listAllGrouped());
    }

    @Operation(summary = "新增字典项")
    @PostMapping
    public Result<DictEntity> create(@RequestBody DictEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(dictService.create(entity));
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/{id}")
    public Result<DictEntity> update(@PathVariable Long id, @RequestBody DictEntity entity) {
        return Result.ok(dictService.update(id, entity));
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "清空字典缓存")
    @PostMapping("/cache/evict")
    public Result<Void> evictCache() {
        dictService.evictAllCache();
        return Result.ok();
    }
}
