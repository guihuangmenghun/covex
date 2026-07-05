package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.DataScopeEntity;
import com.covex.service.entity.RoleEntity;
import com.covex.service.mapper.DataScopeMapper;
import com.covex.service.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据范围管理服务
 */
@Service
public class DataScopeService {

    private static final Logger log = LoggerFactory.getLogger(DataScopeService.class);

    private final DataScopeMapper dataScopeMapper;
    private final RoleMapper roleMapper;
    private final PermissionCacheService permissionCacheService;

    public DataScopeService(DataScopeMapper dataScopeMapper, RoleMapper roleMapper,
                            PermissionCacheService permissionCacheService) {
        this.dataScopeMapper = dataScopeMapper;
        this.roleMapper = roleMapper;
        this.permissionCacheService = permissionCacheService;
    }

    /**
     * 设置角色数据范围（先删后增）
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDataScopes(Long roleId, List<DataScopeEntity> scopes) {
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "角色不存在: " + roleId);
        }

        // 物理删除原有数据范围
        dataScopeMapper.physicalDeleteByRoleId(roleId);

        // 新增数据范围
        if (scopes != null && !scopes.isEmpty()) {
            for (DataScopeEntity scope : scopes) {
                scope.setRoleId(roleId);
                scope.setTenantId(0L);
                dataScopeMapper.insert(scope);
            }
        }
        log.info("Data scopes set for role {}: {} entries", roleId, scopes != null ? scopes.size() : 0);
        permissionCacheService.evictAllPermissions();
    }

    /**
     * 获取角色数据范围
     */
    public List<DataScopeEntity> getDataScopes(Long roleId) {
        LambdaQueryWrapper<DataScopeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataScopeEntity::getRoleId, roleId);
        return dataScopeMapper.selectList(wrapper);
    }
}
