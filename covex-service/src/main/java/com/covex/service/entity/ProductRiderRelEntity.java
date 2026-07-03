package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 主附险关联
 */
@Data
@TableName("ins_product_rider_rel")
public class ProductRiderRelEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private String mainProductCode;
    private String riderProductCode;
    private Integer maxRiderCount;
    private Integer isActive;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
