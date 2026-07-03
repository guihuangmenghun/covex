package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 缴费规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_product_premium", autoResultMap = true)
public class ProductPremiumEntity extends BaseEntity {

    private Long productId;
    private String premiumPlanCode;
    private String premiumPlanName;
    private Integer paymentFrequency;
    private Integer paymentTerm;
    private Integer paymentTermUnit;
    private Integer gracePeriod;
    private Integer roundingMode;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> premiumDetail;
}
