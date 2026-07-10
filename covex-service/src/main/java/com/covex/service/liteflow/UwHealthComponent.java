package com.covex.service.liteflow;

import com.covex.service.entity.CustomerInsuredEntity;
import com.covex.service.entity.ProposalEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 健康告知评估 — 对比 health_declaration + customer_insured.medical_history
 */
@LiteflowComponent("uwHealth")
public class UwHealthComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(UwHealthComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        ProposalEntity proposal = ctx.getProposal();
        CustomerInsuredEntity insuredHealth = ctx.getInsuredHealth();

        boolean hasHealthIssue = false;

        // 检查健康告知
        List<Map<String, Object>> declarations = proposal.getHealthDeclaration();
        if (declarations != null) {
            for (Map<String, Object> item : declarations) {
                Object answer = item.get("answer");
                if (answer != null && (Boolean.TRUE.equals(answer) || "yes".equalsIgnoreCase(answer.toString()) || "1".equals(answer.toString()))) {
                    hasHealthIssue = true;
                    break;
                }
            }
        }

        // 检查被保人健康档案中的高风险因素
        if (insuredHealth != null) {
            if (insuredHealth.getBmi() != null) {
                double bmi = insuredHealth.getBmi().doubleValue();
                if (bmi > 35 || bmi < 15) {
                    hasHealthIssue = true;
                    ctx.addUwResult(2, "BMI异常: " + bmi + "，建议加费");
                    log.info("Health UW: BMI risk detected, bmi={}", bmi);
                }
            }

            if (insuredHealth.getSmokingStatus() != null && insuredHealth.getSmokingStatus() == 1) {
                ctx.addUwResult(2, "吸烟者，建议加费");
                log.info("Health UW: smoker detected");
            }

            List<Map<String, Object>> medicalHistory = insuredHealth.getMedicalHistory();
            if (medicalHistory != null && !medicalHistory.isEmpty()) {
                hasHealthIssue = true;
            }
        }

        if (hasHealthIssue && ctx.getUwResults().isEmpty()) {
            ctx.addUwResult(6, "健康告知存在异常，建议转人工核保");
            log.info("Health UW: issues found, recommend manual review");
        } else if (!hasHealthIssue) {
            ctx.addUwResult(1, "健康告知正常");
            log.info("Health UW: standard body");
        }
    }
}
