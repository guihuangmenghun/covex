package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_dict")
public class DictEntity extends BaseEntity {

    private String dictType;
    private String dictCode;
    private String dictName;
    private String parentCode;
    private Integer sortOrder;
    private Integer isActive;
    private String remark;
}
