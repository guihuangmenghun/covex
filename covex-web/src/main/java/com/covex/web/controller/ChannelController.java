package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.entity.ChannelEntity;
import com.covex.service.entity.ChannelProductEntity;
import com.covex.service.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "渠道商管理")
@RestController
@RequestMapping("/api/channel")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequiresPermission(code = "channel:edit")
    @Operation(summary = "创建渠道商")
    @PostMapping
    public Result<ChannelEntity> create(@RequestBody ChannelEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(channelService.createChannel(entity));
    }

    @Operation(summary = "查询渠道商详情")
    @GetMapping("/{id}")
    public Result<ChannelEntity> getById(@PathVariable Long id) {
        return Result.ok(channelService.getChannelById(id));
    }

    @Operation(summary = "分页查询渠道商")
    @GetMapping
    public Result<Page<ChannelEntity>> list(
            @RequestParam(defaultValue = "0") Long tenantId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(channelService.listChannels(tenantId, keyword, status, page, size));
    }

    @RequiresPermission(code = "channel:edit")
    @Operation(summary = "更新渠道商")
    @PutMapping("/{id}")
    public Result<ChannelEntity> update(@PathVariable Long id, @RequestBody ChannelEntity entity) {
        return Result.ok(channelService.updateChannel(id, entity));
    }

    @RequiresPermission(code = "channel:approve")
    @Operation(summary = "更新渠道商状态")
    @PutMapping("/{id}/status")
    public Result<ChannelEntity> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer newStatus = body.get("status");
        if (newStatus == null) {
            return Result.fail("缺少 status 参数");
        }
        return Result.ok(channelService.updateStatus(id, newStatus));
    }

    @RequiresPermission(code = "channel:approve")
    @Operation(summary = "审核通过渠道商")
    @PostMapping("/{id}/approve")
    public Result<ChannelEntity> approve(@PathVariable Long id) {
        return Result.ok(channelService.updateStatus(id, 2));
    }

    @RequiresPermission(code = "channel:approve")
    @Operation(summary = "驳回渠道商")
    @PostMapping("/{id}/reject")
    public Result<ChannelEntity> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return Result.ok(channelService.updateStatus(id, 5));
    }

    @Operation(summary = "授权产品")
    @PostMapping("/{channelId}/authorize")
    public Result<ChannelProductEntity> authorizeProduct(
            @PathVariable Long channelId,
            @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        BigDecimal firstYearRate = new BigDecimal(body.get("firstYearRate").toString());
        BigDecimal renewalRate = new BigDecimal(body.get("renewalRate").toString());
        return Result.ok(channelService.authorizeProduct(channelId, productId, firstYearRate, renewalRate));
    }

    @Operation(summary = "撤销产品授权")
    @DeleteMapping("/{channelId}/authorize/{productId}")
    public Result<Void> revokeProduct(@PathVariable Long channelId, @PathVariable Long productId) {
        channelService.revokeProduct(channelId, productId);
        return Result.ok();
    }

    @Operation(summary = "查询授权产品列表")
    @GetMapping("/{channelId}/products")
    public Result<List<ChannelProductEntity>> listProducts(@PathVariable Long channelId) {
        return Result.ok(channelService.listAuthorizedProducts(channelId));
    }
}
