package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 理赔案件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_claim")
public class ClaimEntity extends BaseEntity {

    private String claimNo;
    private Long policyId;
    private Long coverageId;
    private Long reporterId;
    private Integer reporterRelation;
    private LocalDate accidentDate;
    private String accidentType;
    private String accidentDesc;
    private String accidentLocation;
    private BigDecimal claimAmount;
    private BigDecimal approvedAmount;
    private Integer status;
    private String claimHandler;
    private LocalDateTime reportedAt;
    private LocalDateTime closedAt;

    // ====== 虚拟字段（不映射数据库列，Service 层填充） ======
    @TableField(exist = false)
    private String policyNo;
    @TableField(exist = false)
    private String coverageName;
    @TableField(exist = false)
    private String reporterName;
}
