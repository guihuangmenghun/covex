package com.covex.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建投保单请求
 */
@Data
public class CreateProposalDTO {

    private Long tenantId;
    private Long productId;
    private Long channelId;
    private Long channelUserId;
    private Long applicantId;
    private Long insuredId;
    private List<Map<String, Object>> selectedCoverages;
    private Map<String, Object> selectedPremiumPlan;
    private List<Map<String, Object>> healthDeclaration;
    private BigDecimal totalSumInsured;
}
