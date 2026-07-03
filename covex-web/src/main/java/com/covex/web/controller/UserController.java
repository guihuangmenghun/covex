package com.covex.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.covex.common.result.Result;
import com.covex.service.entity.PermissionEntity;
import com.covex.service.entity.RoleEntity;
import com.covex.service.entity.UserEntity;
import com.covex.service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<UserEntity> createUser(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String realName = (String) body.get("realName");
        String phone = (String) body.get("phone");
        String email = (String) body.get("email");
        Integer userType = body.get("userType") != null ? ((Number) body.get("userType")).intValue() : null;
        UserEntity user = userService.createUser(username, password, realName, phone, email, userType);
        user.setPasswordHash(null); // 不返回密码
        return Result.ok(user);
    }

    @Operation(summary = "用户登录（返回 JWT Token）")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String token = userService.login(username, password);
        return Result.ok(Map.of("token", token));
    }

    @Operation(summary = "根据 ID 查询用户")
    @GetMapping("/{id}")
    public Result<UserEntity> getUser(@PathVariable Long id) {
        UserEntity user = userService.getUserById(id);
        user.setPasswordHash(null);
        return Result.ok(user);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<IPage<UserEntity>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        IPage<UserEntity> result = userService.listUsers(page, size, keyword);
        // 清除密码
        result.getRecords().forEach(u -> u.setPasswordHash(null));
        return Result.ok(result);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<UserEntity> updateUser(@PathVariable Long id, @RequestBody UserEntity entity) {
        UserEntity updated = userService.updateUser(id, entity);
        updated.setPasswordHash(null);
        return Result.ok(updated);
    }

    @Operation(summary = "切换用户启用/停用状态")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return Result.ok();
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.ok();
    }

    @Operation(summary = "查询用户角色")
    @GetMapping("/{id}/roles")
    public Result<List<RoleEntity>> getUserRoles(@PathVariable Long id) {
        return Result.ok(userService.getUserRoles(id));
    }

    @Operation(summary = "查询用户权限")
    @GetMapping("/{id}/permissions")
    public Result<List<PermissionEntity>> getUserPermissions(@PathVariable Long id) {
        return Result.ok(userService.getUserPermissions(id));
    }
}
