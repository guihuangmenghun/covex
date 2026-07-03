package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 被保人扩展 + 健康档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_customer_insured", autoResultMap = true)
public class CustomerInsuredEntity extends BaseEntity {

    private Long customerId;
    private String occupation;
    private String occupationCode;
    private Integer occupationRiskLevel;
    private Integer smokingStatus;
    private Integer drinkingStatus;
    private BigDecimal bmi;
    private String bloodType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> medicalHistory;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> familyHistory;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> currentMedications;

    private LocalDateTime lastHealthUpdate;
}
