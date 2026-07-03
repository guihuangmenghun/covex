package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 责任-缴费关联
 */
@Data
@TableName("ins_coverage_premium_rel")
public class CoveragePremiumRelEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long coverageId;
    private Long premiumId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
