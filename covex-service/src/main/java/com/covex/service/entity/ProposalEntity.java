package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投保单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_proposal", autoResultMap = true)
public class ProposalEntity extends BaseEntity {

    private String proposalNo;
    private Long productId;
    private Long channelId;
    private Long channelUserId;
    private Long applicantId;
    private Long insuredId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object productSnapshot;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object selectedCoverages;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object selectedPremiumPlan;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object healthDeclaration;

    private BigDecimal totalPremium;
    private BigDecimal totalSumInsured;
    private Integer status;
    private LocalDateTime submitAt;
    private String operator;
}
