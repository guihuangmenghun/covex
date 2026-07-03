package com.covex.service.liteflow;

import com.covex.service.entity.ProposalEntity;
import com.covex.service.service.UnderwritingService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * 累计风险保额校验 — 查询该被保人所有有效保单的总保额
 */
@LiteflowComponent("uwCumulativeAmount")
public class UwCumulativeAmountComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(UwCumulativeAmountComponent.class);

    /** 累计风险保额上限 */
    private static final BigDecimal MAX_CUMULATIVE_SUM_INSURED = new BigDecimal("5000000");

    @Autowired
    private UnderwritingService underwritingService;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();

        BigDecimal cumulativeSumInsured = BigDecimal.ZERO;
        try {
            cumulativeSumInsured = underwritingService.getCumulativeSumInsured(proposal.getInsuredId());
        } catch (Exception e) {
            // 降级处理：如果查询失败，默认通过
            log.warn("Failed to query cumulative sum insured, degrade to pass: {}", e.getMessage());
            ctx.addUwResult(1, "累计保额查询降级，默认通过");
            return;
        }

        BigDecimal totalWithCurrent = cumulativeSumInsured.add(
                proposal.getTotalSumInsured() != null ? proposal.getTotalSumInsured() : BigDecimal.ZERO);

        if (totalWithCurrent.compareTo(MAX_CUMULATIVE_SUM_INSURED) > 0) {
            ctx.addUwResult(5, "累计风险保额超限: 已有=" + cumulativeSumInsured
                    + ", 本次=" + proposal.getTotalSumInsured()
                    + ", 上限=" + MAX_CUMULATIVE_SUM_INSURED);
            log.warn("Cumulative sum insured exceeded: total={}, max={}",
                    totalWithCurrent, MAX_CUMULATIVE_SUM_INSURED);
        } else {
            ctx.addUwResult(1, "累计风险保额正常: " + totalWithCurrent);
            log.info("Cumulative UW: OK, total={}", totalWithCurrent);
        }
    }
}
