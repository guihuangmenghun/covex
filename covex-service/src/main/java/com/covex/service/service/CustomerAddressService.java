package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.CustomerAddressEntity;
import com.covex.service.entity.CustomerEntity;
import com.covex.service.mapper.CustomerAddressMapper;
import com.covex.service.mapper.CustomerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系地址服务
 */
@Service
public class CustomerAddressService {

    private static final Logger log = LoggerFactory.getLogger(CustomerAddressService.class);

    private final CustomerAddressMapper addressMapper;
    private final CustomerMapper customerMapper;

    public CustomerAddressService(CustomerAddressMapper addressMapper,
                                  CustomerMapper customerMapper) {
        this.addressMapper = addressMapper;
        this.customerMapper = customerMapper;
    }

    /**
     * 创建地址
     */
    @Transactional
    public CustomerAddressEntity createAddress(Long customerId, CustomerAddressEntity entity) {
        CustomerEntity customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(404, "客户不存在: " + customerId);
        }

        entity.setCustomerId(customerId);
        entity.setTenantId(customer.getTenantId());

        // Check duplicate address type
        LambdaQueryWrapper<CustomerAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressEntity::getTenantId, customer.getTenantId())
               .eq(CustomerAddressEntity::getCustomerId, customerId)
               .eq(CustomerAddressEntity::getAddressType, entity.getAddressType());
        if (addressMapper.selectCount(wrapper) > 0) {
            throw new BizException("该客户已有相同类型的地址");
        }

        entity.setIsDefault(entity.getIsDefault() != null ? entity.getIsDefault() : 0);
        addressMapper.insert(entity);
        log.info("Address created: id={}, customerId={}, type={}", entity.getId(), customerId, entity.getAddressType());
        return entity;
    }

    /**
     * 查询地址列表
     */
    public List<CustomerAddressEntity> listAddresses(Long customerId) {
        LambdaQueryWrapper<CustomerAddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerAddressEntity::getCustomerId, customerId)
               .orderByAsc(CustomerAddressEntity::getAddressType);
        return addressMapper.selectList(wrapper);
    }

    /**
     * 更新地址
     */
    @Transactional
    public CustomerAddressEntity updateAddress(Long id, CustomerAddressEntity entity) {
        CustomerAddressEntity existing = addressMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "地址不存在: " + id);
        }
        entity.setId(id);
        entity.setTenantId(existing.getTenantId());
        entity.setCustomerId(existing.getCustomerId());
        addressMapper.updateById(entity);
        return addressMapper.selectById(id);
    }

    /**
     * 删除地址
     */
    @Transactional
    public void deleteAddress(Long id) {
        CustomerAddressEntity existing = addressMapper.selectById(id);
        if (existing == null) {
            throw new BizException(404, "地址不存在: " + id);
        }
        addressMapper.deleteById(id);
        log.info("Address deleted: id={}", id);
    }

    /**
     * 设置默认地址 — 同一 address_type 下只有一个默认
     */
    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId, Integer addressType) {
        // Clear existing default for same customer + address type
        LambdaUpdateWrapper<CustomerAddressEntity> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(CustomerAddressEntity::getCustomerId, customerId)
                    .eq(CustomerAddressEntity::getAddressType, addressType)
                    .set(CustomerAddressEntity::getIsDefault, 0);
        addressMapper.update(null, clearWrapper);

        // Set new default
        CustomerAddressEntity entity = addressMapper.selectById(addressId);
        if (entity == null) {
            throw new BizException(404, "地址不存在: " + addressId);
        }
        if (!entity.getCustomerId().equals(customerId)) {
            throw new BizException("地址不属于该客户");
        }
        entity.setIsDefault(1);
        addressMapper.updateById(entity);

        log.info("Default address set: customerId={}, addressId={}, type={}", customerId, addressId, addressType);
    }
}
