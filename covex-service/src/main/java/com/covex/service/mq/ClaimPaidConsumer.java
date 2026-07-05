package com.covex.service.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 赔付通知消费者 — 记录赔付通知日志
 * 消息格式：claimId=123, paidAt=2026-07-05T10:00:00
 * 当前阶段仅需日志，后续可扩展短信/邮件通知
 */
@Service
@RocketMQMessageListener(topic = "CLAIM_PAID", consumerGroup = "covex-claim-paid-consumer")
public class ClaimPaidConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(ClaimPaidConsumer.class);

    @Override
    public void onMessage(String message) {
        log.info("CLAIM_PAID received: {}", message);
        // 当前阶段仅记录日志
        // 后续可扩展：短信通知、邮件通知、推送给渠道商等
        log.info("Claim payment notification logged successfully");
    }
}
