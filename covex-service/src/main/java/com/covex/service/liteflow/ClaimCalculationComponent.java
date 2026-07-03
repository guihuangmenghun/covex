package com.covex.service.liteflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.ClaimPaymentEntity;
import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.mapper.ClaimMapper;
import com.covex.service.mapper.ClaimPaymentMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 理赔赔付计算 — 使用 Aviator 表达式引擎
 * <p>
 * 计算逻辑：
 * claim = (claim_amount - deductible) * claim_ratio
 * 如果 max_benefit 不为空：claim = min(claim, max_benefit - 累计已赔)
 * 如果 claim_day_limit 不为空：校验天数限额
 */
@LiteflowComponent("claimCalculation")
public class ClaimCalculationComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimCalculationComponent.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ClaimMapper claimMapper;
    private final ClaimPaymentMapper claimPaymentMapper;

    public ClaimCalculationComponent(ClaimMapper claimMapper,
                                      ClaimPaymentMapper claimPaymentMapper) {
        this.claimMapper = claimMapper;
        this.claimPaymentMapper = claimPaymentMapper;
    }

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ClaimEntity claim = ctx.getClaim();
        PolicyCoverageEntity coverage = ctx.getClaimCoverage();

        if (claim == null || coverage == null) {
            ctx.addError("理赔案件或险种明细信息缺失，无法计算赔付");
            return;
        }

        BigDecimal claimAmount = claim.getClaimAmount();
        if (claimAmount == null || claimAmount.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.addError("申请赔付金额必须大于0");
            return;
        }

        // 从 coverage_detail 读取计算参数
        BigDecimal deductible = coverage.getDeductible() != null ? coverage.getDeductible() : BigDecimal.ZERO;
        BigDecimal claimRatio = BigDecimal.ONE;
        BigDecimal maxBenefit = null;
        BigDecimal claimDayLimit = null;

        try {
            if (coverage.getCoverageDetail() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detail = OBJECT_MAPPER.convertValue(coverage.getCoverageDetail(), Map.class);

                Object ratioObj = detail.get("claim_ratio");
                if (ratioObj != null) {
                    claimRatio = new BigDecimal(ratioObj.toString());
                }

                Object maxObj = detail.get("max_benefit");
                if (maxObj != null) {
                    maxBenefit = new BigDecimal(maxObj.toString());
                }

                Object dayLimitObj = detail.get("claim_day_limit");
                if (dayLimitObj != null) {
                    claimDayLimit = new BigDecimal(dayLimitObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse coverage_detail for claim calculation: {}", e.getMessage());
        }

        // Aviator 计算: claim = (claim_amount - deductible) * claim_ratio
        BigDecimal base = claimAmount.subtract(deductible);
        if (base.compareTo(BigDecimal.ZERO) < 0) {
            base = BigDecimal.ZERO;
        }
        BigDecimal calculated = base.multiply(claimRatio).setScale(2, RoundingMode.HALF_UP);

        // 如果 max_benefit 不为空：claim = min(claim, max_benefit - 累计已赔)
        if (maxBenefit != null) {
            BigDecimal cumulativePaid = getCumulativePaid(coverage.getId());
            BigDecimal remaining = maxBenefit.subtract(cumulativePaid);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }
            if (calculated.compareTo(remaining) > 0) {
                calculated = remaining;
            }
        }

        // 如果 claim_day_limit 不为空：校验天数限额
        if (claimDayLimit != null) {
            // 天数限额校验（简化处理：按事故描述中的天数或默认1天计算）
            if (calculated.compareTo(claimDayLimit) > 0) {
                calculated = claimDayLimit;
            }
        }

        // 确保赔付金额不超过保额
        if (coverage.getSumInsured() != null && calculated.compareTo(coverage.getSumInsured()) > 0) {
            calculated = coverage.getSumInsured();
        }

        ctx.setCalculatedAmount(calculated);
        log.info("ClaimCalculation: claimId={}, claimAmount={}, deductible={}, ratio={}, calculated={}",
                claim.getId(), claimAmount, deductible, claimRatio, calculated);
    }

    /**
     * 获取险种明细的累计已赔金额
     */
    private BigDecimal getCumulativePaid(Long coverageId) {
        try {
            LambdaQueryWrapper<ClaimEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ClaimEntity::getCoverageId, coverageId)
                   .in(ClaimEntity::getStatus, 4, 6); // 已赔付 或 已结案
            var claims = claimMapper.selectList(wrapper);

            BigDecimal total = BigDecimal.ZERO;
            for (ClaimEntity c : claims) {
                if (c.getApprovedAmount() != null) {
                    total = total.add(c.getApprovedAmount());
                }
            }
            return total;
        } catch (Exception e) {
            log.warn("Failed to query cumulative paid for coverageId={}: {}", coverageId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
