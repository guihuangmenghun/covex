package com.covex.service.liteflow;

import com.covex.service.entity.PolicyCoverageEntity;
import com.covex.service.entity.PolicyEntity;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.PolicyCoverageMapper;
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
 * 出单 — 创建保单险种明细
 */
@LiteflowComponent("issueCreateCoverage")
public class IssueCreateCoverageComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(IssueCreateCoverageComponent.class);

    @Autowired
    private PolicyCoverageMapper policyCoverageMapper;

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();
        PolicyEntity policy = ctx.getPolicy();

        List<PolicyCoverageEntity> coverageEntities = new ArrayList<>();

        if (proposal.getSelectedCoverages() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> coverages = (List<Map<String, Object>>) proposal.getSelectedCoverages();
            for (Map<String, Object> cov : coverages) {
                PolicyCoverageEntity entity = new PolicyCoverageEntity();
                entity.setTenantId(proposal.getTenantId());
                entity.setPolicyId(policy.getId());
                entity.setCoverageCode(cov.getOrDefault("coverageCode", "").toString());
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

                entity.setDeductible(BigDecimal.ZERO);
                entity.setStatus(1); // 有效

                policyCoverageMapper.insert(entity);
                coverageEntities.add(entity);
            }
        }

        ctx.setPolicyCoverages(coverageEntities);
        log.info("Policy coverages created: policyId={}, count={}", policy.getId(), coverageEntities.size());
    }
}
