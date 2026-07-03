package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 保障定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_product_coverage", autoResultMap = true)
public class ProductCoverageEntity extends BaseEntity {

    private Long productId;
    private String coverageCode;
    private String coverageName;
    private Integer selectionMode;
    private Integer benefitType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> coverageDetail;

    private Integer sortOrder;
}
