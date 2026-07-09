package com.covex.service.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.ChannelProductEntity;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.ChannelProductMapper;
import com.covex.service.mapper.PolicyMapper;
import com.covex.service.mapper.ProposalMapper;
import com.covex.service.service.CommissionService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 出单消息消费者 — 自动计算佣金
 * 消息格式：policyNo=xxx, policyId=123, premium=100.00, sumInsured=50000.00
 */
@Service
@RocketMQMessageListener(topic = "POLICY_ISSUED", consumerGroup = "covex-commission-consumer")
public class PolicyIssuedConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(PolicyIssuedConsumer.class);
    private static final Pattern MSG_PATTERN = Pattern.compile(
            "policyNo=(.+?),\\s*policyId=(\\d+),\\s*premium=([\\d.]+),\\s*sumInsured=([\\d.]+)");

    private final PolicyMapper policyMapper;
    private final ProposalMapper proposalMapper;
    private final ChannelProductMapper channelProductMapper;
    private final CommissionService commissionService;

    public PolicyIssuedConsumer(PolicyMapper policyMapper,
                                ProposalMapper proposalMapper,
                                ChannelProductMapper channelProductMapper,
                                CommissionService commissionService) {
        this.policyMapper = policyMapper;
        this.proposalMapper = proposalMapper;
        this.channelProductMapper = channelProductMapper;
        this.commissionService = commissionService;
    }

    @Override
    public void onMessage(String message) {
        log.info("POLICY_ISSUED received: {}", message);

        Matcher matcher = MSG_PATTERN.matcher(message);
        if (!matcher.find()) {
            log.error("POLICY_ISSUED message format invalid: {}", message);
            return;
        }

        String policyNo = matcher.group(1);
        Long policyId = Long.valueOf(matcher.group(2));

        // 查保单获取 proposalId
        PolicyEntity policy = policyMapper.selectById(policyId);
        if (policy == null) {
            log.error("Policy not found: policyId={}", policyId);
            return;
        }

        // 查投保单获取渠道信息
        ProposalEntity proposal = proposalMapper.selectById(policy.getProposalId());
        if (proposal == null) {
            log.error("Proposal not found: proposalId={}", policy.getProposalId());
            return;
        }

        // 查渠道产品佣金率
        // 无渠道商的投保单不计算佣金（直接出单的场景）
        if (proposal.getChannelId() == null) {
            log.info("No channel associated, skip commission: policyNo={}, proposalId={}",
                    policyNo, proposal.getId());
            return;
        }

        LambdaQueryWrapper<ChannelProductEntity> cpWrapper = new LambdaQueryWrapper<>();
        cpWrapper.eq(ChannelProductEntity::getChannelId, proposal.getChannelId())
                 .eq(ChannelProductEntity::getProductId, proposal.getProductId());
        ChannelProductEntity channelProduct = channelProductMapper.selectOne(cpWrapper);

        BigDecimal firstYearRate = (channelProduct != null && channelProduct.getFirstYearRate() != null)
                ? channelProduct.getFirstYearRate()
                : BigDecimal.ZERO;

        // 计算佣金（幂等，commissionNo = policyId + "-" + commissionType 去重）
        Long tenantId = policy.getTenantId();
        commissionService.calculateCommission(
                tenantId,
                policyId,
                proposal.getChannelId(),
                proposal.getChannelUserId(),
                proposal.getTotalPremium(),
                1, // 首年佣金
                firstYearRate
        );

        log.info("Commission calculated for policyNo={}, policyId={}", policyNo, policyId);
    }
}
