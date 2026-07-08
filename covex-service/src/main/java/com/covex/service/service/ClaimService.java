package com.covex.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.covex.common.exception.BizException;
import com.covex.service.dto.ClaimReviewDTO;
import com.covex.service.dto.InvestigationResultDTO;
import com.covex.service.dto.ReportClaimDTO;
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

/**
 * 理赔服务
 */
@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimMapper claimMapper;
    private final ClaimReviewMapper claimReviewMapper;
    private final PolicyMapper policyMapper;
    private final PolicyCoverageMapper policyCoverageMapper;
    private final CustomerMapper customerMapper;
    private final FlowExecutor flowExecutor;
    private final StringRedisTemplate redisTemplate;

    public ClaimService(ClaimMapper claimMapper,
                        ClaimReviewMapper claimReviewMapper,
                        PolicyMapper policyMapper,
                        PolicyCoverageMapper policyCoverageMapper,
                        CustomerMapper customerMapper,
                        FlowExecutor flowExecutor,
                        StringRedisTemplate redisTemplate) {
        this.claimMapper = claimMapper;
        this.claimReviewMapper = claimReviewMapper;
        this.policyMapper = policyMapper;
        this.policyCoverageMapper = policyCoverageMapper;
        this.customerMapper = customerMapper;
        this.flowExecutor = flowExecutor;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 报案 — 创建理赔案件，触发 claim validate 链
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimEntity reportClaim(ReportClaimDTO dto) {
        // 验证保单
        PolicyEntity policy = policyMapper.selectById(dto.getPolicyId());
        if (policy == null) {
            throw new BizException(404, "保单不存在: " + dto.getPolicyId());
        }

        // 验证险种明细
        PolicyCoverageEntity coverage = policyCoverageMapper.selectById(dto.getCoverageId());
        if (coverage == null) {
            throw new BizException(404, "险种明细不存在: " + dto.getCoverageId());
        }
        if (!coverage.getPolicyId().equals(dto.getPolicyId())) {
            throw new BizException("险种明细不属于该保单");
        }

        // 创建理赔案件
        ClaimEntity claim = new ClaimEntity();
        claim.setTenantId(policy.getTenantId());
        claim.setClaimNo(generateClaimNo(policy.getTenantId()));
        claim.setPolicyId(dto.getPolicyId());
        claim.setCoverageId(dto.getCoverageId());
        claim.setReporterId(dto.getReporterId());
        claim.setReporterRelation(dto.getReporterRelation());
        claim.setAccidentDate(dto.getAccidentDate());
        claim.setAccidentType(dto.getAccidentType());
        claim.setAccidentDesc(dto.getAccidentDesc());
        claim.setAccidentLocation(dto.getAccidentLocation());
        claim.setClaimAmount(dto.getClaimAmount() != null ? dto.getClaimAmount() : BigDecimal.ZERO);
        claim.setStatus(1); // 已报案
        claim.setReportedAt(LocalDateTime.now());
        claimMapper.insert(claim);

        log.info("Claim reported: claimNo={}, policyId={}, coverageId={}",
                claim.getClaimNo(), dto.getPolicyId(), dto.getCoverageId());

        // 构建上下文并执行 claim validate 链
        CovexFlowContext ctx = buildClaimFlowContext(claim, policy, coverage);

        LiteflowResponse validateResponse = flowExecutor.execute2Resp("claimValidateChain", null, ctx);
        if (!validateResponse.isSuccess()) {
            log.warn("Claim validate chain execution error: {}", validateResponse.getMessage());
        }

        if (!ctx.getErrors().isEmpty()) {
            log.warn("Claim validation failed: claimNo={}, errors={}",
                    claim.getClaimNo(), String.join("; ", ctx.getErrors()));
            // 即使校验不通过也保留案件，但记录审核标记
            claim.setStatus(2); // 审核中（需要人工复核）
            claimMapper.updateById(claim);

            // 创建自动审核记录，标记校验问题
            ClaimReviewEntity review = new ClaimReviewEntity();
            review.setTenantId(claim.getTenantId());
            review.setClaimId(claim.getId());
            review.setReviewType(1); // 自动审核
            review.setReviewResult(4); // 需调查
            review.setReviewComment("自动校验发现问题: " + String.join("; ", ctx.getErrors()));
            review.setReviewer("system");
            review.setReviewedAt(LocalDateTime.now());
            claimReviewMapper.insert(review);

            return claim;
        }

        // 校验通过 → 审核中
        claim.setStatus(2);
        claimMapper.updateById(claim);

        log.info("Claim validation passed, status → 审核中: claimNo={}", claim.getClaimNo());
        return claim;
    }

    /**
     * 查询理赔案件详情
     */
    public ClaimEntity getClaimById(Long id) {
        ClaimEntity claim = claimMapper.selectById(id);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + id);
        }
        return claim;
    }

    /**
     * 分页查询理赔案件列表
     */
    public Page<ClaimEntity> listClaims(String policyNo, Integer status, String handler,
                                         int page, int size) {
        LambdaQueryWrapper<ClaimEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(policyNo)) {
            // 先查保单ID
            LambdaQueryWrapper<PolicyEntity> policyWrapper = new LambdaQueryWrapper<>();
            policyWrapper.like(PolicyEntity::getPolicyNo, policyNo);
            List<PolicyEntity> policies = policyMapper.selectList(policyWrapper);
            if (policies.isEmpty()) {
                return new Page<>(page, size);
            }
            wrapper.in(ClaimEntity::getPolicyId,
                    policies.stream().map(PolicyEntity::getId).toList());
        }
        if (status != null) {
            wrapper.eq(ClaimEntity::getStatus, status);
        }
        if (StringUtils.isNotBlank(handler)) {
            wrapper.like(ClaimEntity::getClaimHandler, handler);
        }
        wrapper.orderByDesc(ClaimEntity::getCreatedAt);
        Page<ClaimEntity> result = claimMapper.selectPage(new Page<>(page, size), wrapper);
        enrichClaimNames(result.getRecords());
        return result;
    }

    /**
     * 自动分配理赔员（Redis 轮转计数器）
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimEntity assignHandler(Long claimId) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }

        String region = "default";
        String redisKey = "claim:assign:" + region;

        // 预设理赔员列表
        String[] handlers = {"handler_zhang", "handler_li", "handler_wang", "handler_zhao", "handler_chen"};

        // Redis 轮转计数器
        Long counter = redisTemplate.opsForValue().increment(redisKey);
        if (counter == null) {
            counter = 0L;
        }
        int index = (int) (counter % handlers.length);
        String assignedHandler = handlers[index];

        claim.setClaimHandler(assignedHandler);
        claimMapper.updateById(claim);

        log.info("Claim handler assigned: claimId={}, handler={}", claimId, assignedHandler);
        return claim;
    }

    /**
     * 提交审核结论
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimReviewEntity submitReview(Long claimId, ClaimReviewDTO dto) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }
        if (claim.getStatus() != 2 && claim.getStatus() != 3) {
            throw new BizException("只有审核中或需调查状态的案件可以提交审核，当前状态: " + claim.getStatus());
        }

        // 创建审核记录
        ClaimReviewEntity review = new ClaimReviewEntity();
        review.setTenantId(claim.getTenantId());
        review.setClaimId(claimId);
        review.setReviewType(dto.getReviewType());
        review.setReviewResult(dto.getReviewResult());
        review.setApprovedAmount(dto.getApprovedAmount());
        review.setRejectReason(dto.getRejectReason());
        review.setReviewComment(dto.getComment());
        review.setReviewer(dto.getReviewer());
        review.setReviewedAt(LocalDateTime.now());
        claimReviewMapper.insert(review);

        // 根据审核结论更新案件状态
        if (dto.getReviewResult() != null) {
            switch (dto.getReviewResult()) {
                case 1, 2 -> {
                    // 正常赔付/部分赔付 — 检查金额权限
                    BigDecimal amount = dto.getApprovedAmount();
                    if (amount != null) {
                        claim.setApprovedAmount(amount);
                    }
                    // 金额权限：理赔员上限1万，超出需主管复审
                    if (amount != null && amount.compareTo(new BigDecimal("10000")) > 0) {
                        claim.setStatus(10); // 待主管复审
                        log.info("Claim escalated to supervisor: claimId={}, amount={}", claimId, amount);
                    } else {
                        claim.setStatus(2); // 保持审核中，等待赔付
                    }
                }
                case 3 -> {
                    // 拒赔
                    claim.setStatus(5);
                    claim.setApprovedAmount(BigDecimal.ZERO);
                }
                case 4 -> {
                    // 需调查
                    claim.setStatus(3);
                }
            }
        }
        claimMapper.updateById(claim);

        log.info("Claim review submitted: claimId={}, result={}, reviewer={}",
                claimId, dto.getReviewResult(), dto.getReviewer());
        return review;
    }

    /**
     * 主管审批（处理待主管复审的案件，主管上限10万，超出需总经理）
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimEntity supervisorApprove(Long claimId, BigDecimal approvedAmount) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }
        if (claim.getStatus() != 10) {
            throw new BizException("只有待主管复审的案件可以主管审批，当前状态: " + claim.getStatus());
        }

        if (approvedAmount != null) {
            claim.setApprovedAmount(approvedAmount);
        }

        // 主管权限上限10万，超出继续升级到总经理（暂用状态11）
        if (approvedAmount != null && approvedAmount.compareTo(new BigDecimal("100000")) > 0) {
            claim.setStatus(11); // 待总经理审批
            log.info("Claim escalated to GM: claimId={}, amount={}", claimId, approvedAmount);
        } else {
            claim.setStatus(4); // 已赔付
            log.info("Claim approved by supervisor: claimId={}, amount={}", claimId, approvedAmount);
        }

        claimMapper.updateById(claim);
        enrichClaimNames(java.util.List.of(claim));
        return claim;
    }

    /**
     * 赔付计算 — 触发 claim calculation 链
     */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal calculateClaimAmount(Long claimId) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }

        PolicyEntity policy = policyMapper.selectById(claim.getPolicyId());
        PolicyCoverageEntity coverage = policyCoverageMapper.selectById(claim.getCoverageId());

        CovexFlowContext ctx = buildClaimFlowContext(claim, policy, coverage);

        LiteflowResponse response = flowExecutor.execute2Resp("claimCalculationChain", null, ctx);
        if (!response.isSuccess()) {
            throw new BizException("赔付计算链执行失败: " + response.getMessage());
        }

        if (!ctx.getErrors().isEmpty()) {
            throw new BizException("赔付计算校验不通过: " + String.join("; ", ctx.getErrors()));
        }

        BigDecimal calculated = ctx.getCalculatedAmount();
        if (calculated != null) {
            claim.setApprovedAmount(calculated);
            claimMapper.updateById(claim);
        }

        log.info("Claim amount calculated: claimId={}, calculated={}", claimId, calculated);
        return calculated;
    }

    /**
     * 标记需调查
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimEntity startInvestigation(Long claimId) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }
        if (claim.getStatus() != 2) {
            throw new BizException("只有审核中的案件可以启动调查，当前状态: " + claim.getStatus());
        }

        claim.setStatus(3); // 需调查
        claimMapper.updateById(claim);

        log.info("Claim investigation started: claimId={}", claimId);
        return claim;
    }

    /**
     * 调查结论提交 → 重新进入审核
     */
    @Transactional(rollbackFor = Exception.class)
    public ClaimEntity submitInvestigationResult(Long claimId, InvestigationResultDTO dto) {
        ClaimEntity claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new BizException(404, "理赔案件不存在: " + claimId);
        }
        if (claim.getStatus() != 3) {
            throw new BizException("只有需调查状态的案件可以提交调查结论，当前状态: " + claim.getStatus());
        }

        // 创建调查审核记录
        ClaimReviewEntity review = new ClaimReviewEntity();
        review.setTenantId(claim.getTenantId());
        review.setClaimId(claimId);
        review.setReviewType(3); // 调查审核
        review.setReviewResult(dto.getResult());
        review.setReviewComment(dto.getComment());
        review.setReviewer("investigator");
        review.setReviewedAt(LocalDateTime.now());
        claimReviewMapper.insert(review);

        // 重新进入审核中
        claim.setStatus(2);
        claimMapper.updateById(claim);

        log.info("Investigation result submitted: claimId={}, result={}", claimId, dto.getResult());
        return claim;
    }

    /**
     * 查询审核记录
     */
    public List<ClaimReviewEntity> getReviews(Long claimId) {
        LambdaQueryWrapper<ClaimReviewEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimReviewEntity::getClaimId, claimId)
               .orderByDesc(ClaimReviewEntity::getReviewedAt);
        return claimReviewMapper.selectList(wrapper);
    }

    /**
     * 查询理赔详情（含审核记录+材料）
     */
    public Map<String, Object> getClaimDetail(Long id) {
        ClaimEntity claim = getClaimById(id);
        enrichClaimNames(List.of(claim));
        Map<String, Object> detail = new HashMap<>();
        detail.put("claim", claim);
        detail.put("reviews", getReviews(id));
        return detail;
    }

    // ========== 名称解析 ==========

    private void enrichClaimNames(List<ClaimEntity> claims) {
        if (claims == null || claims.isEmpty()) return;

        // 保单号
        List<Long> policyIds = claims.stream().map(ClaimEntity::getPolicyId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> policyNoMap = new java.util.HashMap<>();
        if (!policyIds.isEmpty()) {
            policyMapper.selectBatchIds(policyIds)
                    .forEach(p -> policyNoMap.put(p.getId(), p.getPolicyNo()));
        }

        // 保障名称
        List<Long> coverageIds = claims.stream().map(ClaimEntity::getCoverageId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> coverageNameMap = new java.util.HashMap<>();
        if (!coverageIds.isEmpty()) {
            policyCoverageMapper.selectBatchIds(coverageIds)
                    .forEach(c -> coverageNameMap.put(c.getId(), c.getCoverageName()));
        }

        // 报案人名称
        List<Long> reporterIds = claims.stream().map(ClaimEntity::getReporterId)
                .filter(id -> id != null).distinct().toList();
        Map<Long, String> reporterNameMap = new java.util.HashMap<>();
        if (!reporterIds.isEmpty()) {
            customerMapper.selectBatchIds(reporterIds)
                    .forEach(c -> reporterNameMap.put(c.getId(), c.getCustomerName()));
        }

        for (ClaimEntity c : claims) {
            if (c.getPolicyId() != null) c.setPolicyNo(policyNoMap.get(c.getPolicyId()));
            if (c.getCoverageId() != null) c.setCoverageName(coverageNameMap.get(c.getCoverageId()));
            if (c.getReporterId() != null) c.setReporterName(reporterNameMap.get(c.getReporterId()));
        }
    }

    // ========== 内部方法 ==========

    private CovexFlowContext buildClaimFlowContext(ClaimEntity claim,
                                                    PolicyEntity policy,
                                                    PolicyCoverageEntity coverage) {
        CovexFlowContext ctx = new CovexFlowContext();
        ctx.setClaim(claim);
        ctx.setClaimPolicy(policy);
        ctx.setClaimCoverage(coverage);
        return ctx;
    }

    private String generateClaimNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "claim_no:" + dateStr;
        Long seq = redisTemplate.opsForValue().increment(redisKey);
        return String.format("CLM%02d%s%06d", tenantId != null ? tenantId : 0, dateStr, seq);
    }
}
