package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.RateTableEntity;
import com.covex.service.entity.RateTableRowEntity;
import com.covex.service.service.RateTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "费率表管理")
@RestController
@RequestMapping("/api/rate-table")
public class RateTableController {

    private final RateTableService rateTableService;

    public RateTableController(RateTableService rateTableService) {
        this.rateTableService = rateTableService;
    }

    @Operation(summary = "创建费率表")
    @PostMapping
    public Result<RateTableEntity> create(@RequestBody RateTableEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(rateTableService.createRateTable(entity));
    }

    @Operation(summary = "查询费率表详情")
    @GetMapping("/{id}")
    public Result<RateTableEntity> getById(@PathVariable Long id) {
        return Result.ok(rateTableService.getById(id));
    }

    @Operation(summary = "按产品查询费率表列表")
    @GetMapping
    public Result<List<RateTableEntity>> listByProduct(
            @RequestParam(required = false) Long productId) {
        return Result.ok(rateTableService.listByProductId(productId));
    }

    @Operation(summary = "查询费率表行数据")
    @GetMapping("/{id}/rows")
    public Result<List<RateTableRowEntity>> listRows(@PathVariable Long id) {
        return Result.ok(rateTableService.listRows(id));
    }

    @Operation(summary = "批量导入费率表行数据")
    @PostMapping("/{id}/import")
    public Result<Map<String, Object>> importRows(
            @PathVariable Long id,
            @RequestBody List<RateTableRowEntity> rows) {
        rateTableService.importRows(id, rows);
        return Result.ok(Map.of("imported", rows.size()));
    }

    @Operation(summary = "查询费率（先查 Redis，miss 查 DB）")
    @GetMapping("/query")
    public Result<Map<String, Object>> queryRate(
            @RequestParam String tableCode,
            @RequestParam String version,
            @RequestParam String dimensionKey) {
        BigDecimal rate = rateTableService.queryRate(tableCode, version, dimensionKey);
        return Result.ok(Map.of(
                "tableCode", tableCode,
                "version", version,
                "dimensionKey", dimensionKey,
                "rateValue", rate
        ));
    }

    @Operation(summary = "加载费率表到 Redis")
    @PostMapping("/load")
    public Result<Map<String, Object>> loadToRedis(
            @RequestParam String tableCode,
            @RequestParam String version) {
        int count = rateTableService.loadToRedis(tableCode, version);
        return Result.ok(Map.of("loaded", count, "tableCode", tableCode, "version", version));
    }

    @Operation(summary = "清除费率表 Redis 缓存")
    @PostMapping("/evict")
    public Result<Map<String, Object>> evictFromRedis(
            @RequestParam String tableCode,
            @RequestParam String version) {
        rateTableService.evictFromRedis(tableCode, version);
        return Result.ok(Map.of("evicted", true, "tableCode", tableCode, "version", version));
    }
}
