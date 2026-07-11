package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.service.dto.CreateProposalDTO;
import com.covex.service.entity.*;
import com.covex.service.liteflow.CovexFlowContext;
import com.covex.service.mapper.*;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 投保单服务
 */
@Service
public class ProposalService {

    private static final Logger log = LoggerFactory.getLogger(ProposalService.class);

    /**
     * 投保单状态机：允许的流转
     * 1-待校验 → 2-待核保, 8-已撤销
     * 2-待核保 → 3-核保中, 8-已撤销
     * 3-核保中 → 4-待支付, 7-已拒保, 8-已撤销
     * 4-待支付 → 5-已支付, 8-已撤销
     * 5-已支付 → 6-已出单
     * 6-已出单 (终态)
     * 7-已拒保 (终态)
     * 8-已撤销 (终态)
     */
    private static final Map<Integer, Set<Integer>> VALID_TRANSITIONS = Map.of(
            1, Set.of(2, 8),
            2, Set.of(3, 8),
            3, Set.of(4, 7, 8),
            4, Set.of(5, 8),
            5, Set.of(6),
            6, Set.of(),
            7, Set.of(),
            8, Set.of()
    );

    private final ProposalMapper proposalMapper;
    private final ProductMapper productMapper;
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final CustomerInsuredMapper customerInsuredMapper;
    private final ChannelMapper channelMapper;
    private final UnderwritingRecordMapper uwRecordMapper;
    private final FlowExecutor flowExecutor;
    private final StringRedisTemplate redisTemplate;

    public ProposalService(ProposalMapper proposalMapper,
                           ProductMapper productMapper,
                           CustomerService customerService,
                           CustomerMapper customerMapper,
                           CustomerInsuredMapper customerInsuredMapper,
                           ChannelMapper channelMapper,
                           UnderwritingRecordMapper uwRecordMapper,
                           FlowExecutor flowExecutor,
                           StringRedisTemplate redisTemplate) {
        this.proposalMapper = proposalMapper;
        this.productMapper = productMapper;
        this.customerService = customerService;
        this.customerMapper = customerMapper;
        this.customerInsuredMapper = customerInsuredMapper;
        this.channelMapper = channelMapper;
        this.uwRecordMapper = uwRecordMapper;
        this.flowExecutor = flowExecutor;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建投保单（含产品快照深拷贝）
     */
    @Transactional(rollbackFor = Exception.class)
    public ProposalEntity createProposal(CreateProposalDTO dto) {
        // 验证产品存在
        ProductEntity product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BizException(404, "产品不存在: " + dto.getProductId());
        }

        ProposalEntity proposal = new ProposalEntity();
        proposal.setTenantId(dto.getTenantId() != null ? dto.getTenantId() : 0L);
        proposal.setProposalNo(generateProposalNo(proposal.getTenantId()));
        proposal.setProductId(dto.getProductId());
        proposal.setChannelId(dto.getChannelId());
        proposal.setChannelUserId(dto.getChannelUserId());
        proposal.setApplicantId(dto.getApplicantId());
        proposal.setInsuredId(dto.getInsuredId());
        proposal.setSelectedCoverages(dto.getSelectedCoverages());
        proposal.setSelectedPremiumPlan(dto.getSelectedPremiumPlan());
        proposal.setHealthDeclaration(dto.getHealthDeclaration());
        proposal.setTotalSumInsured(dto.getTotalSumInsured() != null ? dto.getTotalSumInsured() : BigDecimal.ZERO);
        proposal.setTotalPremium(BigDecimal.ZERO);
        proposal.setStatus(1); // 待校验

        // 产品快照深拷贝
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("productCode", product.getProductCode());
        snapshot.put("productName", product.getProductName());
        snapshot.put("shortName", product.getShortName());
        snapshot.put("productType", product.getProductType());
        snapshot.put("productNature", product.getProductNature());
        snapshot.put("termType", product.getTermType());
        snapshot.put("attributes", product.getAttributes());
        snapshot.put("capabilities", product.getCapabilities());
        snapshot.put("version", product.getVersion());
        proposal.setProductSnapshot(snapshot);

        proposal.setOperator(com.covex.common.util.OperatorContext.getCurrentOperator());
        proposalMapper.insert(proposal);
        log.info("Proposal created: proposalNo={}, productId={}", proposal.getProposalNo(), dto.getProductId());
        return proposal;
    }

    /**
     * 提交投保单 — 触发 validate 链，通过则触发 underwrite 链
     */
    @Transactional(rollbackFor = Exception.class)
    public ProposalEntity submitProposal(Long id) {
        ProposalEntity proposal = proposalMapper.selectById(id);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + id);
        }
        if (proposal.getStatus() != 1) {
            throw new BizException("只有待校验状态的投保单可以提交，当前状态: " + statusName(proposal.getStatus()));
        }

        // 构建上下文
        CovexFlowContext ctx = buildFlowContext(proposal);

        // 执行 validate 链
        LiteflowResponse validateResponse = flowExecutor.execute2Resp("validateChain", null, ctx);
        if (!validateResponse.isSuccess()) {
            proposal.setStatus(8); // 撤销
            proposalMapper.updateById(proposal);
            throw new BizException("校验链执行失败: " + String.join("; ", ctx.getErrors()));
        }

        if (!ctx.getErrors().isEmpty()) {
            proposal.setStatus(8);
            proposalMapper.updateById(proposal);
            throw new BizException("投保校验不通过: " + String.join("; ", ctx.getErrors()));
        }

        // 校验通过 → 待核保
        proposal.setStatus(2);
        proposal.setSubmitAt(LocalDateTime.now());
        proposalMapper.updateById(proposal);
        log.info("Proposal validation passed, status → 待核保: proposalNo={}", proposal.getProposalNo());

        // 触发核保链
        ctx.setErrors(new java.util.ArrayList<>());
        ctx.setPassed(true);
        LiteflowResponse uwResponse = flowExecutor.execute2Resp("underwriteChain", null, ctx);
        if (!uwResponse.isSuccess()) {
            log.warn("Underwrite chain execution error: {}", uwResponse.getMessage());
        }

        // 根据核保结论决定状态
        int finalResult = determineUwResult(ctx);
        if (finalResult == 1 || finalResult == 2) {
            // 标准体或加费 → 待支付
            proposal.setStatus(4);
        } else if (finalResult == 5) {
            // 拒保
            proposal.setStatus(7);
        } else if (finalResult == 6) {
            // 转人工 → 核保中
            proposal.setStatus(3);
        } else {
            // 延期或其他 → 核保中
            proposal.setStatus(3);
        }

        proposalMapper.updateById(proposal);

        // 保存自动核保记录到数据库
        String uwComment = ctx.getUwComments().isEmpty() ? "自动核保通过"
                : String.join("; ", ctx.getUwComments());
        UnderwritingRecordEntity uwRecord = new UnderwritingRecordEntity();
        uwRecord.setTenantId(proposal.getTenantId());
        uwRecord.setProposalId(proposal.getId());
        uwRecord.setUwType(1); // 自动核保
        uwRecord.setUwResult(finalResult);
        uwRecord.setUwComment(uwComment);
        uwRecord.setUwOperator("system");
        uwRecord.setUwAt(LocalDateTime.now());
        uwRecordMapper.insert(uwRecord);

        log.info("Proposal underwriting result: {}, status → {}: proposalNo={}",
                finalResult, statusName(proposal.getStatus()), proposal.getProposalNo());

        return proposal;
    }

    /**
     * 查询投保单详情
     */
    public ProposalEntity getProposalById(Long id) {
        ProposalEntity proposal = proposalMapper.selectById(id);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + id);
        }
        enrichProposalNames(List.of(proposal));
        return proposal;
    }

    /**
     * 分页查询投保单列表
     */
    public Page<ProposalEntity> listProposals(Integer status, Long channelId, String keyword,
                                              int page, int size) {
        LambdaQueryWrapper<ProposalEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ProposalEntity::getStatus, status);
        }
        if (channelId != null) {
            wrapper.eq(ProposalEntity::getChannelId, channelId);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ProposalEntity::getProposalNo, keyword));
        }
        wrapper.orderByDesc(ProposalEntity::getCreatedAt);
        Page<ProposalEntity> result = proposalMapper.selectPage(new Page<>(page, size), wrapper);
        enrichProposalNames(result.getRecords());
        return result;
    }

    /**
     * 更新投保单状态（带状态机校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, int newStatus) {
        ProposalEntity proposal = proposalMapper.selectById(id);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + id);
        }
        Set<Integer> allowed = VALID_TRANSITIONS.getOrDefault(proposal.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BizException("状态流转不合法: " + statusName(proposal.getStatus())
                    + " → " + statusName(newStatus));
        }
        proposal.setStatus(newStatus);
        proposalMapper.updateById(proposal);
        log.info("Proposal status updated: id={}, newStatus={}", id, statusName(newStatus));
    }

    // ========== 内部方法 ==========

    private CovexFlowContext buildFlowContext(ProposalEntity proposal) {
        CovexFlowContext ctx = new CovexFlowContext();
        ctx.setProposal(proposal);

        // 加载投保人（使用 CustomerService 以获取解密后的数据）
        if (proposal.getApplicantId() != null) {
            try {
                ctx.setApplicant(customerService.getCustomerById(proposal.getApplicantId()));
            } catch (Exception e) {
                log.warn("buildFlowContext: failed to load applicant id={}: {}",
                        proposal.getApplicantId(), e.getMessage());
            }
        }

        // 加载被保人
        if (proposal.getInsuredId() != null) {
            try {
                CustomerEntity insured = customerService.getCustomerById(proposal.getInsuredId());
                ctx.setInsured(insured);
                log.info("buildFlowContext: insured loaded id={}, birthDate={}",
                        insured.getId(), insured.getBirthDate());
            } catch (Exception e) {
                log.warn("buildFlowContext: failed to load insured id={}: {}",
                        proposal.getInsuredId(), e.getMessage());
            }
            // 加载被保人健康档案
            LambdaQueryWrapper<CustomerInsuredEntity> insuredWrapper = new LambdaQueryWrapper<>();
            insuredWrapper.eq(CustomerInsuredEntity::getCustomerId, proposal.getInsuredId());
            ctx.setInsuredHealth(customerInsuredMapper.selectOne(insuredWrapper));
        }

        // 加载产品
        if (proposal.getProductId() != null) {
            ctx.setProduct(productMapper.selectById(proposal.getProductId()));
        }

        // 加载渠道商
        if (proposal.getChannelId() != null) {
            ctx.setChannel(channelMapper.selectById(proposal.getChannelId()));
        }

        return ctx;
    }

    /**
     * 综合核保结论：取最严格的结果
     * 5-拒保 > 6-转人工 > 3-除外 > 2-加费 > 4-延期 > 1-标准体
     */
    private int determineUwResult(CovexFlowContext ctx) {
        if (ctx.getUwResults().isEmpty()) {
            return 1; // 默认标准体
        }
        int worst = 1;
        for (int r : ctx.getUwResults()) {
            if (r == 5) return 5;
            if (r == 6 && worst < 6) worst = 6;
            if (r == 3 && worst < 3) worst = 3;
            if (r == 2 && worst < 2) worst = 2;
            if (r == 4 && worst != 5 && worst != 6 && worst != 3) worst = 4;
        }
        return worst;
    }

    private String generateProposalNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "proposal_no:" + dateStr;
        Long seq = redisTemplate.opsForValue().increment(redisKey);
        return String.format("P%02d%s%06d", tenantId != null ? tenantId : 0, dateStr, seq);
    }

    private String statusName(int status) {
        return switch (status) {
            case 1 -> "待校验";
            case 2 -> "待核保";
            case 3 -> "核保中";
            case 4 -> "待支付";
            case 5 -> "已支付";
            case 6 -> "已出单";
            case 7 -> "已拒保";
            case 8 -> "已撤销";
            default -> "未知(" + status + ")";
        };
    }

    // ========== 名称解析 ==========

    private void enrichProposalNames(List<ProposalEntity> proposals) {
        if (proposals == null || proposals.isEmpty()) return;

        // 投保人/被保人名称
        List<Long> customerIds = proposals.stream()
                .flatMap(p -> java.util.stream.Stream.of(p.getApplicantId(), p.getInsuredId()))
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> nameMap = new java.util.HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds)
                    .forEach(c -> nameMap.put(c.getId(), c.getCustomerName()));
        }

        // 渠道商名称
        List<Long> channelIds = proposals.stream().map(ProposalEntity::getChannelId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> channelNameMap = new java.util.HashMap<>();
        if (!channelIds.isEmpty()) {
            channelMapper.selectBatchIds(channelIds)
                    .forEach(ch -> channelNameMap.put(ch.getId(), ch.getChannelName()));
        }

        for (ProposalEntity p : proposals) {
            if (p.getApplicantId() != null) p.setApplicantName(nameMap.get(p.getApplicantId()));
            if (p.getInsuredId() != null) p.setInsuredName(nameMap.get(p.getInsuredId()));
            if (p.getChannelId() != null) p.setChannelName(channelNameMap.get(p.getChannelId()));
        }
    }
}
