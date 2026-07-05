package com.covex.web.controller;

import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.entity.CommissionEntity;
import com.covex.service.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "佣金管理")
@RestController
@RequestMapping("/api/commission")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @Operation(summary = "查询佣金列表")
    @GetMapping
    public Result<List<CommissionEntity>> list(
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer status) {
        return Result.ok(commissionService.listCommissions(channelId, month, status));
    }

    @Operation(summary = "计算佣金")
    @PostMapping("/calculate")
    public Result<CommissionEntity> calculate(@RequestBody Map<String, Object> body) {
        Long tenantId = Long.valueOf(body.get("tenantId").toString());
        Long policyId = Long.valueOf(body.get("policyId").toString());
        Long channelId = Long.valueOf(body.get("channelId").toString());
        Long channelUserId = body.get("channelUserId") != null ? Long.valueOf(body.get("channelUserId").toString()) : null;
        BigDecimal premiumAmount = new BigDecimal(body.get("premiumAmount").toString());
        Integer commissionType = Integer.valueOf(body.get("commissionType").toString());
        BigDecimal commissionRate = new BigDecimal(body.get("commissionRate").toString());
        return Result.ok(commissionService.calculateCommission(tenantId, policyId, channelId, channelUserId, premiumAmount, commissionType, commissionRate));
    }

    @RequiresPermission(code = "commission:settle")
    @Operation(summary = "触发月度结算")
    @PostMapping("/settle")
    public Result<Map<String, Object>> settle(@RequestBody Map<String, String> body) {
        String yearMonth = body.get("yearMonth");
        if (yearMonth == null || yearMonth.isEmpty()) {
            return Result.fail("缺少 yearMonth 参数");
        }
        return Result.ok(commissionService.monthlySettle(yearMonth));
    }

    @Operation(summary = "月度汇总统计")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(
            @RequestParam Long channelId,
            @RequestParam String yearMonth) {
        return Result.ok(commissionService.getMonthlySummary(channelId, yearMonth));
    }

    @Operation(summary = "确认支付")
    @PutMapping("/confirm")
    public Result<CommissionEntity> confirm(@RequestBody Map<String, Long> body) {
        Long commissionId = body.get("commissionId");
        if (commissionId == null) {
            return Result.fail("缺少 commissionId 参数");
        }
        return Result.ok(commissionService.confirmSettle(commissionId));
    }
}
