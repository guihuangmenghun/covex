package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 理赔审核记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_claim_review")
public class ClaimReviewEntity extends BaseEntity {

    private Long claimId;
    private Integer reviewType;
    private Integer reviewResult;
    private BigDecimal approvedAmount;
    private String rejectReason;
    private String reviewComment;
    private String reviewer;
    private LocalDateTime reviewedAt;
}
