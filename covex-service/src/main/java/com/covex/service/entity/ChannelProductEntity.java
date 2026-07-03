package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 渠道-产品授权
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_channel_product")
public class ChannelProductEntity extends BaseEntity {

    private Long channelId;
    private Long productId;
    private BigDecimal firstYearRate;
    private BigDecimal renewalRate;
    private String saleRegion;
    private Integer isActive;
}
