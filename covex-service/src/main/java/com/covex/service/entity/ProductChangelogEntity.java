package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 变更日志
 */
@Data
@TableName("ins_product_changelog")
public class ProductChangelogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private Long productId;
    private Integer changeType;
    private String changeTarget;
    private Long changeTargetId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String operator;
    private LocalDateTime operatedAt;
    private String remark;
}
