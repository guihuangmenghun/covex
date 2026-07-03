package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 渠道商账号
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_channel_user")
public class ChannelUserEntity extends BaseEntity {

    private Long channelId;
    private String username;
    private String passwordHash;
    private String realName;
    private String agentLicenseNo;
    private String phone;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
