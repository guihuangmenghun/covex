package com.covex.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.covex.service.entity.DataScopeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DataScopeMapper extends BaseMapper<DataScopeEntity> {

    @Delete("DELETE FROM ins_data_scope WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("roleId") Long roleId);
}
