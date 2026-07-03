package com.covex.service.liteflow;

import com.covex.service.entity.PolicyEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 出单 — 发送 MQ 通知（Mock 实现，RocketMQ 未启动时仅打日志）
 */
@LiteflowComponent("issueNotify")
public class IssueNotifyComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueNotifyComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        PolicyEntity policy = ctx.getPolicy();

        // RocketMQ 消息发送（当前 Mock，因为 NameServer 未启动）
        log.info("POLICY_ISSUED event: policyNo={}, policyId={}, premium={}, sumInsured={}",
                policy.getPolicyNo(), policy.getId(),
                policy.getTotalPremium(), policy.getTotalSumInsured());

        // TODO: 当 RocketMQ 可用时，发送真实消息
        // rocketMQTemplate.convertAndSend("POLICY_ISSUED", policy);
    }
}
