package com.covex.web.controller;

import com.covex.common.result.Result;
import com.covex.service.dto.UploadClaimDocumentDTO;
import com.covex.service.entity.ClaimDocumentEntity;
import com.covex.service.service.ClaimDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "理赔材料管理")
@RestController
@RequestMapping("/api/claim/{claimId}/document")
public class ClaimDocumentController {

    private final ClaimDocumentService claimDocumentService;

    public ClaimDocumentController(ClaimDocumentService claimDocumentService) {
        this.claimDocumentService = claimDocumentService;
    }

    @Operation(summary = "上传理赔材料")
    @PostMapping
    public Result<ClaimDocumentEntity> upload(@PathVariable Long claimId,
                                               @RequestBody UploadClaimDocumentDTO dto) {
        return Result.ok(claimDocumentService.uploadDocument(claimId, dto));
    }

    @Operation(summary = "查询理赔材料列表")
    @GetMapping
    public Result<List<ClaimDocumentEntity>> list(@PathVariable Long claimId) {
        return Result.ok(claimDocumentService.listDocuments(claimId));
    }
}
