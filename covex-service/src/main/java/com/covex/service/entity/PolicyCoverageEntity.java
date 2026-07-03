package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 保单险种明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_policy_coverage", autoResultMap = true)
public class PolicyCoverageEntity extends BaseEntity {

    private Long policyId;
    private String coverageCode;
    private String coverageName;
    private BigDecimal sumInsured;
    private BigDecimal premium;
    private BigDecimal deductible;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object coverageDetail;

    private Integer status;

    /** 累计已赔付金额 */
    private BigDecimal cumulativePaid;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
