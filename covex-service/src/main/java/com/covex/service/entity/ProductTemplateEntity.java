package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 产品模板表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ins_product_template", autoResultMap = true)
public class ProductTemplateEntity extends BaseEntity {

    private String templateCode;
    private String templateName;
    private String templateDesc;
    private Integer productType;
    private String icon;
    private Integer sortOrder;
    private Integer isActive;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateData;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> paramSchema;
}
