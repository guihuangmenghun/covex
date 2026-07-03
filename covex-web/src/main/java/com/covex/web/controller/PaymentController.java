package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.dto.CreatePaymentDTO;
import com.covex.service.dto.PaymentCallbackDTO;
import com.covex.service.entity.PaymentEntity;
import com.covex.service.service.PaymentService;
import com.covex.service.service.PremiumCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "支付管理")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PremiumCalculationService premiumCalculationService;

    public PaymentController(PaymentService paymentService,
                             PremiumCalculationService premiumCalculationService) {
        this.paymentService = paymentService;
        this.premiumCalculationService = premiumCalculationService;
    }

    @Operation(summary = "计算保费")
    @PostMapping("/calculate/{proposalId}")
    public Result<BigDecimal> calculatePremium(@PathVariable Long proposalId) {
        return Result.ok(premiumCalculationService.calculatePremium(proposalId));
    }

    @Operation(summary = "创建支付记录")
    @PostMapping("/create")
    public Result<PaymentEntity> create(@RequestBody CreatePaymentDTO dto) {
        return Result.ok(paymentService.createPayment(dto));
    }

    @Operation(summary = "支付回调")
    @PostMapping("/callback")
    public Result<PaymentEntity> callback(@RequestBody PaymentCallbackDTO dto) {
        return Result.ok(paymentService.handlePaymentCallback(dto));
    }

    @Operation(summary = "查询投保单支付记录")
    @GetMapping("/query/{proposalId}")
    public Result<List<PaymentEntity>> queryByProposal(@PathVariable Long proposalId) {
        return Result.ok(paymentService.queryPaymentByProposal(proposalId));
    }

    @Operation(summary = "扫描超时投保单并撤销")
    @PostMapping("/timeout-scan")
    public Result<Integer> timeoutScan() {
        return Result.ok(paymentService.handlePaymentTimeout());
    }
}
