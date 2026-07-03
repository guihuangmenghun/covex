package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_commission")
public class CommissionEntity extends BaseEntity {

    private Long channelId;
    private Long channelUserId;
    private Long policyId;
    private Integer commissionType;
    private BigDecimal premiumAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private String settleMonth;
    private Integer settleStatus;
    private LocalDateTime settledAt;
    private String commissionNo;
    private String operator;
}
