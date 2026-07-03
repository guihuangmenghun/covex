package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 费率表元数据
 */
@Data
@TableName(value = "ins_rate_table", autoResultMap = true)
public class RateTableEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private String rateTableCode;
    private String rateTableName;
    private Long productId;
    private String version;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> tableSchema;

    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
