package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 理赔赔付记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_claim_payment")
public class ClaimPaymentEntity extends BaseEntity {

    private Long claimId;
    private Long paymentId;
    private Long beneficiaryId;
    private String beneficiaryName;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private String operator;
}
