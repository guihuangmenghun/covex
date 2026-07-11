package com.covex.service.liteflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.ProductCoverageEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PolicyCoverageMapper;
import com.covex.service.mapper.ProductCoverageMapper;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 出单 — 创建保单险种明细。
 * <p>
 * 从产品保障定义（ins_product_coverage）快照 coverage_detail 到保单保障（ins_policy_coverage），
 * 确保保单独立于产品配置（产品下架不影响已有保单）。
 */
@LiteflowComponent("issueCreateCoverage")
public class IssueCreateCoverageComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueCreateCoverageComponent.class);

    @Autowired
    private PolicyCoverageMapper policyCoverageMapper;

    @Autowired
    private ProductCoverageMapper productCoverageMapper;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();
        PolicyEntity policy = ctx.getPolicy();

        List<PolicyCoverageEntity> coverageEntities = new ArrayList<>();

        List<Map<String, Object>> coverages = proposal.getSelectedCoverages();
        if (coverages != null) {
            for (Map<String, Object> cov : coverages) {
                PolicyCoverageEntity entity = new PolicyCoverageEntity();
                entity.setTenantId(proposal.getTenantId());
                entity.setPolicyId(policy.getId());

                String coverageCode = cov.getOrDefault("coverageCode", "").toString();
                entity.setCoverageCode(coverageCode);
                entity.setCoverageName(cov.getOrDefault("coverageName", "").toString());

                Object sumInsured = cov.get("sumInsured");
                if (sumInsured != null) {
                    entity.setSumInsured(new BigDecimal(sumInsured.toString()));
                }

                // 均分保费到各责任
                if (proposal.getTotalPremium() != null && !coverages.isEmpty()) {
                    BigDecimal perPremium = proposal.getTotalPremium()
                            .divide(new BigDecimal(coverages.size()), 2, java.math.RoundingMode.HALF_UP);
                    entity.setPremium(perPremium);
                }

                // 从产品保障定义快照 coverage_detail
                Map<String, Object> coverageDetail = lookupCoverageDetail(proposal.getProductId(), coverageCode);
                entity.setCoverageDetail(coverageDetail);

                // 从 coverage_detail 提取 deductible（如有）
                BigDecimal deductible = BigDecimal.ZERO;
                if (coverageDetail != null) {
                    Object deductibleObj = coverageDetail.get("deductible");
                    if (deductibleObj != null) {
                        try {
                            deductible = new BigDecimal(deductibleObj.toString());
                        } catch (NumberFormatException ignored) {}
                    }
                }
                entity.setDeductible(deductible);

                entity.setStatus(1); // 有效
                entity.setVersion(0);

                policyCoverageMapper.insert(entity);
                coverageEntities.add(entity);
            }
        }

        ctx.setPolicyCoverages(coverageEntities);
        log.info("Policy coverages created: policyId={}, count={}", policy.getId(), coverageEntities.size());
    }

    /**
     * 从产品保障定义表查找 coverage_detail JSON。
     */
    private Map<String, Object> lookupCoverageDetail(Long productId, String coverageCode) {
        if (productId == null || coverageCode == null || coverageCode.isEmpty()) {
            return null;
        }
        try {
            LambdaQueryWrapper<ProductCoverageEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductCoverageEntity::getProductId, productId)
                   .eq(ProductCoverageEntity::getCoverageCode, coverageCode)
                   .eq(ProductCoverageEntity::getIsDeleted, 0)
                   .last("LIMIT 1");
            ProductCoverageEntity productCov = productCoverageMapper.selectOne(wrapper);
            if (productCov != null) {
                return productCov.getCoverageDetail();
            }
        } catch (Exception e) {
            log.warn("Failed to lookup coverage_detail for productId={}, code={}: {}",
                    productId, coverageCode, e.getMessage());
        }
        return null;
    }
}
