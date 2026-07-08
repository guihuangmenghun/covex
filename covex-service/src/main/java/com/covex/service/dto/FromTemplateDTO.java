package com.covex.service.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 从模板创建产品的请求 DTO
 */
@Data
public class FromTemplateDTO {

    /** 模板编码 */
    private String templateCode;

    /** 租户ID（可选，默认0） */
    private Long tenantId;

    /** PM 填写的参数 */
    private Map<String, Object> params;
}
