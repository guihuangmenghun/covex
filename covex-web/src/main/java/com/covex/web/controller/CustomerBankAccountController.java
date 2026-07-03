package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.entity.CustomerBankAccountEntity;
import com.covex.service.service.CustomerBankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "客户银行账户")
@RestController
@RequestMapping("/api/customer/{customerId}/bank-account")
public class CustomerBankAccountController {

    private final CustomerBankAccountService accountService;

    public CustomerBankAccountController(CustomerBankAccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "创建银行账户")
    @PostMapping
    public Result<CustomerBankAccountEntity> createAccount(
            @PathVariable Long customerId,
            @RequestBody CustomerBankAccountEntity entity) {
        return Result.ok(accountService.createAccount(customerId, entity));
    }

    @Operation(summary = "查询银行账户列表")
    @GetMapping
    public Result<List<CustomerBankAccountEntity>> listAccounts(@PathVariable Long customerId) {
        return Result.ok(accountService.listAccounts(customerId));
    }

    @Operation(summary = "更新银行账户")
    @PutMapping("/{id}")
    public Result<CustomerBankAccountEntity> updateAccount(
            @PathVariable Long id,
            @RequestBody CustomerBankAccountEntity entity) {
        return Result.ok(accountService.updateAccount(id, entity));
    }

    @Operation(summary = "删除银行账户（有代扣协议保护）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return Result.ok();
    }

    @Operation(summary = "设置默认银行账户")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(
            @PathVariable Long customerId,
            @PathVariable Long id,
            @RequestParam Integer usageType) {
        accountService.setDefaultAccount(customerId, id, usageType);
        return Result.ok();
    }
}
