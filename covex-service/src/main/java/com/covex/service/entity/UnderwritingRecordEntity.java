package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核保记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_underwriting_record")
public class UnderwritingRecordEntity extends BaseEntity {

    private Long proposalId;
    private Integer uwType;
    private Integer uwResult;
    private BigDecimal loadingAmount;
    private String exclusionDesc;
    private String uwComment;
    private String uwOperator;
    private LocalDateTime uwAt;
}
