package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 条款文档
 */
@Data
@TableName("ins_product_document")
public class ProductDocumentEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long productId;
    private Integer documentType;
    private String documentName;
    private String fileUrl;
    private String version;
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
