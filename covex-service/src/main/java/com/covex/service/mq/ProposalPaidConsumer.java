package com.covex.service.mq;

import com.covex.service.service.PolicyService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 投保单已支付消息消费者 — 异步触发出单链
 * 消息格式：proposalId=123
 *
 * 触发链路：PaymentService.handlePaymentCallback() → PROPOSAL_PAID MQ → 本消费者 → PolicyService.issuePolicy()
 * 失败处理：投保单保持 status=5（已支付），人工通过 POST /api/policy/issue/{proposalId} 重试
 */
@Service
@RocketMQMessageListener(topic = "PROPOSAL_PAID", consumerGroup = "covex-proposal-paid-consumer")
public class ProposalPaidConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(ProposalPaidConsumer.class);
    private static final Pattern MSG_PATTERN = Pattern.compile("proposalId=(\\d+)");

    private final PolicyService policyService;

    public ProposalPaidConsumer(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Override
    public void onMessage(String message) {
        log.info("PROPOSAL_PAID received: {}", message);

        Matcher matcher = MSG_PATTERN.matcher(message);
        if (!matcher.find()) {
            log.error("PROPOSAL_PAID message format invalid: {}", message);
            return;
        }

        Long proposalId = Long.valueOf(matcher.group(1));

        try {
            policyService.issuePolicy(proposalId);
            log.info("Policy issued successfully via MQ: proposalId={}", proposalId);
        } catch (Exception e) {
            log.error("Failed to issue policy for proposalId={}, status remains 5 (已支付). Manual retry: POST /api/policy/issue/{}",
                    proposalId, proposalId, e);
        }
    }
}
