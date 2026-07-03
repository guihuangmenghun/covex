package com.covex.service.liteflow;

import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.entity.PolicyEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 理赔校验 — 等待期（accident_date 距 effective_date 超过等待期天数）
 */
@LiteflowComponent("claimValidateWaitingPeriod")
public class ClaimValidateWaitingPeriodComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimValidateWaitingPeriodComponent.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ClaimEntity claim = ctx.getClaim();
        PolicyEntity policy = ctx.getClaimPolicy();
        PolicyCoverageEntity coverage = ctx.getClaimCoverage();

        if (claim == null || policy == null || coverage == null) {
            ctx.addError("理赔案件、保单或险种明细信息缺失");
            return;
        }

        // 从 coverage_detail JSON 读取等待期天数
        int waitingPeriodDays = 0;
        try {
            if (coverage.getCoverageDetail() != null) {
                Map<String, Object> detail = OBJECT_MAPPER.convertValue(coverage.getCoverageDetail(), Map.class);
                Object wpObj = detail.get("waiting_period_days");
                if (wpObj == null) {
                    wpObj = detail.get("waitingPeriodDays");
                }
                if (wpObj != null) {
                    waitingPeriodDays = Integer.parseInt(wpObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse waiting_period_days from coverage_detail: {}", e.getMessage());
        }

        if (waitingPeriodDays > 0 && policy.getEffectiveDate() != null) {
            long daysSinceEffective = ChronoUnit.DAYS.between(policy.getEffectiveDate(), claim.getAccidentDate());
            if (daysSinceEffective < waitingPeriodDays) {
                ctx.addError("出险日期距生效日仅" + daysSinceEffective + "天，未超过等待期(" + waitingPeriodDays + "天)");
            }
        }

        log.info("ClaimValidateWaitingPeriod: waitingPeriodDays={}, accidentDate={}, effectiveDate={}",
                waitingPeriodDays, claim.getAccidentDate(), policy.getEffectiveDate());
    }
}
