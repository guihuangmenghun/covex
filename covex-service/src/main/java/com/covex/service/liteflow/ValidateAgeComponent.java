package com.covex.service.liteflow;

import com.covex.service.entity.CustomerEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.util.ProductAttributeHelper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

/**
 * 校验投保人年龄≥18，被保人年龄在产品 attributes 定义的范围内。
 * <p>
 * 读取产品属性（支持多种历史命名）：
 * - max_insured_age / maxAge / max_applicant_age（默认 70）
 * - min_insured_age / minAge / min_applicant_age（默认 0）
 * - max_maturity_age（保单到期年龄上限，默认 999 不限制）
 */
@LiteflowComponent("validateAge")
public class ValidateAgeComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateAgeComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();
        CustomerEntity applicant = ctx.getApplicant();
        CustomerEntity insured = ctx.getInsured();

        // 从产品快照提取 attributes
        Map<String, Object> attributes = ProductAttributeHelper.extractAttributes(
                proposal != null ? proposal.getProductSnapshot() : null);

        int maxAge = ProductAttributeHelper.getMaxInsuredAge(attributes);
        int minAge = ProductAttributeHelper.getMinInsuredAge(attributes);
        int maxMaturityAge = ProductAttributeHelper.getMaxMaturityAge(attributes);

        // 投保人 ≥ 18 岁
        if (applicant != null && applicant.getBirthDate() != null) {
            int age = Period.between(applicant.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                ctx.addError("投保人年龄不满18岁，当前年龄: " + age);
                log.warn("Validate age failed: applicant age={}", age);
            }
        }

        // 被保人年龄范围检查
        if (insured != null && insured.getBirthDate() != null) {
            int insuredAge = Period.between(insured.getBirthDate(), LocalDate.now()).getYears();

            if (insuredAge < minAge) {
                ctx.addError("被保人年龄不在产品允许范围(最小" + minAge + "岁)，当前年龄: " + insuredAge);
                log.warn("Validate age failed: insured age={} < min={}", insuredAge, minAge);
            }

            if (insuredAge > maxAge) {
                ctx.addError("被保人年龄不在产品允许范围(最大" + maxAge + "岁)，当前年龄: " + insuredAge);
                log.warn("Validate age failed: insured age={} > max={}", insuredAge, maxAge);
            }

            // 保单到期年龄校验（max_maturity_age）
            if (maxMaturityAge < 999 && proposal != null && proposal.getProductSnapshot() != null) {
                Object termTypeObj = proposal.getProductSnapshot().get("termType");
                if (termTypeObj != null) {
                    int termType = Integer.parseInt(termTypeObj.toString());
                    // termType: 1=定期(年), 2=终身, 3=趸交
                    if (termType == 1) {
                        // 定期保险：到期年龄 = 当前年龄 + 保险期间（从 attributes 或默认20年）
                        int termYears = ProductAttributeHelper.getIntAttribute(attributes, 20,
                                "term_years", "coverage_period", "insurance_period");
                        int maturityAge = insuredAge + termYears;
                        if (maturityAge > maxMaturityAge) {
                            ctx.addError("保单到期年龄(" + maturityAge + "岁)超过产品上限(" + maxMaturityAge + "岁)");
                            log.warn("Validate maturity age failed: maturityAge={}, max={}", maturityAge, maxMaturityAge);
                        }
                    }
                }
            }
        }
    }
}
