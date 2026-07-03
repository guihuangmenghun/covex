package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;

/**
 * 渠道商
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_channel", autoResultMap = true)
public class ChannelEntity extends BaseEntity {

    private String channelCode;
    private String channelName;
    private Integer channelType;
    private String licenseNo;
    private LocalDate licenseExpiry;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String regionCode;
    private Integer status;
    private String contractNo;
    private LocalDate contractStart;
    private LocalDate contractEnd;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attributes;
}
