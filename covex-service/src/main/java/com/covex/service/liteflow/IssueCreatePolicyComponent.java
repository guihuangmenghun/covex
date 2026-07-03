package com.covex.service.liteflow;

import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PolicyMapper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 出单 — 创建保单主表（从投保单实例化）
 */
@LiteflowComponent("issueCreatePolicy")
public class IssueCreatePolicyComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueCreatePolicyComponent.class);

    @Autowired
    private PolicyMapper policyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();

        PolicyEntity policy = new PolicyEntity();
        policy.setTenantId(proposal.getTenantId());
        policy.setPolicyNo(generatePolicyNo());
        policy.setProposalId(proposal.getId());
        policy.setProductId(proposal.getProductId());
        policy.setChannelId(proposal.getChannelId());
        policy.setApplicantId(proposal.getApplicantId());
        policy.setInsuredId(proposal.getInsuredId());
        policy.setProductSnapshot(proposal.getProductSnapshot());
        policy.setEffectiveDate(LocalDate.now());
        // 默认保障 1 年
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        policy.setTotalPremium(proposal.getTotalPremium());
        policy.setTotalSumInsured(proposal.getTotalSumInsured());
        policy.setPaymentMode(1); // 趸交
        policy.setStatus(1); // 有效

        policyMapper.insert(policy);
        ctx.setPolicy(policy);

        log.info("Policy created: policyNo={}, proposalId={}", policy.getPolicyNo(), proposal.getId());
    }

    /**
     * 保单号生成 — Redis INCR 保证全局递增唯一
     * 格式：{tenant_code}{yyyy}{6位流水号}，例如 T0012026000001
     */
    private String generatePolicyNo() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = "T001" + year;
        Long seq = stringRedisTemplate.opsForValue().increment("policy_no:" + year);
        return prefix + String.format("%06d", seq);
    }
}
