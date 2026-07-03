package com.covex.service.liteflow;

import com.covex.service.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * LiteFlow 流程上下文 — 在链组件间传递数据
 */
@Data
public class CovexFlowContext {

    /** 投保单 */
    private ProposalEntity proposal;

    /** 投保人 */
    private CustomerEntity applicant;

    /** 被保人 */
    private CustomerEntity insured;

    /** 被保人健康档案 */
    private CustomerInsuredEntity insuredHealth;

    /** 产品 */
    private ProductEntity product;

    /** 渠道商 */
    private ChannelEntity channel;

    /** 产品保障列表 */
    private List<ProductCoverageEntity> coverages = new ArrayList<>();

    /** 核保结论列表 */
    private List<Integer> uwResults = new ArrayList<>();

    /** 核保备注 */
    private List<String> uwComments = new ArrayList<>();

    /** 校验/核保失败原因 */
    private List<String> errors = new ArrayList<>();

    /** 是否通过 */
    private boolean passed = true;

    /** 生成的保单 */
    private PolicyEntity policy;

    /** 保单险种明细 */
    private List<PolicyCoverageEntity> policyCoverages = new ArrayList<>();

    /** 保单缴费计划 */
    private List<PolicyPremiumEntity> policyPremiums = new ArrayList<>();

    // ========== 理赔域 ==========

    /** 理赔案件 */
    private ClaimEntity claim;

    /** 理赔关联的保单 */
    private PolicyEntity claimPolicy;

    /** 理赔关联的险种明细 */
    private PolicyCoverageEntity claimCoverage;

    /** 赔付计算结果 */
    private java.math.BigDecimal calculatedAmount;

    public void addError(String error) {
        this.errors.add(error);
        this.passed = false;
    }

    public void addUwResult(int result, String comment) {
        this.uwResults.add(result);
        if (comment != null) {
            this.uwComments.add(comment);
        }
    }
}
