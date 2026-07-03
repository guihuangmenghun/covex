package com.covex.service.liteflow;

import com.covex.service.entity.CustomerEntity;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * 校验证件号格式 + 有效期
 */
@LiteflowComponent("validateId")
public class ValidateIdComponent extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(ValidateIdComponent.class);

    @Override
    public void process() throws Exception {
        CovexFlowContext ctx = this.getContextBean(CovexFlowContext.class);
        CustomerEntity applicant = ctx.getApplicant();
        CustomerEntity insured = ctx.getInsured();

        // 投保人证件校验
        if (applicant != null) {
            validateCustomerId(ctx, applicant, "投保人");
        }

        // 被保人证件校验
        if (insured != null && !insured.getId().equals(applicant != null ? applicant.getId() : null)) {
            validateCustomerId(ctx, insured, "被保人");
        }
    }

    private void validateCustomerId(CovexFlowContext ctx, CustomerEntity customer, String role) {
        // 证件号非空
        if (StringUtils.isBlank(customer.getIdNo())) {
            ctx.addError(role + "证件号为空");
            return;
        }

        // 身份证格式（简单校验 18 位）
        if (customer.getIdType() != null && customer.getIdType() == 1) {
            String idNo = customer.getIdNo();
            if (idNo.length() != 18 && idNo.length() != 15) {
                ctx.addError(role + "身份证格式不正确");
            }
        }

        // 证件有效期
        if (customer.getIdExpiry() != null && customer.getIdExpiry().isBefore(LocalDate.now())) {
            ctx.addError(role + "证件已过期，过期日期: " + customer.getIdExpiry());
        }
    }
}
