package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 保单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_policy", autoResultMap = true)
public class PolicyEntity extends BaseEntity {

    private String policyNo;
    private Long proposalId;
    private Long productId;
    private Long channelId;
    private Long applicantId;
    private Long insuredId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> productSnapshot;

    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private BigDecimal totalPremium;
    private BigDecimal totalSumInsured;
    private Integer paymentMode;
    private Integer status;
    private Integer terminationReason;
    private LocalDateTime terminatedAt;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> beneficiaries;

    // ====== 虚拟字段（不映射数据库列，Service 层填充） ======
    @TableField(exist = false)
    private String applicantName;
    @TableField(exist = false)
    private String insuredName;
}
