package com.covex.service.liteflow;

import com.covex.service.entity.ClaimEntity;
import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.entity.PolicyEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * 理赔校验 — 保障期间（accident_date 在 effective_date ~ expiry_date 之间）
 */
@LiteflowComponent("claimValidateCoverage")
public class ClaimValidateCoverageComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimValidateCoverageComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ClaimEntity claim = ctx.getClaim();
        PolicyEntity policy = ctx.getClaimPolicy();

        if (claim == null || policy == null) {
            ctx.addError("理赔案件或保单信息缺失");
            return;
        }

        LocalDate accidentDate = claim.getAccidentDate();
        if (accidentDate == null) {
            ctx.addError("出险日期不能为空");
            return;
        }

        LocalDate effectiveDate = policy.getEffectiveDate();
        LocalDate expiryDate = policy.getExpiryDate();

        if (effectiveDate != null && accidentDate.isBefore(effectiveDate)) {
            ctx.addError("出险日期(" + accidentDate + ")早于保单生效日(" + effectiveDate + ")");
        }

        if (expiryDate != null && accidentDate.isAfter(expiryDate)) {
            ctx.addError("出险日期(" + accidentDate + ")晚于保单到期日(" + expiryDate + ")");
        }

        log.info("ClaimValidateCoverage: accidentDate={}, effectiveDate={}, expiryDate={}",
                accidentDate, effectiveDate, expiryDate);
    }
}
