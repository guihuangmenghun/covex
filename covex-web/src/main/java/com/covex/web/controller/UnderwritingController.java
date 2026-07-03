package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.dto.ManualUnderwriteDTO;
import com.covex.service.entity.UnderwritingRecordEntity;
import com.covex.service.service.UnderwritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "核保管理")
@RestController
@RequestMapping("/api/underwriting")
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    public UnderwritingController(UnderwritingService underwritingService) {
        this.underwritingService = underwritingService;
    }

    @Operation(summary = "自动核保（触发核保链）")
    @PostMapping("/auto/{proposalId}")
    public Result<UnderwritingRecordEntity> autoUnderwrite(@PathVariable Long proposalId) {
        // 自动核保已在 submitProposal 中执行，此处仅保存记录
        UnderwritingRecordEntity record = underwritingService.saveAutoUwResult(
                proposalId, 1, null, null, "手动触发自动核保");
        return Result.ok(record);
    }

    @Operation(summary = "人工核保")
    @PostMapping("/manual/{proposalId}")
    public Result<UnderwritingRecordEntity> manualUnderwrite(
            @PathVariable Long proposalId,
            @RequestBody ManualUnderwriteDTO dto) {
        return Result.ok(underwritingService.manualUnderwrite(proposalId, dto));
    }

    @Operation(summary = "查询核保记录")
    @GetMapping("/records/{proposalId}")
    public Result<List<UnderwritingRecordEntity>> getRecords(@PathVariable Long proposalId) {
        return Result.ok(underwritingService.getUnderwritingRecords(proposalId));
    }
}
