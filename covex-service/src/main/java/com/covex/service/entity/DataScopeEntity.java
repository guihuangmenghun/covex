package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_data_scope")
public class DataScopeEntity extends BaseEntity {

    private Long roleId;
    private Integer scopeType;
    private String scopeValue;
}
