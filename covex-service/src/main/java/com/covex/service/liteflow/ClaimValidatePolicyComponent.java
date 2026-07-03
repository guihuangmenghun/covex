package com.covex.service.liteflow;

import com.covex.service.entity.PolicyEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 理赔校验 — 保单有效性（status=1 有效）
 */
@LiteflowComponent("claimValidatePolicy")
public class ClaimValidatePolicyComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ClaimValidatePolicyComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        PolicyEntity policy = ctx.getClaimPolicy();

        if (policy == null) {
            ctx.addError("保单不存在");
            return;
        }

        if (policy.getStatus() != 1) {
            String statusName = switch (policy.getStatus()) {
                case 2 -> "中止";
                case 3 -> "终止";
                default -> "未知(" + policy.getStatus() + ")";
            };
            ctx.addError("保单状态无效，当前状态: " + statusName + "，只有有效状态的保单可以报案");
        }

        log.info("ClaimValidatePolicy: policyId={}, status={}", policy.getId(), policy.getStatus());
    }
}
