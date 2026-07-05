package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.dto.ClaimReviewDTO;
import com.covex.service.dto.InvestigationResultDTO;
import com.covex.service.dto.ReportClaimDTO;
import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.ClaimReviewEntity;
import com.covex.service.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "理赔管理")
@RestController
@RequestMapping("/api/claim")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @Operation(summary = "报案")
    @PostMapping
    public Result<ClaimEntity> reportClaim(@RequestBody ReportClaimDTO dto) {
        return Result.ok(claimService.reportClaim(dto));
    }

    @Operation(summary = "查询理赔详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.ok(claimService.getClaimDetail(id));
    }

    @Operation(summary = "分页查询理赔列表")
    @GetMapping
    public Result<Page<ClaimEntity>> list(
            @RequestParam(required = false) String policyNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String handler,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(claimService.listClaims(policyNo, status, handler, page, size));
    }

    @Operation(summary = "分配理赔员")
    @PostMapping("/{id}/assign")
    public Result<ClaimEntity> assignHandler(@PathVariable Long id) {
        return Result.ok(claimService.assignHandler(id));
    }

    @RequiresPermission(code = "claim:review")
    @Operation(summary = "提交审核")
    @PostMapping("/{id}/review")
    public Result<ClaimReviewEntity> submitReview(@PathVariable Long id,
                                                   @RequestBody ClaimReviewDTO dto) {
        return Result.ok(claimService.submitReview(id, dto));
    }

    @Operation(summary = "赔付计算")
    @PostMapping("/{id}/calculate")
    public Result<BigDecimal> calculate(@PathVariable Long id) {
        return Result.ok(claimService.calculateClaimAmount(id));
    }

    @Operation(summary = "启动调查")
    @PostMapping("/{id}/investigate")
    public Result<ClaimEntity> startInvestigation(@PathVariable Long id) {
        return Result.ok(claimService.startInvestigation(id));
    }

    @Operation(summary = "提交调查结论")
    @PostMapping("/{id}/investigation-result")
    public Result<ClaimEntity> submitInvestigationResult(@PathVariable Long id,
                                                          @RequestBody InvestigationResultDTO dto) {
        return Result.ok(claimService.submitInvestigationResult(id, dto));
    }
}
