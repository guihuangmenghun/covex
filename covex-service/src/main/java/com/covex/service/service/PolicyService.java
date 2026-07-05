package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.service.entity.*;
import com.covex.service.liteflow.CovexFlowContext;
import com.covex.service.mapper.*;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 保单服务
 */
@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    /**
     * 保单状态机
     * 1-有效 → 2-中止, 3-终止
     * 2-中止 → 1-有效（复效）, 3-终止
     * 3-终止 (终态)
     */
    private static final Map<Integer, Set<Integer>> VALID_TRANSITIONS = Map.of(
            1, Set.of(2, 3),
            2, Set.of(1, 3),
            3, Set.of()
    );

    private final PolicyMapper policyMapper;
    private final PolicyCoverageMapper policyCoverageMapper;
    private final PolicyPremiumMapper policyPremiumMapper;
    private final ProposalMapper proposalMapper;
    private final FlowExecutor flowExecutor;

    public PolicyService(PolicyMapper policyMapper,
                         PolicyCoverageMapper policyCoverageMapper,
                         PolicyPremiumMapper policyPremiumMapper,
                         ProposalMapper proposalMapper,
                         FlowExecutor flowExecutor) {
        this.policyMapper = policyMapper;
        this.policyCoverageMapper = policyCoverageMapper;
        this.policyPremiumMapper = policyPremiumMapper;
        this.proposalMapper = proposalMapper;
        this.flowExecutor = flowExecutor;
    }

    /**
     * 出单 — 执行 LiteFlow issue 链
     * 1. 创建 ins_policy（从投保单实例化）
     * 2. 创建 ins_policy_coverage（逐行，含保额+保费）
     * 3. 创建 ins_policy_premium（缴费计划）
     * 4. 生成保单号
     * 5. 更新投保单 status=6
     * 6. 异步：触发 RocketMQ POLICY_ISSUED 消息
     */
    @Transactional(rollbackFor = Exception.class)
    public PolicyEntity issuePolicy(Long proposalId) {
        ProposalEntity proposal = proposalMapper.selectById(proposalId);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + proposalId);
        }
        if (proposal.getStatus() != 5) {
            throw new BizException("只有已支付状态的投保单可以出单，当前状态: " + proposal.getStatus());
        }

        // 构建上下文
        CovexFlowContext ctx = new CovexFlowContext();
        ctx.setProposal(proposal);

        // 执行 issue 链
        LiteflowResponse response = flowExecutor.execute2Resp("issueChain", null, ctx);
        if (!response.isSuccess()) {
            throw new BizException("出单链执行失败: " + response.getMessage());
        }

        PolicyEntity policy = ctx.getPolicy();
        if (policy == null) {
            throw new BizException("出单链执行异常: 未生成保单");
        }

        // 更新投保单状态 → 已出单
        proposal.setStatus(6);
        proposalMapper.updateById(proposal);

        log.info("Policy issued: policyNo={}, proposalNo={}", policy.getPolicyNo(), proposal.getProposalNo());
        return policy;
    }

    /**
     * 查询保单详情（含险种明细+缴费计划）
     */
    public Map<String, Object> getPolicyById(Long id) {
        PolicyEntity policy = policyMapper.selectById(id);
        if (policy == null) {
            throw new BizException(404, "保单不存在: " + id);
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("policy", policy);

        // 险种明细
        LambdaQueryWrapper<PolicyCoverageEntity> covWrapper = new LambdaQueryWrapper<>();
        covWrapper.eq(PolicyCoverageEntity::getPolicyId, id);
        detail.put("coverages", policyCoverageMapper.selectList(covWrapper));

        // 缴费计划
        LambdaQueryWrapper<PolicyPremiumEntity> premWrapper = new LambdaQueryWrapper<>();
        premWrapper.eq(PolicyPremiumEntity::getPolicyId, id);
        detail.put("premiums", policyPremiumMapper.selectList(premWrapper));

        return detail;
    }

    /**
     * 分页查询保单列表
     */
    public Page<PolicyEntity> listPolicies(String policyNo, Integer status, Long applicantId,
                                           int page, int size) {
        LambdaQueryWrapper<PolicyEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(policyNo)) {
            wrapper.like(PolicyEntity::getPolicyNo, policyNo);
        }
        if (status != null) {
            wrapper.eq(PolicyEntity::getStatus, status);
        }
        if (applicantId != null) {
            wrapper.eq(PolicyEntity::getApplicantId, applicantId);
        }
        wrapper.orderByDesc(PolicyEntity::getCreatedAt);
        return policyMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 更新保单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicyStatus(Long id, int newStatus, Integer terminationReason) {
        PolicyEntity policy = policyMapper.selectById(id);
        if (policy == null) {
            throw new BizException(404, "保单不存在: " + id);
        }

        Set<Integer> allowed = VALID_TRANSITIONS.getOrDefault(policy.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BizException("保单状态流转不合法: " + policyStatusName(policy.getStatus())
                    + " → " + policyStatusName(newStatus));
        }

        policy.setStatus(newStatus);
        if (newStatus == 3) {
            policy.setTerminationReason(terminationReason);
            policy.setTerminatedAt(LocalDateTime.now());
        }
        policyMapper.updateById(policy);

        log.info("Policy status updated: id={}, newStatus={}, reason={}",
                id, policyStatusName(newStatus), terminationReason);
    }

    private String policyStatusName(int status) {
        return switch (status) {
            case 1 -> "有效";
            case 2 -> "中止";
            case 3 -> "终止";
            default -> "未知(" + status + ")";
        };
    }
}
