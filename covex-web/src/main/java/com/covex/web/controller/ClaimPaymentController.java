package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.dto.ClaimPaymentCallbackDTO;
import com.covex.service.dto.ClaimPaymentDTO;
import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.ClaimPaymentEntity;
import com.covex.service.service.ClaimPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "理赔赔付管理")
@RestController
@RequestMapping("/api/claim/{claimId}/payment")
public class ClaimPaymentController {

    private final ClaimPaymentService claimPaymentService;

    public ClaimPaymentController(ClaimPaymentService claimPaymentService) {
        this.claimPaymentService = claimPaymentService;
    }

    @Operation(summary = "触发赔付")
    @PostMapping("/process")
    public Result<ClaimPaymentEntity> processPayment(@PathVariable Long claimId,
                                                      @RequestBody(required = false) ClaimPaymentDTO dto) {
        Long beneficiaryId = dto != null ? dto.getBeneficiaryId() : null;
        return Result.ok(claimPaymentService.processPayment(claimId, beneficiaryId));
    }

    @Operation(summary = "支付回调")
    @PostMapping("/callback")
    public Result<Void> paymentCallback(@PathVariable Long claimId,
                                         @RequestBody ClaimPaymentCallbackDTO dto) {
        // 查找最新的 claim_payment 记录
        // 简化处理：通过 claimId 查找最新的赔付记录
        // 在实际中应该通过支付流水号来查找
        claimPaymentService.handlePaymentCallbackByClaimId(claimId, dto.getSuccess());
        return Result.ok();
    }

    @Operation(summary = "结案")
    @PostMapping("/close")
    public Result<ClaimEntity> closeCase(@PathVariable Long claimId) {
        return Result.ok(claimPaymentService.closeCase(claimId));
    }

    @Operation(summary = "拒赔申诉")
    @PostMapping("/dispute")
    public Result<ClaimEntity> dispute(@PathVariable Long claimId) {
        return Result.ok(claimPaymentService.handleClaimDispute(claimId));
    }
}
