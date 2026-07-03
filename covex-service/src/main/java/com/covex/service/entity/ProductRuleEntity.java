package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则引用
 */
@Data
@TableName("ins_product_rule")
public class ProductRuleEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long productId;
    private Long coverageId;
    private Integer ruleType;
    private String ruleEngine;
    private String ruleCode;
    private String ruleName;
    private Integer sortOrder;
    private Integer isActive;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
