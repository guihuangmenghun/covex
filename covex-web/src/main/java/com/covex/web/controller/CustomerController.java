package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.result.Result;
import com.covex.service.entity.CustomerEntity;
import com.covex.service.entity.CustomerInsuredEntity;
import com.covex.service.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "创建客户")
    @PostMapping
    public Result<CustomerEntity> createCustomer(@RequestBody CustomerEntity entity) {
        if (entity.getTenantId() == null) {
            entity.setTenantId(0L);
        }
        return Result.ok(customerService.createCustomer(entity));
    }

    @Operation(summary = "查询客户详情")
    @GetMapping("/{id}")
    public Result<CustomerEntity> getCustomer(@PathVariable Long id) {
        return Result.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "分页查询客户")
    @GetMapping
    public Result<Page<CustomerEntity>> listCustomers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "0") Long tenantId) {
        return Result.ok(customerService.listCustomers(tenantId, keyword, page, size));
    }

    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    public Result<CustomerEntity> updateCustomer(@PathVariable Long id, @RequestBody CustomerEntity entity) {
        return Result.ok(customerService.updateCustomer(id, entity));
    }

    @Operation(summary = "确保投保人扩展存在")
    @PostMapping("/{id}/ensure-applicant")
    public Result<Void> ensureApplicant(@PathVariable Long id) {
        customerService.ensureApplicant(id);
        return Result.ok();
    }

    @Operation(summary = "确保被保人扩展存在")
    @PostMapping("/{id}/ensure-insured")
    public Result<Void> ensureInsured(@PathVariable Long id) {
        customerService.ensureInsured(id);
        return Result.ok();
    }

    @Operation(summary = "查询健康档案")
    @GetMapping("/{id}/health")
    public Result<CustomerInsuredEntity> getHealth(@PathVariable Long id) {
        CustomerInsuredEntity health = customerService.getHealthRecord(id);
        return Result.ok(health);
    }

    @Operation(summary = "更新健康档案")
    @PutMapping("/{id}/health")
    public Result<Void> updateHealth(@PathVariable Long id, @RequestBody CustomerInsuredEntity healthData) {
        customerService.updateHealthRecord(id, healthData);
        return Result.ok();
    }
}
