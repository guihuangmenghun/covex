package com.covex.service.liteflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.mapper.PolicyCoverageMapper;
import com.covex.service.util.ProductAttributeHelper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 理赔校验 — 共享保额逻辑。
 * <p>
 * 当产品 capabilities.shared_amount = true 且保障 coverage_detail.use_shared_amount = true 时，
 * 多个保障共享同一个保额池。本组件校验：
 * 1. 共享池总额（来自 attributes.max_sum_insured 或 capabilities.shared_amount_limit）
 * 2. 已使用总额（同一保单下所有 use_shared_amount=true 的保障的 cumulative_paid 总和）
 * 3. 本次理赔金额不超过可赔付额度
 */
@LiteflowComponent("claimSharedAmount")
public class ClaimSharedAmountComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimSharedAmountComponent.class);

    @Autowired
    private PolicyCoverageMapper policyCoverageMapper;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ClaimEntity claim = ctx.getClaim();
        PolicyEntity policy = ctx.getClaimPolicy();
        PolicyCoverageEntity coverage = ctx.getClaimCoverage();

        if (claim == null || policy == null || coverage == null) {
            return;
        }

        // 1. 从保单产品快照读取 capabilities
        Map<String, Object> snapshot = policy.getProductSnapshot();
        Map<String, Object> capabilities = ProductAttributeHelper.extractCapabilities(snapshot);
        Map<String, Object> attributes = ProductAttributeHelper.extractAttributes(snapshot);

        // 检查产品是否启用共享保额
        boolean sharedAmountEnabled = ProductAttributeHelper.getBooleanCapability(
                capabilities, false, ProductAttributeHelper.CAP_SHARED_AMOUNT);
        if (!sharedAmountEnabled) {
            log.debug("Shared amount not enabled for policy {}, skip", policy.getPolicyNo());
            return;
        }

        // 2. 检查当前保障是否使用共享保额池
        Map<String, Object> covDetail = coverage.getCoverageDetail();
        boolean useSharedAmount = false;
        if (covDetail != null) {
            Object usa = covDetail.get(ProductAttributeHelper.COV_USE_SHARED_AMOUNT);
            if (usa != null) {
                useSharedAmount = Boolean.parseBoolean(usa.toString());
            }
        }
        if (!useSharedAmount) {
            log.debug("Coverage {} does not use shared amount, skip", coverage.getCoverageCode());
            return;
        }

        // 3. 计算共享池总额
        BigDecimal sharedLimit = ProductAttributeHelper.getBigDecimalAttribute(
                capabilities, null, ProductAttributeHelper.CAP_SHARED_AMOUNT_LIMIT);
        if (sharedLimit == null) {
            sharedLimit = ProductAttributeHelper.getMaxSumInsured(attributes);
        }

        // 4. 查询同一保单下所有使用共享保额的保障的累计已赔付金额
        LambdaQueryWrapper<PolicyCoverageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyCoverageEntity::getPolicyId, policy.getId())
               .eq(PolicyCoverageEntity::getIsDeleted, 0);
        List<PolicyCoverageEntity> allCoverages = policyCoverageMapper.selectList(wrapper);

        BigDecimal totalUsed = BigDecimal.ZERO;
        for (PolicyCoverageEntity cov : allCoverages) {
            Map<String, Object> detail = cov.getCoverageDetail();
            if (detail != null) {
                Object usa = detail.get(ProductAttributeHelper.COV_USE_SHARED_AMOUNT);
                if (usa != null && Boolean.parseBoolean(usa.toString())) {
                    if (cov.getCumulativePaid() != null) {
                        totalUsed = totalUsed.add(cov.getCumulativePaid());
                    }
                }
            }
        }

        // 5. 计算可赔付额度
        BigDecimal remaining = sharedLimit.subtract(totalUsed);
        BigDecimal claimAmount = claim.getClaimAmount();

        if (claimAmount != null && claimAmount.compareTo(remaining) > 0) {
            ctx.addError("共享保额不足: 共享池限额=" + sharedLimit
                    + ", 已使用=" + totalUsed + ", 可赔付=" + remaining
                    + ", 申请理赔=" + claimAmount);
            log.warn("Shared amount insufficient: limit={}, used={}, remaining={}, claim={}",
                    sharedLimit, totalUsed, remaining, claimAmount);
        } else {
            log.info("Shared amount check passed: remaining={}, claim={}", remaining, claimAmount);
        }
    }
}
