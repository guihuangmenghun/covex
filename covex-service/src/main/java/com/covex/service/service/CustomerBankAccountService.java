package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.covex.common.exception.BizException;
import com.covex.common.util.AesUtil;
import com.covex.service.entity.CustomerBankAccountEntity;
import com.covex.service.entity.CustomerEntity;
import com.covex.service.mapper.CustomerBankAccountMapper;
import com.covex.service.mapper.CustomerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户银行账户服务
 */
@Service
public class CustomerBankAccountService {

    private static final Logger log = LoggerFactory.getLogger(CustomerBankAccountService.class);

    private final CustomerBankAccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final AesUtil aesUtil;

    public CustomerBankAccountService(CustomerBankAccountMapper accountMapper,
                                      CustomerMapper customerMapper,
                                      AesUtil aesUtil) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.aesUtil = aesUtil;
    }

    /**
     * 创建银行账户 — 银行账号加密存储，户名校验（必须与客户姓名一致）
     */
    @Transactional
    public CustomerBankAccountEntity createAccount(Long customerId, CustomerBankAccountEntity entity) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(404, "客户不存在: " + customerId);
        }

        // Account holder name must match customer name
        if (!customer.getCustomerName().equals(entity.getAccountHolder())) {
            throw new BizException("账户户名必须与客户姓名一致");
        }

        entity.setCustomerId(customerId);
        entity.setTenantId(customer.getTenantId());

        // Encrypt account number
        entity.setAccountNo(aesUtil.encrypt(entity.getAccountNo()));
        entity.setIsDefault(entity.getIsDefault() != null ? entity.getIsDefault() : 0);
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);

        accountMapper.insert(entity);
        log.info("Bank account created: id={}, customerId={}", entity.getId(), customerId);

        return decryptAccount(entity);
    }

    /**
     * 查询账户列表（解密返回）
     */
    public List<CustomerBankAccountEntity> listAccounts(Long customerId) {
        LambdaQueryWrapper<CustomerBankAccountEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerBankAccountEntity::getCustomerId, customerId)
               .orderByDesc(CustomerBankAccountEntity::getIsDefault)
               .orderByDesc(CustomerBankAccountEntity::getCreatedAt);
        List<CustomerBankAccountEntity> list = accountMapper.selectList(wrapper);
        list.forEach(this::decryptAccount);
        return list;
    }

    /**
     * 更新账户
     */
    @Transactional
    public CustomerBankAccountEntity updateAccount(Long id, CustomerBankAccountEntity entity) {
        CustomerBankAccountEntity existing = accountMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "银行账户不存在: " + id);
        }

        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setCustomerId(existing.getCustomerId());

        // Re-encrypt account number if updated
        if (entity.getAccountNo() != null) {
            entity.setAccountNo(aesUtil.encrypt(entity.getAccountNo()));
        }

        accountMapper.updateById(entity);
        return decryptAccount(accountMapper.selectById(id));
    }

    /**
     * 删除账户 — 删除保护：有未过期代扣协议（agreement_expiry > now）时禁止删除
     */
    @Transactional
    public void deleteAccount(Long id) {
        CustomerBankAccountEntity existing = accountMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "银行账户不存在: " + id);
        }

        // Deletion protection: active agreement
        if (existing.getAgreementExpiry() != null && existing.getAgreementExpiry().isAfter(LocalDate.now())) {
            throw new BizException("该账户存在未过期的代扣协议（到期日: " + existing.getAgreementExpiry() + "），禁止删除");
        }

        accountMapper.deleteById(id);
        log.info("Bank account deleted: id={}", id);
    }

    /**
     * 设置默认账户 — 同一 usage_type 下只有一个默认
     */
    @Transactional
    public void setDefaultAccount(Long customerId, Long accountId, Integer usageType) {
        // Clear existing default for the same customer + usage type
        LambdaUpdateWrapper<CustomerBankAccountEntity> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(CustomerBankAccountEntity::getCustomerId, customerId)
                    .eq(CustomerBankAccountEntity::getUsageType, usageType)
                    .set(CustomerBankAccountEntity::getIsDefault, 0);
        accountMapper.update(null, clearWrapper);

        // Set new default
        CustomerBankAccountEntity entity = accountMapper.selectById(accountId);
        if (entity == null) {
            throw new BizException(404, "银行账户不存在: " + accountId);
        }
        if (!entity.getCustomerId().equals(customerId)) {
            throw new BizException("账户不属于该客户");
        }
        entity.setIsDefault(1);
        entity.setUsageType(usageType);
        accountMapper.updateById(entity);

        log.info("Default account set: customerId={}, accountId={}, usageType={}", customerId, accountId, usageType);
    }

    private CustomerBankAccountEntity decryptAccount(CustomerBankAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        entity.setAccountNo(aesUtil.decrypt(entity.getAccountNo()));
        return entity;
    }
}
