package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.PaymentEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PaymentMapper;
import com.covex.service.mapper.ProposalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付补偿服务 — 兜底处理超时未支付的投保单
 * 每天凌晨 2 点执行，扫描超过 24 小时仍待支付的记录
 */
@Service
public class PaymentCompensationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompensationService.class);

    private final PaymentMapper paymentMapper;
    private final ProposalMapper proposalMapper;

    public PaymentCompensationService(PaymentMapper paymentMapper,
                                      ProposalMapper proposalMapper) {
        this.paymentMapper = paymentMapper;
        this.proposalMapper = proposalMapper;
    }

    /**
     * 每天凌晨 2 点扫描超时支付记录
     * 将超过 24 小时仍待支付的记录标记为挂起，对应投保单撤销
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void compensatePaymentTimeout() {
        log.info("Payment compensation scan started");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        // 查找超过 24 小时仍待支付的记录
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getStatus, 1) // 待支付
               .lt(PaymentEntity::getCreatedAt, threshold);
        List<PaymentEntity> expiredPayments = paymentMapper.selectList(wrapper);

        int count = 0;
        for (PaymentEntity payment : expiredPayments) {
            // 标记支付记录为挂起
            payment.setStatus(5); // 挂起
            paymentMapper.updateById(payment);

            // 撤销对应投保单
            if (payment.getProposalId() != null) {
                ProposalEntity proposal = proposalMapper.selectById(payment.getProposalId());
                if (proposal != null && proposal.getStatus() == 4) {
                    proposal.setStatus(8); // 已撤销
                    proposalMapper.updateById(proposal);
                }
            }
            count++;
            log.info("Compensation: payment {} marked as suspended, proposal cancelled",
                    payment.getPaymentNo());
        }

        log.info("Payment compensation scan completed: {} records processed", count);
    }
}
