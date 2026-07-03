package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_user_role")
public class UserRoleEntity extends BaseEntity {

    private Long userId;
    private Long roleId;
}
