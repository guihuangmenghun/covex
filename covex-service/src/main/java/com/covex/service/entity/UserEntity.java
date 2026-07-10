package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_user")
public class UserEntity extends BaseEntity {

    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
