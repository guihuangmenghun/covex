package com.covex.service.dto;

import lombok.Data;

/**
 * 理赔材料上传请求
 */
@Data
public class UploadClaimDocumentDTO {

    private Integer documentType;
    private String fileUrl;
    private String fileName;
    private String uploadedBy;
}
