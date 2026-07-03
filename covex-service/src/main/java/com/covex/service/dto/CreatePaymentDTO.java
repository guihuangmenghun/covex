package com.covex.service.dto;

import lombok.Data;

/**
 * 创建支付请求
 */
@Data
public class CreatePaymentDTO {

    private Long proposalId;
    private Integer payChannel;
}
