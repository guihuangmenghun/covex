package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 投保人扩展
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_customer_applicant")
public class CustomerApplicantEntity extends BaseEntity {

    private Long customerId;
    private BigDecimal annualIncome;
    private String incomeSource;
    private Integer educationLevel;
    private Integer maritalStatus;
    private Integer hasSocialSecurity;
    private Integer hasOtherInsurance;
    private String otherInsuranceDesc;
}
