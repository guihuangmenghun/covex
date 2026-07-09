package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.entity.CommissionEntity;
import com.covex.service.mapper.CommissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 佣金服务
 */
@Service
public class CommissionService {

    private static final Logger log = LoggerFactory.getLogger(CommissionService.class);

    private final CommissionMapper commissionMapper;

    public CommissionService(CommissionMapper commissionMapper) {
        this.commissionMapper = commissionMapper;
    }

    /**
     * 计算佣金 — 幂等（commission_no 去重）
     * commission_no = policyId + "-" + commissionType
     */
    @Transactional(rollbackFor = Exception.class)
    public CommissionEntity calculateCommission(Long tenantId, Long policyId, Long channelId,
                                                 Long channelUserId, BigDecimal premiumAmount,
                                                 Integer commissionType, BigDecimal commissionRate) {
        // 无渠道商不算佣金
        if (channelId == null) {
            log.info("Skip commission: channelId is null, policyId={}", policyId);
            return null;
        }

        String commissionNo = policyId + "-" + commissionType;

        // Idempotency check
        LambdaQueryWrapper<CommissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionEntity::getTenantId, tenantId)
               .eq(CommissionEntity::getCommissionNo, commissionNo);
        CommissionEntity existing = commissionMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("Commission already exists (idempotent): {}", commissionNo);
            return existing;
        }

        // Calculate commission amount: premium * rate / 100
        BigDecimal amount = premiumAmount.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Determine settle month (current month)
        String settleMonth = LocalDateTime.now().toString().substring(0, 7);

        CommissionEntity entity = new CommissionEntity();
        entity.setTenantId(tenantId);
        entity.setChannelId(channelId);
        entity.setChannelUserId(channelUserId);
        entity.setPolicyId(policyId);
        entity.setCommissionType(commissionType);
        entity.setPremiumAmount(premiumAmount);
        entity.setCommissionRate(commissionRate);
        entity.setCommissionAmount(amount);
        entity.setSettleMonth(settleMonth);
        entity.setSettleStatus(1); // 待结算
        entity.setCommissionNo(commissionNo);

        entity.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        commissionMapper.insert(entity);
        log.info("Commission calculated: no={}, amount={}", commissionNo, amount);
        return entity;
    }

    /**
     * 按条件查询佣金列表
     */
    public List<CommissionEntity> listCommissions(Long channelId, String month, Integer status) {
        LambdaQueryWrapper<CommissionEntity> wrapper = new LambdaQueryWrapper<>();
        if (channelId != null) {
            wrapper.eq(CommissionEntity::getChannelId, channelId);
        }
        if (month != null && !month.isEmpty()) {
            wrapper.eq(CommissionEntity::getSettleMonth, month);
        }
        if (status != null) {
            wrapper.eq(CommissionEntity::getSettleStatus, status);
        }
        wrapper.orderByDesc(CommissionEntity::getCreatedAt);
        return commissionMapper.selectList(wrapper);
    }

    /**
     * 月度结算 — 将该月份所有待结算佣金状态变更为已确认
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> monthlySettle(String yearMonth) {
        LambdaQueryWrapper<CommissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionEntity::getSettleMonth, yearMonth)
               .eq(CommissionEntity::getSettleStatus, 1);
        List<CommissionEntity> pendingList = commissionMapper.selectList(wrapper);

        if (pendingList.isEmpty()) {
            throw new BizException("该月份无待结算佣金: " + yearMonth);
        }

        String operator = com.covex.common.util.OperatorContext.getCurrentOperator();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int count = 0;
        for (CommissionEntity c : pendingList) {
            CommissionEntity update = new CommissionEntity();
            update.setId(c.getId());
            update.setSettleStatus(2); // 已确认
            update.setSettledAt(LocalDateTime.now());
            update.setOperator(operator);
            commissionMapper.updateById(update);
            totalAmount = totalAmount.add(c.getCommissionAmount());
            count++;
        }

        log.info("Monthly settle completed: month={}, count={}, total={}, operator={}", yearMonth, count, totalAmount, operator);

        Map<String, Object> result = new HashMap<>();
        result.put("yearMonth", yearMonth);
        result.put("settledCount", count);
        result.put("totalAmount", totalAmount);
        return result;
    }

    /**
     * 确认支付 — 将已确认的佣金标记为已支付
     */
    @Transactional(rollbackFor = Exception.class)
    public CommissionEntity confirmSettle(Long commissionId) {
        CommissionEntity existing = commissionMapper.selectById(commissionId);
        if (existing == null) {
            throw new BizException(404, "佣金记录不存在: " + commissionId);
        }
        if (existing.getSettleStatus() != 2) {
            throw new BizException("佣金非已确认状态，无法支付");
        }

        CommissionEntity update = new CommissionEntity();
        update.setId(commissionId);
        update.setSettleStatus(3); // 已支付
        update.setSettledAt(LocalDateTime.now());
        update.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        commissionMapper.updateById(update);

        log.info("Commission confirmed/paid: id={}", commissionId);
        return commissionMapper.selectById(commissionId);
    }

    /**
     * 驳回佣金 — 将已确认的佣金退回待结算
     */
    @Transactional(rollbackFor = Exception.class)
    public CommissionEntity rejectSettle(Long commissionId) {
        CommissionEntity existing = commissionMapper.selectById(commissionId);
        if (existing == null) {
            throw new BizException(404, "佣金记录不存在: " + commissionId);
        }
        if (existing.getSettleStatus() != 2) {
            throw new BizException("佣金非已确认状态，无法驳回");
        }

        CommissionEntity update = new CommissionEntity();
        update.setId(commissionId);
        update.setSettleStatus(4); // 已驳回
        update.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        commissionMapper.updateById(update);

        log.info("Commission rejected: id={}", commissionId);
        return commissionMapper.selectById(commissionId);
    }

    /**
     * 月度汇总统计
     */
    public Map<String, Object> getMonthlySummary(Long channelId, String yearMonth) {
        LambdaQueryWrapper<CommissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionEntity::getChannelId, channelId)
               .eq(CommissionEntity::getSettleMonth, yearMonth);
        List<CommissionEntity> list = commissionMapper.selectList(wrapper);

        BigDecimal totalPremium = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        int pendingCount = 0;
        int confirmedCount = 0;
        int paidCount = 0;

        for (CommissionEntity c : list) {
            totalPremium = totalPremium.add(c.getPremiumAmount());
            totalCommission = totalCommission.add(c.getCommissionAmount());
            switch (c.getSettleStatus()) {
                case 1 -> pendingCount++;
                case 2 -> confirmedCount++;
                case 3 -> paidCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("channelId", channelId);
        summary.put("yearMonth", yearMonth);
        summary.put("totalCount", list.size());
        summary.put("totalPremium", totalPremium);
        summary.put("totalCommission", totalCommission);
        summary.put("pendingCount", pendingCount);
        summary.put("confirmedCount", confirmedCount);
        summary.put("paidCount", paidCount);
        return summary;
    }
}
