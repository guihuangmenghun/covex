package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 费率表行数据
 */
@Data
@TableName(value = "ins_rate_table_row", autoResultMap = true)
public class RateTableRowEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long rateTableId;
    private String dimensionKey;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dimensionJson;

    private BigDecimal rateValue;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraValues;
}
