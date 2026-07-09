package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.dto.CreatePaymentDTO;
import com.covex.service.dto.PaymentCallbackDTO;
import com.covex.service.entity.PaymentEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PaymentMapper;
import com.covex.service.mapper.ProposalMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentMapper paymentMapper;
    private final ProposalMapper proposalMapper;
    private final PolicyService policyService;
    private final RocketMQTemplate rocketMQTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    public PaymentService(PaymentMapper paymentMapper,
                          ProposalMapper proposalMapper,
                          PolicyService policyService,
                          RocketMQTemplate rocketMQTemplate,
                          StringRedisTemplate redisTemplate,
                          RedissonClient redissonClient) {
        this.paymentMapper = paymentMapper;
        this.proposalMapper = proposalMapper;
        this.policyService = policyService;
        this.rocketMQTemplate = rocketMQTemplate;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 创建支付记录 + 发送 30 分钟延迟消息用于超时自动撤销
     */
    @Transactional(rollbackFor = Exception.class)
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

        // 发送 30 分钟延迟消息用于支付超时自动撤销（MQ 不可用时仅警告，不影响支付创建）
        try {
            String mqMessage = String.format("paymentId=%d, proposalId=%d", payment.getId(), proposal.getId());
            rocketMQTemplate.syncSend("PAYMENT_TIMEOUT",
                    MessageBuilder.withPayload(mqMessage).build(),
                    3000, // 发送超时 3s
                    16);  // delayLevel 16 = 30 分钟（RocketMQ 默认延迟级别）
            log.info("Payment created and timeout MQ sent: paymentNo={}, amount={}, channel={}",
                    payment.getPaymentNo(), payment.getAmount(), dto.getPayChannel());
        } catch (Exception e) {
            log.warn("Payment timeout MQ send failed (non-critical): paymentNo={}, error={}",
                    payment.getPaymentNo(), e.getMessage());
        }
        return payment;
    }

    /**
     * 支付回调处理（Redisson 分布式锁防重复回调）
     * - 幂等（payment_no 去重）
     * - 金额校验（不匹配→status=4 挂起）
     * - 更新状态→触发后续流程
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentEntity handlePaymentCallback(PaymentCallbackDTO dto) {
        RLock lock = redissonClient.getLock("lock:payment:callback:" + dto.getPaymentNo());
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new BizException("支付回调处理中，请稍后重试");
            }
            try {
                return doHandlePaymentCallback(dto);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("支付回调处理被中断");
        }
    }

    private PaymentEntity doHandlePaymentCallback(PaymentCallbackDTO dto) {
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

            // 发送 PROPOSAL_PAID 消息，异步触发出单链（需求规格 AC-3）
            try {
                String mqMessage = String.format("proposalId=%d", proposal.getId());
                rocketMQTemplate.convertAndSend("PROPOSAL_PAID", mqMessage);
                log.info("PROPOSAL_PAID MQ sent: proposalId={}", proposal.getId());
            } catch (Exception e) {
                log.error("Failed to send PROPOSAL_PAID MQ: proposalId={}, error={}", proposal.getId(), e.getMessage());
            }
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
    @Transactional(rollbackFor = Exception.class)
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
        String redisKey = "payment_no:" + dateStr;
        Long seq = redisTemplate.opsForValue().increment(redisKey);
        return String.format("PAY%02d%s%06d", tenantId != null ? tenantId : 0, dateStr, seq);
    }
}
