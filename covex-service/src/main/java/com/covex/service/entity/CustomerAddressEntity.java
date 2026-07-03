package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户联系地址
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_customer_address")
public class CustomerAddressEntity extends BaseEntity {

    private Long customerId;
    private Integer addressType;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String postalCode;
    private Integer isDefault;
}
