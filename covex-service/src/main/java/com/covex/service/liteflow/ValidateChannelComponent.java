package com.covex.service.liteflow;

import com.covex.service.entity.ChannelEntity;
import com.covex.service.entity.ProposalEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * 校验渠道商状态和合同有效期
 * <p>注意：产品授权检查（ins_channel_product）不在此组件中，应在业务层单独校验。</p>
 */
@LiteflowComponent("validateChannel")
public class ValidateChannelComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateChannelComponent.class);

    /** 已签约（有效状态，允许投保） */
    private static final int CHANNEL_STATUS_SIGNED = 2;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        if (ctx == null || ctx.getProposal() == null) {
            throw new IllegalStateException("ValidateChannelComponent: 缺少流程上下文或投保单");
        }

        ProposalEntity proposal = ctx.getProposal();

        // 直销渠道（channelId 为空），跳过校验
        if (proposal.getChannelId() == null) {
            log.debug("Direct sale, skip channel validation");
            return;
        }

        ChannelEntity channel = ctx.getChannel();
        if (channel == null) {
            ctx.addError("渠道商不存在: channelId=" + proposal.getChannelId());
            return;
        }

        // 检查渠道商状态（仅已签约允许投保）
        if (channel.getStatus() == null || channel.getStatus() != CHANNEL_STATUS_SIGNED) {
            ctx.addError("渠道商状态异常(当前=" + statusName(channel.getStatus()) + "): " + channel.getChannelName());
        }

        // 检查合同是否已生效
        if (channel.getContractStart() != null && channel.getContractStart().isAfter(LocalDate.now())) {
            ctx.addError("渠道商合同尚未生效: " + channel.getChannelName());
        }

        // 检查合同是否已过期
        if (channel.getContractEnd() != null && channel.getContractEnd().isBefore(LocalDate.now())) {
            ctx.addError("渠道商合同已过期: " + channel.getChannelName());
        }
    }

    private String statusName(Integer status) {
        if (status == null) return "null";
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已签约";
            case 3 -> "已暂停";
            case 4 -> "已终止";
            default -> "未知(" + status + ")";
        };
    }
}
