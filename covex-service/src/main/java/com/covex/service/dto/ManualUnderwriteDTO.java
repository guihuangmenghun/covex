package com.covex.service.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 人工核保请求
 */
@Data
public class ManualUnderwriteDTO {

    private Integer uwResult;
    private BigDecimal loadingAmount;
    private String exclusionDesc;
    private String comment;
    private String operator;
}
