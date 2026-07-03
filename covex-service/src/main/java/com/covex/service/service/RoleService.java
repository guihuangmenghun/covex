package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.PermissionEntity;
import com.covex.service.entity.RoleEntity;
import com.covex.service.entity.RolePermissionEntity;
import com.covex.service.mapper.PermissionMapper;
import com.covex.service.mapper.RoleMapper;
import com.covex.service.mapper.RolePermissionMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务
 */
@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public RoleService(RoleMapper roleMapper,
                       RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 创建角色
     */
    public RoleEntity createRole(String roleCode, String roleName, String description) {
        if (StringUtils.isBlank(roleCode)) {
            throw new BizException("角色编码不能为空");
        }
        if (StringUtils.isBlank(roleName)) {
            throw new BizException("角色名称不能为空");
        }
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleEntity::getRoleCode, roleCode);
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BizException("角色编码已存在: " + roleCode);
        }

        RoleEntity role = new RoleEntity();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setIsSystem(0);
        role.setTenantId(0L);
        roleMapper.insert(role);
        log.info("Role created: {}", roleCode);
        return role;
    }

    /**
     * 查询所有角色
     */
    public List<RoleEntity> listRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<RoleEntity>().orderByAsc(RoleEntity::getCreatedAt)
        );
    }

    /**
     * 更新角色
     */
    public RoleEntity updateRole(Long id, RoleEntity entity) {
        RoleEntity existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "角色不存在: " + id);
        }
        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        // 系统角色不允许修改编码
        if (existing.getIsSystem() == 1) {
            entity.setRoleCode(null);
        }
        roleMapper.updateById(entity);
        return roleMapper.selectById(id);
    }

    /**
     * 删除角色 — 系统内置角色不可删除
     */
    public void deleteRole(Long id) {
        RoleEntity existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "角色不存在: " + id);
        }
        if (existing.getIsSystem() == 1) {
            throw new BizException("系统内置角色不可删除: " + existing.getRoleName());
        }
        roleMapper.deleteById(id);
        log.info("Role deleted: {}", existing.getRoleCode());
    }

    /**
     * 批量分配权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "角色不存在: " + roleId);
        }

        // 物理删除原有权限关联（避免唯一约束冲突）
        rolePermissionMapper.physicalDeleteByRoleId(roleId);

        // 新增权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                PermissionEntity perm = permissionMapper.selectById(permissionId);
                if (perm == null) {
                    throw new BizException("权限不存在: " + permissionId);
                }
                RolePermissionEntity rp = new RolePermissionEntity();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setTenantId(0L);
                rolePermissionMapper.insert(rp);
            }
        }
        log.info("Permissions assigned to role {}: {}", roleId, permissionIds);
    }

    /**
     * 获取角色权限列表
     */
    public List<PermissionEntity> getRolePermissions(Long roleId) {
        LambdaQueryWrapper<RolePermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermissionEntity::getRoleId, roleId);
        List<RolePermissionEntity> rolePermissions = rolePermissionMapper.selectList(wrapper);

        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toList());

        return permissionMapper.selectBatchIds(permissionIds);
    }
}
