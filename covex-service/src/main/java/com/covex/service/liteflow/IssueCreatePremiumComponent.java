package com.covex.service.liteflow;

import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.PolicyPremiumEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PolicyPremiumMapper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 出单 — 创建保单缴费计划
 */
@LiteflowComponent("issueCreatePremium")
public class IssueCreatePremiumComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueCreatePremiumComponent.class);

    @Autowired
    private PolicyPremiumMapper policyPremiumMapper;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();
        PolicyEntity policy = ctx.getPolicy();

        List<PolicyPremiumEntity> premiumEntities = new ArrayList<>();

        PolicyPremiumEntity premium = new PolicyPremiumEntity();
        premium.setTenantId(proposal.getTenantId());
        premium.setPolicyId(policy.getId());

        // 从选中的缴费计划中提取信息
        Map<String, Object> plan = proposal.getSelectedPremiumPlan();
        if (plan != null) {
            premium.setPremiumPlanCode(plan.getOrDefault("premiumPlanCode", "DEFAULT").toString());
            if (plan.get("paymentFrequency") != null) {
                premium.setPaymentFrequency(Integer.parseInt(plan.get("paymentFrequency").toString()));
            } else {
                premium.setPaymentFrequency(1); // 年缴
            }
            if (plan.get("paymentTerm") != null) {
                premium.setPaymentTerm(Integer.parseInt(plan.get("paymentTerm").toString()));
            } else {
                premium.setPaymentTerm(1);
            }
            if (plan.get("paymentTermUnit") != null) {
                premium.setPaymentTermUnit(Integer.parseInt(plan.get("paymentTermUnit").toString()));
            } else {
                premium.setPaymentTermUnit(1); // 年
            }
        } else {
            premium.setPremiumPlanCode("DEFAULT");
            premium.setPaymentFrequency(1);
            premium.setPaymentTerm(1);
            premium.setPaymentTermUnit(1);
        }

        premium.setPeriodPremium(proposal.getTotalPremium() != null ? proposal.getTotalPremium() : BigDecimal.ZERO);
        premium.setTotalPeriods(premium.getPaymentTerm() != null ? premium.getPaymentTerm() : 1);
        premium.setPaidPeriods(1); // 首期已缴
        premium.setNextDueDate(LocalDate.now().plusYears(1));
        premium.setGracePeriod(60);

        policyPremiumMapper.insert(premium);
        premiumEntities.add(premium);

        ctx.setPolicyPremiums(premiumEntities);
        log.info("Policy premium plan created: policyId={}, planCode={}",
                policy.getId(), premium.getPremiumPlanCode());
    }
}
