package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> productSnapshot;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> selectedCoverages;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> selectedPremiumPlan;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> healthDeclaration;

    private BigDecimal totalPremium;
    private BigDecimal totalSumInsured;
    private Integer status;
    private LocalDateTime submitAt;
    private String operator;

    // ====== 虚拟字段（不映射数据库列，Service 层填充） ======
    @TableField(exist = false)
    private String applicantName;
    @TableField(exist = false)
    private String insuredName;
    @TableField(exist = false)
    private String channelName;
}
