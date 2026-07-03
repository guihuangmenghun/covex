package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.CustomerAddressEntity;
import com.covex.service.service.CustomerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户联系地址")
@RestController
@RequestMapping("/api/customer/{customerId}/address")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "创建地址")
    @PostMapping
    public Result<CustomerAddressEntity> createAddress(
            @PathVariable Long customerId,
            @RequestBody CustomerAddressEntity entity) {
        return Result.ok(addressService.createAddress(customerId, entity));
    }

    @Operation(summary = "查询地址列表")
    @GetMapping
    public Result<List<CustomerAddressEntity>> listAddresses(@PathVariable Long customerId) {
        return Result.ok(addressService.listAddresses(customerId));
    }

    @Operation(summary = "更新地址")
    @PutMapping("/{id}")
    public Result<CustomerAddressEntity> updateAddress(
            @PathVariable Long id,
            @RequestBody CustomerAddressEntity entity) {
        return Result.ok(addressService.updateAddress(id, entity));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.ok();
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(
            @PathVariable Long customerId,
            @PathVariable Long id,
            @RequestParam Integer addressType) {
        addressService.setDefaultAddress(customerId, id, addressType);
        return Result.ok();
    }
}
