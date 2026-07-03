package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 客户银行账户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_customer_bank_account")
public class CustomerBankAccountEntity extends BaseEntity {

    private Long customerId;
    private String accountHolder;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String accountNo;
    private Integer accountType;
    private Integer usageType;
    private Integer isDefault;
    private String agreementNo;
    private LocalDate agreementExpiry;
    private Integer status;
}
