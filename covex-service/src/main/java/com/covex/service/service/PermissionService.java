package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.PermissionEntity;
import com.covex.service.mapper.PermissionMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理服务
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 创建权限
     */
    public PermissionEntity createPermission(String permissionCode, String permissionName,
                                              String module, String action) {
        if (StringUtils.isBlank(permissionCode)) {
            throw new BizException("权限编码不能为空");
        }
        if (StringUtils.isBlank(permissionName)) {
            throw new BizException("权限名称不能为空");
        }
        LambdaQueryWrapper<PermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionEntity::getPermissionCode, permissionCode);
        if (permissionMapper.selectCount(wrapper) > 0) {
            throw new BizException("权限编码已存在: " + permissionCode);
        }

        PermissionEntity perm = new PermissionEntity();
        perm.setPermissionCode(permissionCode);
        perm.setPermissionName(permissionName);
        perm.setModule(module);
        perm.setAction(action);
        perm.setTenantId(0L);
        permissionMapper.insert(perm);
        log.info("Permission created: {}", permissionCode);
        return perm;
    }

    /**
     * 查询所有权限
     */
    public List<PermissionEntity> listPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<PermissionEntity>()
                        .orderByAsc(PermissionEntity::getModule)
                        .orderByAsc(PermissionEntity::getPermissionCode)
        );
    }

    /**
     * 按 module 分组返回权限
     */
    public Map<String, List<PermissionEntity>> listPermissionsByModule() {
        List<PermissionEntity> all = listPermissions();
        return all.stream().collect(Collectors.groupingBy(PermissionEntity::getModule));
    }
}
