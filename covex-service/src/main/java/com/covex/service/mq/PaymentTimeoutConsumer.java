package com.covex.service.mq;

import com.covex.service.entity.PaymentEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PaymentMapper;
import com.covex.service.mapper.ProposalMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付超时消费者 — 延迟消息到期后检查支付状态，未支付则撤销投保单
 * 消息格式：paymentId=123, proposalId=456
 */
@Service
@RocketMQMessageListener(topic = "PAYMENT_TIMEOUT", consumerGroup = "covex-payment-timeout-consumer")
public class PaymentTimeoutConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(PaymentTimeoutConsumer.class);

    private final PaymentMapper paymentMapper;
    private final ProposalMapper proposalMapper;

    public PaymentTimeoutConsumer(PaymentMapper paymentMapper,
                                  ProposalMapper proposalMapper) {
        this.paymentMapper = paymentMapper;
        this.proposalMapper = proposalMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String message) {
        log.info("PAYMENT_TIMEOUT received: {}", message);

        try {
            // 解析消息
            Long paymentId = null;
            Long proposalId = null;
            String[] parts = message.split(",\\s*");
            for (String part : parts) {
                String[] kv = part.split("=");
                if (kv.length == 2) {
                    if ("paymentId".equals(kv[0].trim())) {
                        paymentId = Long.valueOf(kv[1].trim());
                    } else if ("proposalId".equals(kv[0].trim())) {
                        proposalId = Long.valueOf(kv[1].trim());
                    }
                }
            }

            if (paymentId == null || proposalId == null) {
                log.error("PAYMENT_TIMEOUT message format invalid: {}", message);
                return;
            }

            // 查支付记录
            PaymentEntity payment = paymentMapper.selectById(paymentId);
            if (payment == null) {
                log.warn("Payment not found: paymentId={}", paymentId);
                return;
            }

            // 幂等：已支付则忽略
            if (payment.getStatus() == 2) {
                log.info("Payment already paid, ignore timeout: paymentId={}", paymentId);
                return;
            }

            // 仍为待支付 -> 撤销投保单
            if (payment.getStatus() == 1) {
                ProposalEntity proposal = proposalMapper.selectById(proposalId);
                if (proposal != null && proposal.getStatus() == 4) {
                    proposal.setStatus(8); // 已撤销
                    proposalMapper.updateById(proposal);
                    log.info("Proposal cancelled due to payment timeout: proposalId={}", proposalId);
                }

                // 支付记录标记为超时
                payment.setStatus(5); // 超时
                paymentMapper.updateById(payment);
                log.info("Payment marked as timeout: paymentId={}", paymentId);
            }
        } catch (Exception e) {
            log.error("PAYMENT_TIMEOUT processing error: {}", e.getMessage(), e);
            throw e; // 重试
        }
    }
}
