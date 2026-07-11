package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.common.util.AesUtil;
import com.covex.service.entity.CustomerApplicantEntity;
import com.covex.service.entity.CustomerEntity;
import com.covex.service.entity.CustomerInsuredEntity;
import com.covex.service.mapper.CustomerApplicantMapper;
import com.covex.service.mapper.CustomerInsuredMapper;
import com.covex.service.mapper.CustomerMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户服务
 */
@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerMapper customerMapper;
    private final CustomerApplicantMapper applicantMapper;
    private final CustomerInsuredMapper insuredMapper;
    private final AesUtil aesUtil;

    public CustomerService(CustomerMapper customerMapper,
                           CustomerApplicantMapper applicantMapper,
                           CustomerInsuredMapper insuredMapper,
                           AesUtil aesUtil) {
        this.customerMapper = customerMapper;
        this.applicantMapper = applicantMapper;
        this.insuredMapper = insuredMapper;
        this.aesUtil = aesUtil;
    }

    /**
     * 创建客户 — 证件号查重（用加密后的值比对）
     */
    @Transactional
    public CustomerEntity createCustomer(CustomerEntity entity) {
        // Encrypt sensitive fields
        String encryptedIdNo = aesUtil.encrypt(entity.getIdNo());
        String encryptedPhone = entity.getPhone() != null ? aesUtil.encrypt(entity.getPhone()) : null;

        // Check duplicate by encrypted id_type + id_no
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerEntity::getTenantId, entity.getTenantId())
               .eq(CustomerEntity::getIdType, entity.getIdType())
               .eq(CustomerEntity::getIdNo, encryptedIdNo);
        if (customerMapper.selectCount(wrapper) > 0) {
            throw new BizException("客户已存在（相同证件类型+证件号码）");
        }

        // Generate customer_code (UUID first 8 chars)
        entity.setCustomerCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        entity.setIdNo(encryptedIdNo);
        entity.setPhone(encryptedPhone);

        // Initialize role_flags if not provided
        if (entity.getRoleFlags() == null) {
            Map<String, Object> flags = new HashMap<>();
            flags.put("applicant", false);
            flags.put("insured", false);
            flags.put("beneficiary", false);
            entity.setRoleFlags(flags);
        }

        customerMapper.insert(entity);
        log.info("Customer created: id={}, code={}", entity.getId(), entity.getCustomerCode());

        // Decrypt before returning
        return decryptCustomer(entity);
    }

    /**
     * 按 ID 查询客户（解密返回）
     */
    public CustomerEntity getCustomerById(Long id) {
        CustomerEntity entity = customerMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "客户不存在: " + id);
        }
        return decryptCustomer(entity);
    }

    /**
     * 按证件号查询（加密后查询）
     */
    public CustomerEntity getCustomerByIdNo(Long tenantId, Integer idType, String idNo) {
        String encryptedIdNo = aesUtil.encrypt(idNo);
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerEntity::getTenantId, tenantId)
               .eq(CustomerEntity::getIdType, idType)
               .eq(CustomerEntity::getIdNo, encryptedIdNo);
        CustomerEntity entity = customerMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException(404, "客户不存在");
        }
        return decryptCustomer(entity);
    }

    /**
     * 分页查询 — 按姓名/手机号模糊搜索
     */
    public Page<CustomerEntity> listCustomers(Long tenantId, String keyword, int page, int size) {
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerEntity::getTenantId, tenantId);

        if (StringUtils.isNotBlank(keyword)) {
            // Try matching by name or encrypted phone
            String encryptedKeyword = aesUtil.encrypt(keyword);
            wrapper.and(w -> w.like(CustomerEntity::getCustomerName, keyword)
                              .or()
                              .eq(CustomerEntity::getPhone, encryptedKeyword));
        }

        wrapper.orderByDesc(CustomerEntity::getCreatedAt);
        Page<CustomerEntity> result = customerMapper.selectPage(new Page<>(page, size), wrapper);

        // Decrypt all records
        result.getRecords().forEach(this::decryptCustomer);
        return result;
    }

    /**
     * 更新客户
     */
    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity entity) {
        CustomerEntity existing = customerMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "客户不存在: " + id);
        }

        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setCustomerCode(existing.getCustomerCode());

        // Re-encrypt sensitive fields if updated
        if (entity.getIdNo() != null) {
            entity.setIdNo(aesUtil.encrypt(entity.getIdNo()));
        }
        if (entity.getPhone() != null) {
            entity.setPhone(aesUtil.encrypt(entity.getPhone()));
        }

        customerMapper.updateById(entity);
        return decryptCustomer(customerMapper.selectById(id));
    }

    /**
     * 确保投保人扩展存在 — 如果 role_flags 没有 applicant，创建 applicant 记录 + 更新 role_flags
     */
    @Transactional
    public void ensureApplicant(Long customerId) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(404, "客户不存在: " + customerId);
        }

        // Check if applicant already exists
        LambdaQueryWrapper<CustomerApplicantEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerApplicantEntity::getTenantId, customer.getTenantId())
               .eq(CustomerApplicantEntity::getCustomerId, customerId);
        if (applicantMapper.selectCount(wrapper) > 0) {
            log.info("Applicant extension already exists for customer: {}", customerId);
            return;
        }

        // Create applicant record
        CustomerApplicantEntity applicant = new CustomerApplicantEntity();
        applicant.setTenantId(customer.getTenantId());
        applicant.setCustomerId(customerId);
        applicant.setHasSocialSecurity(0);
        applicant.setHasOtherInsurance(0);
        applicantMapper.insert(applicant);

        // Update role_flags
        Map<String, Object> flags = customer.getRoleFlags();
        if (flags == null) {
            flags = new HashMap<>();
        }
        flags.put("applicant", true);
        CustomerEntity update = new CustomerEntity();
        update.setId(customerId);
        update.setRoleFlags(flags);
        customerMapper.updateById(update);

        log.info("Applicant extension created for customer: {}", customerId);
    }

    /**
     * 确保被保人扩展存在 — 如果 role_flags 没有 insured，创建 insured 记录 + 更新 role_flags
     */
    @Transactional
    public void ensureInsured(Long customerId) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(404, "客户不存在: " + customerId);
        }

        // Check if insured already exists
        LambdaQueryWrapper<CustomerInsuredEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInsuredEntity::getTenantId, customer.getTenantId())
               .eq(CustomerInsuredEntity::getCustomerId, customerId);
        if (insuredMapper.selectCount(wrapper) > 0) {
            log.info("Insured extension already exists for customer: {}", customerId);
            return;
        }

        // Create insured record
        CustomerInsuredEntity insured = new CustomerInsuredEntity();
        insured.setTenantId(customer.getTenantId());
        insured.setCustomerId(customerId);
        insured.setSmokingStatus(0);
        insured.setDrinkingStatus(0);
        insured.setLastHealthUpdate(LocalDateTime.now());
        insuredMapper.insert(insured);

        // Update role_flags
        Map<String, Object> flags = customer.getRoleFlags();
        if (flags == null) {
            flags = new HashMap<>();
        }
        flags.put("insured", true);
        CustomerEntity update = new CustomerEntity();
        update.setId(customerId);
        update.setRoleFlags(flags);
        customerMapper.updateById(update);

        log.info("Insured extension created for customer: {}", customerId);
    }

    /**
     * 查询健康档案（从 ins_customer_insured 表读取）
     */
    public CustomerInsuredEntity getHealthRecord(Long customerId) {
        LambdaQueryWrapper<CustomerInsuredEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInsuredEntity::getTenantId, 0L)
               .eq(CustomerInsuredEntity::getCustomerId, customerId)
               .last("LIMIT 1");
        return insuredMapper.selectOne(wrapper);
    }

    /**
     * 更新健康档案（更新 ins_customer_insured 的健康相关字段）
     */
    @Transactional
    public void updateHealthRecord(Long customerId, CustomerInsuredEntity healthData) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(404, "客户不存在: " + customerId);
        }

        LambdaQueryWrapper<CustomerInsuredEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInsuredEntity::getTenantId, customer.getTenantId())
               .eq(CustomerInsuredEntity::getCustomerId, customerId);
        CustomerInsuredEntity insured = insuredMapper.selectOne(wrapper);
        if (insured == null) {
            throw new BizException("被保人扩展不存在，请先调用 ensure-insured");
        }

        healthData.setId(insured.getId());
        healthData.setTenantId(insured.getTenantId());
        healthData.setCustomerId(insured.getCustomerId());
        healthData.setLastHealthUpdate(LocalDateTime.now());
        insuredMapper.updateById(healthData);

        log.info("Health record updated for customer: {}", customerId);
    }

    /**
     * 解密客户敏感字段（就地修改并返回）
     */
    private CustomerEntity decryptCustomer(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        entity.setIdNo(aesUtil.decrypt(entity.getIdNo()));
        if (entity.getPhone() != null) {
            entity.setPhone(aesUtil.decrypt(entity.getPhone()));
        }
        return entity;
    }
}
