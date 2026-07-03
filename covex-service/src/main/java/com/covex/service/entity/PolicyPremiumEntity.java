package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 保单缴费计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_policy_premium")
public class PolicyPremiumEntity extends BaseEntity {

    private Long policyId;
    private String premiumPlanCode;
    private Integer paymentFrequency;
    private Integer paymentTerm;
    private Integer paymentTermUnit;
    private BigDecimal periodPremium;
    private Integer totalPeriods;
    private Integer paidPeriods;
    private LocalDate nextDueDate;
    private Integer gracePeriod;
}
