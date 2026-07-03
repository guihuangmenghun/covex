package com.covex.service.liteflow;

import com.covex.service.entity.CustomerInsuredEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 职业风险评估 — 根据 occupation_risk_level 判断
 */
@LiteflowComponent("uwOccupation")
public class UwOccupationComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(UwOccupationComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        CustomerInsuredEntity insuredHealth = ctx.getInsuredHealth();

        if (insuredHealth == null) {
            ctx.addUwResult(1, "无职业信息，默认标准体");
            return;
        }

        Integer riskLevel = insuredHealth.getOccupationRiskLevel();
        if (riskLevel == null) {
            ctx.addUwResult(1, "无职业风险等级信息，默认标准体");
            return;
        }

        // 1-2: 低风险 → 标准体
        // 3-4: 中风险 → 加费
        // 5-6: 高风险 → 除外或拒保
        if (riskLevel <= 2) {
            ctx.addUwResult(1, "职业风险等级" + riskLevel + "，标准体");
            log.info("Occupation UW: low risk, level={}", riskLevel);
        } else if (riskLevel <= 4) {
            ctx.addUwResult(2, "职业风险等级" + riskLevel + "，建议加费");
            log.info("Occupation UW: medium risk, level={}", riskLevel);
        } else {
            ctx.addUwResult(3, "职业风险等级" + riskLevel + "，建议除外");
            log.info("Occupation UW: high risk, level={}", riskLevel);
        }
    }
}
