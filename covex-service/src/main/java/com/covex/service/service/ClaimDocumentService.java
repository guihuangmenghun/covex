package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.dto.UploadClaimDocumentDTO;
import com.covex.service.entity.ClaimDocumentEntity;
import com.covex.service.entity.ClaimEntity;
import com.covex.service.mapper.ClaimDocumentMapper;
import com.covex.service.mapper.ClaimMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 理赔材料服务
 */
@Service
public class ClaimDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ClaimDocumentService.class);

    private final ClaimDocumentMapper claimDocumentMapper;
    private final ClaimMapper claimMapper;

    public ClaimDocumentService(ClaimDocumentMapper claimDocumentMapper,
                                 ClaimMapper claimMapper) {
        this.claimDocumentMapper = claimDocumentMapper;
        this.claimMapper = claimMapper;
    }

    /**
     * 上传理赔材料
     */
    @Transactional
    public ClaimDocumentEntity uploadDocument(Long claimId, UploadClaimDocumentDTO dto) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }

        ClaimDocumentEntity doc = new ClaimDocumentEntity();
        doc.setTenantId(claim.getTenantId());
        doc.setClaimId(claimId);
        doc.setDocumentType(dto.getDocumentType());
        doc.setFileUrl(dto.getFileUrl());
        doc.setFileName(dto.getFileName());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setUploadedBy(dto.getUploadedBy());
        claimDocumentMapper.insert(doc);

        log.info("Claim document uploaded: claimId={}, type={}, fileName={}",
                claimId, dto.getDocumentType(), dto.getFileName());
        return doc;
    }

    /**
     * 查询理赔材料列表
     */
    public List<ClaimDocumentEntity> listDocuments(Long claimId) {
        LambdaQueryWrapper<ClaimDocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimDocumentEntity::getClaimId, claimId)
               .orderByDesc(ClaimDocumentEntity::getUploadedAt);
        return claimDocumentMapper.selectList(wrapper);
    }
}
