package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.result.Result;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "保单管理")
@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(summary = "出单（从已支付投保单生成保单）")
    @PostMapping("/issue/{proposalId}")
    public Result<PolicyEntity> issue(@PathVariable Long proposalId) {
        return Result.ok(policyService.issuePolicy(proposalId));
    }

    @Operation(summary = "查询保单详情（含险种明细+缴费计划）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.ok(policyService.getPolicyById(id));
    }

    @Operation(summary = "分页查询保单列表")
    @GetMapping
    public Result<Page<PolicyEntity>> list(
            @RequestParam(required = false) String policyNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(policyService.listPolicies(policyNo, status, applicantId, page, size));
    }
}
