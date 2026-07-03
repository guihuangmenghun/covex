package com.covex.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.covex.service.entity.RolePermissionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionEntity> {

    @Delete("DELETE FROM ins_role_permission WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("roleId") Long roleId);
}
