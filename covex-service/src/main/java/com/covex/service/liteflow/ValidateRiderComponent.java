package com.covex.service.liteflow;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 校验附加险在主险允许范围内
 * 当前简化实现：仅检查投保单中是否有选中的保障
 */
@LiteflowComponent("validateRider")
public class ValidateRiderComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateRiderComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);

        if (ctx.getProposal().getSelectedCoverages() == null) {
            ctx.addError("未选择任何保障责任");
            log.warn("No coverages selected for proposal: {}", ctx.getProposal().getProposalNo());
            return;
        }

        log.debug("Rider validation passed for proposal: {}", ctx.getProposal().getProposalNo());
    }
}
