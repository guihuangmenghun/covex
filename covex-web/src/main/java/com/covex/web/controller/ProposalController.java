package com.covex.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.result.Result;
import com.covex.service.dto.CreateProposalDTO;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "投保单管理")
@RestController
@RequestMapping("/api/proposal")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @Operation(summary = "创建投保单")
    @PostMapping
    public Result<ProposalEntity> create(@RequestBody CreateProposalDTO dto) {
        return Result.ok(proposalService.createProposal(dto));
    }

    @Operation(summary = "查询投保单详情")
    @GetMapping("/{id}")
    public Result<ProposalEntity> getById(@PathVariable Long id) {
        return Result.ok(proposalService.getProposalById(id));
    }

    @Operation(summary = "分页查询投保单列表")
    @GetMapping
    public Result<Page<ProposalEntity>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(proposalService.listProposals(status, channelId, keyword, page, size));
    }

    @Operation(summary = "提交投保单（触发校验+核保链）")
    @PutMapping("/{id}/submit")
    public Result<ProposalEntity> submit(@PathVariable Long id) {
        return Result.ok(proposalService.submitProposal(id));
    }
}
