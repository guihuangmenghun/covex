package com.covex.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.covex.service.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    @Delete("DELETE FROM ins_user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(@Param("userId") Long userId);
}
