package com.covex.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报案请求
 */
@Data
public class ReportClaimDTO {

    private Long policyId;
    private Long coverageId;
    private Long reporterId;
    private Integer reporterRelation;
    private LocalDate accidentDate;
    private String accidentType;
    private String accidentDesc;
    private String accidentLocation;
    private BigDecimal claimAmount;
}
