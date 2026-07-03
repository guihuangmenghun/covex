package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.PermissionEntity;
import com.covex.service.entity.RoleEntity;
import com.covex.service.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleEntity> createRole(@RequestBody RoleEntity entity) {
        return Result.ok(roleService.createRole(
                entity.getRoleCode(), entity.getRoleName(), entity.getDescription()));
    }

    @Operation(summary = "查询角色列表")
    @GetMapping
    public Result<List<RoleEntity>> listRoles() {
        return Result.ok(roleService.listRoles());
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<RoleEntity> updateRole(@PathVariable Long id, @RequestBody RoleEntity entity) {
        return Result.ok(roleService.updateRole(id, entity));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @Operation(summary = "分配权限")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.ok();
    }

    @Operation(summary = "查询角色权限")
    @GetMapping("/{id}/permissions")
    public Result<List<PermissionEntity>> getRolePermissions(@PathVariable Long id) {
        return Result.ok(roleService.getRolePermissions(id));
    }
}
