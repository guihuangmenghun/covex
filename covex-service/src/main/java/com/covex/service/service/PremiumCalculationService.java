package com.covex.service.service;

import com.covex.common.exception.BizException;
import com.covex.service.entity.ProposalEntity;
import com.covex.service.mapper.ProposalMapper;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Options;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 保费计算服务 — 使用 Aviator 表达式引擎
 */
@Service
public class PremiumCalculationService {

    private static final Logger log = LoggerFactory.getLogger(PremiumCalculationService.class);

    private final ProposalMapper proposalMapper;
    private final RateTableService rateTableService;
    private final AviatorEvaluatorInstance aviator;

    public PremiumCalculationService(ProposalMapper proposalMapper,
                                      RateTableService rateTableService) {
        this.proposalMapper = proposalMapper;
        this.rateTableService = rateTableService;
        this.aviator = AviatorEvaluator.newInstance();
    }

    @PostConstruct
    public void init() {
        // 配置 Aviator
        aviator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);

        // 将 rateLookup 作为 Java lambda 函数注入（Aviator 5.x 支持 lambda 注册）
        // 使用方式：在 env 中传入 rateLookupFn，表达式中调用 rateLookupFn(tableCode, version, key)
        log.info("Aviator evaluator initialized for premium calculation");
    }

    /**
     * 计算保费
     * 从投保单取选中责任和费率表，用 Aviator 计算保费
     */
    @SuppressWarnings("unchecked")
    public BigDecimal calculatePremium(Long proposalId) {
        ProposalEntity proposal = proposalMapper.selectById(proposalId);
        if (proposal == null) {
            throw new BizException(404, "投保单不存在: " + proposalId);
        }

        BigDecimal totalPremium = BigDecimal.ZERO;

        if (proposal.getSelectedCoverages() instanceof List) {
            List<Map<String, Object>> coverages = (List<Map<String, Object>>) proposal.getSelectedCoverages();

            for (Map<String, Object> coverage : coverages) {
                Object sumInsuredObj = coverage.get("sumInsured");
                if (sumInsuredObj == null) continue;

                BigDecimal sumInsured = new BigDecimal(sumInsuredObj.toString());

                // 使用 Aviator 表达式计算保费
                BigDecimal premium = calculateWithAviator(proposal, coverage, sumInsured);
                totalPremium = totalPremium.add(premium);
            }
        }

        if (totalPremium.compareTo(BigDecimal.ZERO) == 0) {
            throw new BizException("保费计算结果为零，请检查保障责任和费率表配置");
        }

        // 更新投保单保费
        proposal.setTotalPremium(totalPremium);
        proposalMapper.updateById(proposal);

        log.info("Premium calculated: proposalId={}, totalPremium={}", proposalId, totalPremium);
        return totalPremium;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal calculateWithAviator(ProposalEntity proposal, Map<String, Object> coverage, BigDecimal sumInsured) {
        // 从产品快照获取费率表编码
        String rateTableCode = "DEFAULT_RATE";
        String rateTableVersion = "1.0.0";
        String dimensionKey = "default";

        // 尝试从产品快照 attributes 中获取费率表信息
        if (proposal.getProductSnapshot() instanceof Map) {
            Map<String, Object> snapshot = (Map<String, Object>) proposal.getProductSnapshot();
            Object attrs = snapshot.get("attributes");
            if (attrs instanceof Map) {
                Map<String, Object> attributes = (Map<String, Object>) attrs;
                if (attributes.get("rate_table_code") != null) {
                    rateTableCode = attributes.get("rate_table_code").toString();
                }
                if (attributes.get("rate_table_version") != null) {
                    rateTableVersion = attributes.get("rate_table_version").toString();
                }
            }
        }

        BigDecimal rate;
        try {
            rate = rateTableService.queryRate(rateTableCode, rateTableVersion, dimensionKey);
        } catch (Exception e) {
            throw new BizException("产品未配置费率表或费率查询失败: " + rateTableCode + " v" + rateTableVersion);
        }

        // 构建 Aviator 环境
        Map<String, Object> env = new HashMap<>();
        env.put("sumInsured", sumInsured);
        env.put("rate", rate);

        // Aviator 表达式
        String expression = "sumInsured * rate";
        Object result = aviator.execute(expression, env);

        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        } else if (result instanceof Number) {
            return BigDecimal.valueOf(((Number) result).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }

        return sumInsured.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
