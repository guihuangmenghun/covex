package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_payment")
public class PaymentEntity extends BaseEntity {

    private String paymentNo;
    private Long policyId;
    private Long proposalId;
    private Integer paymentType;
    private BigDecimal amount;
    private Integer payChannel;
    private String payChannelNo;
    private Integer status;
    private LocalDateTime paidAt;
    private String operator;
}
