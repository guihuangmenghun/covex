package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.dto.CreatePaymentDTO;
import com.covex.service.dto.PaymentCallbackDTO;
import com.covex.service.entity.PaymentEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PaymentMapper;
import com.covex.service.mapper.ProposalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支付服务
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final AtomicLong PAYMENT_SEQ = new AtomicLong(1);

    private final PaymentMapper paymentMapper;
    private final ProposalMapper proposalMapper;
    private final PolicyService policyService;

    public PaymentService(PaymentMapper paymentMapper,
                          ProposalMapper proposalMapper,
                          PolicyService policyService) {
        this.paymentMapper = paymentMapper;
        this.proposalMapper = proposalMapper;
        this.policyService = policyService;
    }

    /**
     * 创建支付记录（Mock 支付通道）
     */
    @Transactional
    public PaymentEntity createPayment(CreatePaymentDTO dto) {
        ProposalEntity proposal = proposalMapper.selectById(dto.getProposalId());
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + dto.getProposalId());
        }
        if (proposal.getStatus() != 4) {
            throw new BizException("只有待支付状态的投保单可以创建支付，当前状态: " + proposal.getStatus());
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setTenantId(proposal.getTenantId());
        payment.setPaymentNo(generatePaymentNo(proposal.getTenantId()));
        payment.setProposalId(dto.getProposalId());
        payment.setPaymentType(1); // 首期保费
        payment.setAmount(proposal.getTotalPremium());
        payment.setPayChannel(dto.getPayChannel());
        payment.setStatus(1); // 待支付

        payment.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        paymentMapper.insert(payment);
        log.info("Payment created: paymentNo={}, amount={}, channel={}",
                payment.getPaymentNo(), payment.getAmount(), dto.getPayChannel());
        return payment;
    }

    /**
     * 支付回调处理
     * - 幂等（payment_no 去重）
     * - 金额校验（不匹配→status=4 挂起）
     * - 更新状态→触发后续流程
     */
    @Transactional
    public PaymentEntity handlePaymentCallback(PaymentCallbackDTO dto) {
        // 查找支付记录
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getPaymentNo, dto.getPaymentNo());
        PaymentEntity payment = paymentMapper.selectOne(wrapper);

        if (payment == null) {
            throw new BizException(404, "支付记录不存在: " + dto.getPaymentNo());
        }

        // 幂等：已支付则直接返回
        if (payment.getStatus() == 2) {
            log.info("Payment already paid, idempotent return: paymentNo={}", dto.getPaymentNo());
            return payment;
        }

        // 金额校验
        if (dto.getAmount() != null && payment.getAmount().compareTo(dto.getAmount()) != 0) {
            payment.setStatus(4); // 挂起
            payment.setPayChannelNo(dto.getPayChannelNo());
            paymentMapper.updateById(payment);
            throw new BizException("支付金额不匹配: 期望=" + payment.getAmount() + ", 实际=" + dto.getAmount());
        }

        // 更新为已支付
        payment.setStatus(2);
        payment.setPayChannelNo(dto.getPayChannelNo());
        payment.setPaidAt(LocalDateTime.now());
        payment.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        paymentMapper.updateById(payment);

        // 更新投保单状态 → 已支付
        ProposalEntity proposal = proposalMapper.selectById(payment.getProposalId());
        if (proposal != null && proposal.getStatus() == 4) {
            proposal.setStatus(5);
            proposalMapper.updateById(proposal);
            log.info("Proposal status → 已支付: proposalId={}", proposal.getId());
        }

        log.info("Payment callback handled: paymentNo={}, payChannelNo={}",
                dto.getPaymentNo(), dto.getPayChannelNo());
        return payment;
    }

    /**
     * 按投保单查询支付记录
     */
    public List<PaymentEntity> queryPaymentByProposal(Long proposalId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getProposalId, proposalId)
               .orderByDesc(PaymentEntity::getCreatedAt);
        return paymentMapper.selectList(wrapper);
    }

    /**
     * 定时任务：扫描超时投保单 → 自动撤销
     * 超过30分钟未支付的投保单自动撤销
     */
    @Transactional
    public int handlePaymentTimeout() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        LambdaQueryWrapper<ProposalEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProposalEntity::getStatus, 4) // 待支付
               .lt(ProposalEntity::getSubmitAt, threshold);
        List<ProposalEntity> expiredProposals = proposalMapper.selectList(wrapper);

        int count = 0;
        for (ProposalEntity proposal : expiredProposals) {
            proposal.setStatus(8); // 已撤销
            proposalMapper.updateById(proposal);
            count++;
            log.info("Proposal timeout cancelled: proposalNo={}", proposal.getProposalNo());
        }

        log.info("Payment timeout scan completed: cancelled={}", count);
        return count;
    }

    private String generatePaymentNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = System.nanoTime() % 1000000;
        return String.format("PAY%02d%s%06d", tenantId != null ? tenantId : 0, dateStr, seq);
    }
}
