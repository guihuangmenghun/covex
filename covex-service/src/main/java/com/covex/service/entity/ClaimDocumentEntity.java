package com.covex.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.covex.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 理赔材料
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ins_claim_document")
public class ClaimDocumentEntity extends BaseEntity {

    private Long claimId;
    private Integer documentType;
    private String fileUrl;
    private String fileName;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
