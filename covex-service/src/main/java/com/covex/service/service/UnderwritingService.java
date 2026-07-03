package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.common.exception.BizException;
import com.covex.service.dto.ManualUnderwriteDTO;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.entity.UnderwritingRecordEntity;
import com.covex.service.mapper.PolicyMapper;
import com.covex.service.mapper.ProposalMapper;
import com.covex.service.mapper.UnderwritingRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 核保服务
 */
@Service
public class UnderwritingService {

    private static final Logger log = LoggerFactory.getLogger(UnderwritingService.class);

    private final UnderwritingRecordMapper uwRecordMapper;
    private final ProposalMapper proposalMapper;
    private final PolicyMapper policyMapper;

    public UnderwritingService(UnderwritingRecordMapper uwRecordMapper,
                               ProposalMapper proposalMapper,
                               PolicyMapper policyMapper) {
        this.uwRecordMapper = uwRecordMapper;
        this.proposalMapper = proposalMapper;
        this.policyMapper = policyMapper;
    }

    /**
     * 自动核保 — 由 LiteFlow underwrite 链驱动（已在 ProposalService.submitProposal 中调用）
     * 此方法用于保存自动核保的结论记录
     */
    @Transactional
    public UnderwritingRecordEntity saveAutoUwResult(Long proposalId, int uwResult,
                                                      BigDecimal loadingAmount,
                                                      String exclusionDesc, String comment) {
        UnderwritingRecordEntity record = new UnderwritingRecordEntity();
        record.setTenantId(0L);
        record.setProposalId(proposalId);
        record.setUwType(1); // 自动核保
        record.setUwResult(uwResult);
        record.setLoadingAmount(loadingAmount);
        record.setExclusionDesc(exclusionDesc);
        record.setUwComment(comment);
        record.setUwOperator("system");
        record.setUwAt(LocalDateTime.now());
        uwRecordMapper.insert(record);

        log.info("Auto UW result saved: proposalId={}, result={}", proposalId, uwResult);
        return record;
    }

    /**
     * 人工核保
     */
    @Transactional
    public UnderwritingRecordEntity manualUnderwrite(Long proposalId, ManualUnderwriteDTO dto) {
        ProposalEntity proposal = proposalMapper.selectById(proposalId);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + proposalId);
        }
        if (proposal.getStatus() != 3 && proposal.getStatus() != 2) {
            throw new BizException("只有核保中/待核保状态的投保单可以人工核保，当前状态: " + proposal.getStatus());
        }

        UnderwritingRecordEntity record = new UnderwritingRecordEntity();
        record.setTenantId(proposal.getTenantId());
        record.setProposalId(proposalId);
        record.setUwType(2); // 人工核保
        record.setUwResult(dto.getUwResult());
        record.setLoadingAmount(dto.getLoadingAmount());
        record.setExclusionDesc(dto.getExclusionDesc());
        record.setUwComment(dto.getComment());
        record.setUwOperator(dto.getOperator());
        record.setUwAt(LocalDateTime.now());
        uwRecordMapper.insert(record);

        // 根据核保结论更新投保单状态
        if (dto.getUwResult() == 1 || dto.getUwResult() == 2) {
            // 标准体或加费 → 待支付
            proposal.setStatus(4);
        } else if (dto.getUwResult() == 5) {
            // 拒保
            proposal.setStatus(7);
        } else {
            // 延期/除外 → 保持核保中或待支付
            proposal.setStatus(4);
        }

        proposalMapper.updateById(proposal);
        log.info("Manual UW completed: proposalId={}, result={}, operator={}",
                proposalId, dto.getUwResult(), dto.getOperator());
        return record;
    }

    /**
     * 查询核保记录
     */
    public List<UnderwritingRecordEntity> getUnderwritingRecords(Long proposalId) {
        LambdaQueryWrapper<UnderwritingRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnderwritingRecordEntity::getProposalId, proposalId)
               .orderByDesc(UnderwritingRecordEntity::getCreatedAt);
        return uwRecordMapper.selectList(wrapper);
    }

    /**
     * 查询被保人累计风险保额（带降级处理）
     * 查询该被保人所有有效保单的总保额
     */
    public BigDecimal getCumulativeSumInsured(Long insuredId) {
        try {
            LambdaQueryWrapper<PolicyEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PolicyEntity::getInsuredId, insuredId)
                   .eq(PolicyEntity::getStatus, 1); // 有效保单
            List<PolicyEntity> policies = policyMapper.selectList(wrapper);

            BigDecimal total = BigDecimal.ZERO;
            for (PolicyEntity p : policies) {
                if (p.getTotalSumInsured() != null) {
                    total = total.add(p.getTotalSumInsured());
                }
            }
            return total;
        } catch (Exception e) {
            log.warn("Failed to query cumulative sum insured for insuredId={}, degrade to ZERO: {}",
                    insuredId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
