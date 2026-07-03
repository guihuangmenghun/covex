package com.covex.service.liteflow;

import com.covex.service.entity.ProposalEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 校验保额不超过产品上限
 */
@LiteflowComponent("validateAmount")
public class ValidateAmountComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateAmountComponent.class);

    /** 默认产品保额上限 */
    private static final BigDecimal DEFAULT_MAX_SUM_INSURED = new BigDecimal("10000000");

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();

        if (proposal.getTotalSumInsured() == null) {
            return;
        }

        // 从产品快照获取保额上限，默认使用 1000 万
        BigDecimal maxAmount = DEFAULT_MAX_SUM_INSURED;
        if (proposal.getProductSnapshot() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> snapshot = (Map<String, Object>) proposal.getProductSnapshot();
            Object attrs = snapshot.get("attributes");
            if (attrs instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> attrMap = (Map<String, Object>) attrs;
                Object maxSa = attrMap.get("maxSumInsured");
                if (maxSa != null) {
                    maxAmount = new BigDecimal(maxSa.toString());
                }
            }
        }

        // 校验总保额
        if (proposal.getTotalSumInsured().compareTo(maxAmount) > 0) {
            ctx.addError("总保额 " + proposal.getTotalSumInsured() + " 超过产品上限 " + maxAmount);
            log.warn("Amount validation failed: totalSumInsured={}, max={}",
                    proposal.getTotalSumInsured(), maxAmount);
        }

        // 校验每项保额为正数
        if (proposal.getSelectedCoverages() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> coverages = (List<Map<String, Object>>) proposal.getSelectedCoverages();
            for (Map<String, Object> cov : coverages) {
                Object sumInsured = cov.get("sumInsured");
                if (sumInsured != null) {
                    BigDecimal sa = new BigDecimal(sumInsured.toString());
                    if (sa.compareTo(BigDecimal.ZERO) <= 0) {
                        ctx.addError("保障责任保额必须大于0: " + cov.get("coverageName"));
                    }
                }
            }
        }
    }
}
