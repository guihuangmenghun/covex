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
 * 客户主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_customer", autoResultMap = true)
public class CustomerEntity extends BaseEntity {

    private String customerCode;
    private String customerName;
    private Integer idType;
    private String idNo;
    private LocalDate idExpiry;
    private Integer gender;
    private LocalDate birthDate;
    private String nationality;
    private String phone;
    private String email;
    private Integer customerType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> roleFlags;

    private Integer source;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attributes;
}
