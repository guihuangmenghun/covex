package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.PermissionEntity;
import com.covex.service.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "创建权限")
    @PostMapping
    public Result<PermissionEntity> createPermission(@RequestBody PermissionEntity entity) {
        return Result.ok(permissionService.createPermission(
                entity.getPermissionCode(), entity.getPermissionName(),
                entity.getModule(), entity.getAction()));
    }

    @Operation(summary = "查询权限列表")
    @GetMapping
    public Result<List<PermissionEntity>> listPermissions() {
        return Result.ok(permissionService.listPermissions());
    }

    @Operation(summary = "按模块分组查询权限")
    @GetMapping("/modules")
    public Result<Map<String, List<PermissionEntity>>> listByModule() {
        return Result.ok(permissionService.listPermissionsByModule());
    }
}
