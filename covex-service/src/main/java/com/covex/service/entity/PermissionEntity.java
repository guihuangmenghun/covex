package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_permission")
public class PermissionEntity extends BaseEntity {

    private String permissionCode;
    private String permissionName;
    private String module;
    private String action;
}
