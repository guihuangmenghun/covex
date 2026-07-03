package com.covex.service.liteflow;

import com.covex.service.entity.PolicyCoverageEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 理赔校验 — 险种匹配（coverage 状态=1 有效）
 */
@LiteflowComponent("claimValidateCoverageMatch")
public class ClaimValidateCoverageMatchComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimValidateCoverageMatchComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        PolicyCoverageEntity coverage = ctx.getClaimCoverage();

        if (coverage == null) {
            ctx.addError("险种明细不存在");
            return;
        }

        if (coverage.getStatus() != 1) {
            String statusName = coverage.getStatus() == 2 ? "已终止" : "未知(" + coverage.getStatus() + ")";
            ctx.addError("险种明细状态无效，当前状态: " + statusName + "，只有有效状态的险种可以理赔");
        }

        log.info("ClaimValidateCoverageMatch: coverageId={}, status={}, code={}",
                coverage.getId(), coverage.getStatus(), coverage.getCoverageCode());
    }
}
