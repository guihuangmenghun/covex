package com.covex.service.liteflow;

import com.covex.service.entity.PolicyEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出单 — 发送 MQ 通知（RocketMQ POLICY_ISSUED 消息）
 */
@LiteflowComponent("issueNotify")
public class IssueNotifyComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueNotifyComponent.class);

    private final RocketMQTemplate rocketMQTemplate;

    public IssueNotifyComponent(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        PolicyEntity policy = ctx.getPolicy();

        // 发送 POLICY_ISSUED 消息到 RocketMQ
        String message = String.format("policyNo=%s, policyId=%d, premium=%s, sumInsured=%s",
                policy.getPolicyNo(), policy.getId(),
                policy.getTotalPremium(), policy.getTotalSumInsured());

        rocketMQTemplate.convertAndSend("POLICY_ISSUED", message);
        log.info("POLICY_ISSUED event sent to MQ: policyNo={}, policyId={}",
                policy.getPolicyNo(), policy.getId());
    }
}
