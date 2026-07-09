package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.common.util.JwtUtil;
import com.covex.service.entity.*;
import com.covex.service.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PermissionCacheService permissionCacheService;
    private final RedissonClient redissonClient;

    public UserService(UserMapper userMapper,
                       UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper,
                       RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper,
                       JwtUtil jwtUtil,
                       PermissionCacheService permissionCacheService,
                       RedissonClient redissonClient) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.permissionCacheService = permissionCacheService;
        this.redissonClient = redissonClient;
    }

    /**
     * 创建用户
     */
    public UserEntity createUser(String username, String password, String realName,
                                  String phone, String email, Integer userType) {
        if (StringUtils.isBlank(username)) {
            throw new BizException("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new BizException("密码不能为空");
        }
        // 检查用户名唯一性
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BizException("用户名已存在: " + username);
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setUserType(userType != null ? userType : 1);
        user.setStatus(1);
        user.setTenantId(0L);
        userMapper.insert(user);
        log.info("User created: {}", username);
        return user;
    }

    /**
     * 登录 - 验证密码并生成 JWT token
     */
    public String login(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BizException("用户名和密码不能为空");
        }

        UserEntity user = getUserByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BizException("用户已被停用");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BizException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 获取用户角色
        List<String> roleCodes = getUserRoles(user.getId()).stream()
                .map(RoleEntity::getRoleCode)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes, user.getTenantId());
        log.info("User logged in: {}", username);
        return token;
    }

    /**
     * 根据 ID 查询用户
     */
    public UserEntity getUserById(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在: " + id);
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    public UserEntity getUserByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 分页查询用户
     */
    public IPage<UserEntity> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(UserEntity::getUsername, keyword)
                    .or().like(UserEntity::getRealName, keyword)
                    .or().like(UserEntity::getPhone, keyword)
            );
        }
        wrapper.orderByDesc(UserEntity::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 更新用户
     */
    public UserEntity updateUser(Long id, UserEntity entity) {
        UserEntity existing = getUserById(id);
        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        // 不允许通过此方法修改密码
        entity.setPasswordHash(null);
        userMapper.updateById(entity);
        return userMapper.selectById(id);
    }

    /**
     * 切换用户启用/停用状态
     */
    public void toggleUserStatus(Long id) {
        UserEntity user = getUserById(id);
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
        log.info("User status toggled: {} -> {}", user.getUsername(), user.getStatus());
    }

    /**
     * 批量分配角色（Redisson 分布式锁防并发冲突）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        RLock lock = redissonClient.getLock("lock:user:assignRoles:" + userId);
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new BizException("角色分配操作冲突，请稍后重试");
            }
            try {
                doAssignRoles(userId, roleIds);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("角色分配操作被中断");
        }
    }

    private void doAssignRoles(Long userId, List<Long> roleIds) {
        getUserById(userId); // 确保用户存在

        // 物理删除原有角色关联（避免唯一约束冲突）
        userRoleMapper.physicalDeleteByUserId(userId);

        // 新增角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                RoleEntity role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new BizException("角色不存在: " + roleId);
                }
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setTenantId(0L);
                userRoleMapper.insert(userRole);
            }
        }
        log.info("Roles assigned to user {}: {}", userId, roleIds);
        permissionCacheService.evictUserPermissions(userId);
    }

    /**
     * 获取用户角色列表
     */
    public List<RoleEntity> getUserRoles(Long userId) {
        // 使用 QueryWrapper（字符串列名）避免 LambdaQueryWrapper 在 OGNL 求值阶段触发 lambda 解析递归
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserRoleEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserRoleEntity> userRoles = userRoleMapper.selectList(wrapper);

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds);
    }

    /**
     * 获取用户权限列表（通过角色关联查询）
     */
    public List<PermissionEntity> getUserPermissions(Long userId) {
        List<RoleEntity> roles = getUserRoles(userId);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = roles.stream()
                .map(RoleEntity::getId)
                .collect(Collectors.toList());

        // 查询角色权限关联（使用 QueryWrapper 避免 OGNL lambda 解析递归）
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RolePermissionEntity> rpWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        rpWrapper.in("role_id", roleIds);
        List<RolePermissionEntity> rolePermissions = rolePermissionMapper.selectList(rpWrapper);

        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 去重权限 ID
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermissionEntity::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        return permissionMapper.selectBatchIds(permissionIds);
    }
}
