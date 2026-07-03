package com.covex.service.liteflow;

import com.covex.service.entity.CustomerEntity;
import com.covex.service.entity.ProposalEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;

/**
 * 校验投保人年龄≥18，被保人在产品允许年龄范围内
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

        // 投保人 ≥ 18 岁
        if (applicant != null && applicant.getBirthDate() != null) {
            int age = Period.between(applicant.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                ctx.addError("投保人年龄不满18岁，当前年龄: " + age);
                log.warn("Validate age failed: applicant age={}", age);
            }
        }

        // 被保人年龄范围检查（产品允许 0-70 岁）
        if (insured != null && insured.getBirthDate() != null) {
            int insuredAge = Period.between(insured.getBirthDate(), LocalDate.now()).getYears();
            if (insuredAge < 0 || insuredAge > 70) {
                ctx.addError("被保人年龄不在产品允许范围(0-70)，当前年龄: " + insuredAge);
                log.warn("Validate age failed: insured age={}", insuredAge);
            }
        }
    }
}
