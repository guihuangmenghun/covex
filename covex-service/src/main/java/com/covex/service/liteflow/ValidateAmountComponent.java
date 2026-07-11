package com.covex.service.liteflow;

import com.covex.service.entity.ProposalEntity;
import com.covex.service.util.ProductAttributeHelper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 校验投保保额：
 * 1. 总保额不超过产品 attributes.max_sum_insured
 * 2. 每项保额不低于 attributes.min_sum_insured
 * 3. 每项保额为正数
 * 4. coverage_detail.count_toward_amount = false 的保障不计入总保额校验
 */
@LiteflowComponent("validateAmount")
public class ValidateAmountComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateAmountComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();

        if (proposal == null) return;

        // 从产品快照提取 attributes
        Map<String, Object> attributes = ProductAttributeHelper.extractAttributes(
                proposal.getProductSnapshot());

        BigDecimal maxAmount = ProductAttributeHelper.getMaxSumInsured(attributes);
        BigDecimal minAmount = ProductAttributeHelper.getMinSumInsured(attributes);

        // 校验每项保额
        List<Map<String, Object>> coverages = proposal.getSelectedCoverages();
        BigDecimal countedSumInsured = BigDecimal.ZERO;

        if (coverages != null) {
            for (Map<String, Object> cov : coverages) {
                Object sumInsuredObj = cov.get("sumInsured");
                if (sumInsuredObj == null) continue;

                BigDecimal sumInsured = new BigDecimal(sumInsuredObj.toString());

                // 保额必须为正数
                if (sumInsured.compareTo(BigDecimal.ZERO) <= 0) {
                    ctx.addError("保障责任保额必须大于0: " + cov.get("coverageName"));
                    continue;
                }

                // 单项最低保额校验
                if (minAmount.compareTo(BigDecimal.ZERO) > 0 && sumInsured.compareTo(minAmount) < 0) {
                    ctx.addError("保障责任「" + cov.get("coverageName")
                            + "」保额(" + sumInsured + ")低于产品最低限额(" + minAmount + ")");
                    log.warn("Min sum insured violated: coverage={}, amount={}, min={}",
                            cov.get("coverageName"), sumInsured, minAmount);
                }

                // 判断是否计入总保额（count_toward_amount 默认为 true）
                boolean countToward = true;
                Object coverageDetail = cov.get("coverageDetail");
                if (coverageDetail instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> detail = (Map<String, Object>) coverageDetail;
                    Object cta = detail.get(ProductAttributeHelper.COV_COUNT_TOWARD_AMOUNT);
                    if (cta != null) {
                        countToward = Boolean.parseBoolean(cta.toString());
                    }
                }

                if (countToward) {
                    countedSumInsured = countedSumInsured.add(sumInsured);
                }
            }
        }

        // 校验总保额（仅包含 count_toward_amount=true 的保障）
        if (proposal.getTotalSumInsured() != null
                && countedSumInsured.compareTo(maxAmount) > 0) {
            ctx.addError("有效总保额 " + countedSumInsured + " 超过产品上限 " + maxAmount);
            log.warn("Amount validation failed: countedSumInsured={}, max={}",
                    countedSumInsured, maxAmount);
        }
    }
}
