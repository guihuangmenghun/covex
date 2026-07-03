package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_role")
public class RoleEntity extends BaseEntity {

    private String roleCode;
    private String roleName;
    private String description;
    private Integer isSystem;
}
