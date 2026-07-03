package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.ChannelUserEntity;
import com.covex.service.service.ChannelUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "渠道商账号管理")
@RestController
@RequestMapping("/api/channel/{channelId}/user")
public class ChannelUserController {

    private final ChannelUserService channelUserService;

    public ChannelUserController(ChannelUserService channelUserService) {
        this.channelUserService = channelUserService;
    }

    @Operation(summary = "创建渠道商账号")
    @PostMapping
    public Result<ChannelUserEntity> create(@PathVariable Long channelId, @RequestBody ChannelUserEntity entity) {
        entity.setChannelId(channelId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(channelUserService.createUser(entity));
    }

    @Operation(summary = "查询渠道商下的账号列表")
    @GetMapping
    public Result<List<ChannelUserEntity>> list(@PathVariable Long channelId) {
        return Result.ok(channelUserService.listByChannel(channelId));
    }

    @Operation(summary = "更新渠道商账号")
    @PutMapping("/{id}")
    public Result<ChannelUserEntity> update(@PathVariable Long channelId, @PathVariable Long id,
                                            @RequestBody ChannelUserEntity entity) {
        return Result.ok(channelUserService.updateUser(id, entity));
    }

    @Operation(summary = "切换账号状态")
    @PutMapping("/{id}/status")
    public Result<ChannelUserEntity> toggleStatus(@PathVariable Long channelId, @PathVariable Long id,
                                                  @RequestBody Map<String, Integer> body) {
        Integer newStatus = body.get("status");
        if (newStatus == null) {
            return Result.fail("缺少 status 参数");
        }
        return Result.ok(channelUserService.toggleStatus(id, newStatus));
    }
}
