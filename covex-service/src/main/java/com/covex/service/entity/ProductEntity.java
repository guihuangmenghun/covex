package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;

/**
 * 产品主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_product", autoResultMap = true)
public class ProductEntity extends BaseEntity {

    private String productCode;
    private String version;
    private Integer versionStatus;
    private String productName;
    private String shortName;
    private Integer productType;
    private Integer productNature;
    private Integer termType;
    private Integer mainRiderFlag;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object saleChannel;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> capabilities;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attributes;

    private Long parentVersionId;
    private Integer templateSource;
    private Long templateRefId;
}
