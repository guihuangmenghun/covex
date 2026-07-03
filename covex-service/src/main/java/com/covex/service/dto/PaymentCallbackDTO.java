package com.covex.service.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调请求
 */
@Data
public class PaymentCallbackDTO {

    private String paymentNo;
    private String payChannelNo;
    private BigDecimal amount;
}
