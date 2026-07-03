package com.covex.service.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 理赔审核请求
 */
@Data
public class ClaimReviewDTO {

    private Integer reviewType;
    private Integer reviewResult;
    private BigDecimal approvedAmount;
    private String rejectReason;
    private String comment;
    private String reviewer;
}
