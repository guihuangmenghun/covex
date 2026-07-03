import { useState } from "react";

const domains = [
  {
    name: "产品配置域",
    color: "#3B82F6",
    tables: [
      { name: "ins_product", desc: "产品主表", pk: "id", fields: ["product_code", "version", "product_name", "product_type", "status", "capabilities (JSON)", "attributes (JSON)"] },
      { name: "ins_product_coverage", desc: "保障定义", pk: "id", fk: ["product_id"], fields: ["coverage_code", "coverage_name", "benefit_type", "coverage_detail (JSON)"] },
      { name: "ins_product_premium", desc: "缴费规则", pk: "id", fk: ["product_id"], fields: ["premium_plan_code", "payment_frequency", "premium_detail (JSON)"] },
      { name: "ins_coverage_premium_rel", desc: "责任-缴费关联", pk: "id", fk: ["coverage_id", "premium_id"], fields: [] },
      { name: "ins_product_rule", desc: "规则引用", pk: "id", fk: ["product_id", "coverage_id"], fields: ["rule_engine", "rule_code", "rule_type"] },
      { name: "ins_product_rider_rel", desc: "主附险关联", pk: "id", fields: ["main_product_code", "rider_product_code"] },
      { name: "ins_product_document", desc: "条款文档", pk: "id", fk: ["product_id"], fields: ["document_type", "file_url", "effective_date"] },
      { name: "ins_product_changelog", desc: "变更日志", pk: "id", fk: ["product_id"], fields: ["change_type", "old_value", "new_value"] },
      { name: "ins_rate_table", desc: "费率表元数据", pk: "id", fk: ["product_id"], fields: ["rate_table_code", "version", "table_schema (JSON)"] },
      { name: "ins_rate_table_row", desc: "费率表行数据", pk: "id", fk: ["rate_table_id"], fields: ["dimension_key", "rate_value"] },
      { name: "ins_dict", desc: "数据字典", pk: "id", fields: ["dict_type", "dict_code", "dict_name", "parent_code"] },
    ]
  },
  {
    name: "渠道域",
    color: "#F59E0B",
    tables: [
      { name: "ins_channel", desc: "渠道商", pk: "id", fields: ["channel_code", "channel_name", "channel_type", "status"] },
      { name: "ins_channel_product", desc: "渠道-产品授权", pk: "id", fk: ["channel_id", "product_id"], fields: ["first_year_rate", "renewal_rate"] },
      { name: "ins_channel_user", desc: "渠道商账号", pk: "id", fk: ["channel_id"], fields: ["username", "real_name"] },
      { name: "ins_commission", desc: "佣金记录", pk: "id", fk: ["channel_id", "policy_id", "channel_user_id"], fields: ["commission_type", "commission_amount"] },
    ]
  },
  {
    name: "客户域",
    color: "#10B981",
    tables: [
      { name: "ins_customer", desc: "客户主表", pk: "id", fields: ["customer_code", "customer_name", "id_type", "id_no", "role_flags (JSON)"] },
      { name: "ins_customer_applicant", desc: "投保人扩展", pk: "id", fk: ["customer_id"], fields: ["annual_income", "marital_status", "has_social_security"] },
      { name: "ins_customer_insured", desc: "被保人扩展+健康档案", pk: "id", fk: ["customer_id"], fields: ["occupation", "occupation_risk_level", "medical_history (JSON)"] },
      { name: "ins_customer_bank_account", desc: "银行账户", pk: "id", fk: ["customer_id"], fields: ["bank_name", "account_no", "usage_type", "is_default"] },
      { name: "ins_customer_address", desc: "联系地址", pk: "id", fk: ["customer_id"], fields: ["address_type", "province", "city", "detail"] },
    ]
  },
  {
    name: "承保域",
    color: "#EF4444",
    tables: [
      { name: "ins_proposal", desc: "投保单", pk: "id", fk: ["product_id", "channel_id", "applicant_id", "insured_id"], fields: ["proposal_no", "status", "total_premium"] },
      { name: "ins_underwriting_record", desc: "核保记录", pk: "id", fk: ["proposal_id"], fields: ["uw_type", "uw_result"] },
      { name: "ins_policy", desc: "保单主表", pk: "id", fk: ["proposal_id", "product_id", "applicant_id", "insured_id"], fields: ["policy_no", "effective_date", "status"] },
      { name: "ins_policy_coverage", desc: "保单险种明细", pk: "id", fk: ["policy_id"], fields: ["coverage_code", "sum_insured", "premium"] },
      { name: "ins_policy_premium", desc: "保单缴费计划", pk: "id", fk: ["policy_id"], fields: ["premium_plan_code", "period_premium", "next_due_date"] },
      { name: "ins_payment", desc: "支付记录", pk: "id", fk: ["policy_id"], fields: ["payment_no", "payment_type", "amount", "status"] },
    ]
  },
  {
    name: "保单服务域",
    color: "#8B5CF6",
    tables: [
      { name: "ins_renewal_bill", desc: "续期账单", pk: "id", fk: ["policy_id"], fields: ["period_no", "due_date", "amount", "status"] },
      { name: "ins_endorsement", desc: "保全申请", pk: "id", fk: ["policy_id"], fields: ["endorsement_type", "status"] },
      { name: "ins_endorsement_change", desc: "保全变更明细", pk: "id", fk: ["endorsement_id"], fields: ["change_field", "old_value", "new_value"] },
      { name: "ins_policy_loan", desc: "保单借款", pk: "id", fk: ["policy_id"], fields: ["loan_amount", "interest_rate", "status"] },
    ]
  },
  {
    name: "理赔域",
    color: "#EC4899",
    tables: [
      { name: "ins_claim", desc: "理赔案件", pk: "id", fk: ["policy_id", "coverage_id"], fields: ["claim_no", "accident_date", "claim_amount", "status"] },
      { name: "ins_claim_document", desc: "理赔材料", pk: "id", fk: ["claim_id"], fields: ["document_type", "file_url"] },
      { name: "ins_claim_review", desc: "理赔审核", pk: "id", fk: ["claim_id"], fields: ["review_result", "approved_amount"] },
      { name: "ins_claim_payment", desc: "赔付记录", pk: "id", fk: ["claim_id", "payment_id"], fields: ["amount"] },
    ]
  },
  {
    name: "基础服务层",
    color: "#6B7280",
    tables: [
      { name: "ins_user", desc: "系统用户", pk: "id", fields: ["username", "real_name", "user_type"] },
      { name: "ins_role", desc: "角色", pk: "id", fields: ["role_code", "role_name"] },
      { name: "ins_permission", desc: "权限", pk: "id", fields: ["permission_code", "module", "action"] },
      { name: "ins_user_role", desc: "用户-角色", pk: "id", fk: ["user_id", "role_id"], fields: [] },
      { name: "ins_role_permission", desc: "角色-权限", pk: "id", fk: ["role_id", "permission_id"], fields: [] },
      { name: "ins_data_scope", desc: "数据权限范围", pk: "id", fk: ["role_id"], fields: ["scope_type", "scope_value"] },
    ]
  },
];

const crossDomainRelations = [
  { from: "ins_channel_product", to: "ins_product", label: "可售产品", fromDomain: "渠道域", toDomain: "产品配置域" },
  { from: "ins_proposal", to: "ins_product", label: "投保产品", fromDomain: "承保域", toDomain: "产品配置域" },
  { from: "ins_customer", to: "ins_proposal", label: "投保人/被保人", fromDomain: "客户域", toDomain: "承保域" },
  { from: "ins_commission", to: "ins_policy", label: "关联保单", fromDomain: "渠道域", toDomain: "承保域" },
  { from: "ins_claim_payment", to: "ins_payment", label: "赔付支付", fromDomain: "理赔域", toDomain: "承保域" },
];

function TableCard({ table, color, isExpanded, onClick }) {
  return (
    <div
      onClick={onClick}
      style={{
        border: `2px solid ${color}`,
        borderRadius: 8,
        padding: "8px 12px",
        marginBottom: 6,
        cursor: "pointer",
        background: isExpanded ? `${color}10` : "white",
        transition: "all 0.2s",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <span style={{ fontFamily: "monospace", fontSize: 13, fontWeight: 700, color: "#1F2937" }}>
            {table.name}
          </span>
          <span style={{ fontSize: 12, color: "#6B7280", marginLeft: 8 }}>{table.desc}</span>
        </div>
        <span style={{ fontSize: 11, color: "#9CA3AF" }}>{isExpanded ? "▲" : "▼"}</span>
      </div>
      {isExpanded && (
        <div style={{ marginTop: 8, fontSize: 12 }}>
          <div style={{ color: "#059669", fontWeight: 600, marginBottom: 2 }}>PK: {table.pk}</div>
          {table.fk && table.fk.length > 0 && (
            <div style={{ color: "#D97706", fontWeight: 600, marginBottom: 2 }}>
              FK: {table.fk.join(", ")}
            </div>
          )}
          {table.fields.length > 0 && (
            <div style={{ color: "#4B5563", marginTop: 4 }}>
              {table.fields.map((f, i) => (
                <div key={i} style={{ fontFamily: "monospace", fontSize: 11, padding: "1px 0" }}>
                  • {f}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function CovexER() {
  const [expandedTables, setExpandedTables] = useState(new Set());
  const [activeDomain, setActiveDomain] = useState(null);
  const [view, setView] = useState("domain");

  const toggleTable = (name) => {
    setExpandedTables((prev) => {
      const next = new Set(prev);
      if (next.has(name)) next.delete(name);
      else next.add(name);
      return next;
    });
  };

  const expandAll = () => {
    const all = new Set();
    domains.forEach((d) => d.tables.forEach((t) => all.add(t.name)));
    setExpandedTables(all);
  };

  const collapseAll = () => setExpandedTables(new Set());

  const totalTables = domains.reduce((sum, d) => sum + d.tables.length, 0);

  return (
    <div style={{ fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif", maxWidth: 960, margin: "0 auto", padding: 20 }}>
      <div style={{ textAlign: "center", marginBottom: 24 }}>
        <h1 style={{ fontSize: 24, fontWeight: 800, color: "#111827", margin: 0 }}>Covex 数据模型 ER 图</h1>
        <p style={{ fontSize: 14, color: "#6B7280", marginTop: 4 }}>
          {domains.length} 个业务域 · {totalTables} 张表 · 点击表名展开字段详情
        </p>
      </div>

      <div style={{ display: "flex", justifyContent: "center", gap: 8, marginBottom: 16 }}>
        <button
          onClick={() => setView("domain")}
          style={{
            padding: "6px 16px", borderRadius: 6, border: "none", cursor: "pointer", fontSize: 13, fontWeight: 600,
            background: view === "domain" ? "#1F2937" : "#E5E7EB", color: view === "domain" ? "white" : "#374151",
          }}
        >
          按域分组
        </button>
        <button
          onClick={() => setView("overview")}
          style={{
            padding: "6px 16px", borderRadius: 6, border: "none", cursor: "pointer", fontSize: 13, fontWeight: 600,
            background: view === "overview" ? "#1F2937" : "#E5E7EB", color: view === "overview" ? "white" : "#374151",
          }}
        >
          跨域关系
        </button>
        <button onClick={expandAll} style={{ padding: "6px 12px", borderRadius: 6, border: "1px solid #D1D5DB", background: "white", cursor: "pointer", fontSize: 12 }}>
          全部展开
        </button>
        <button onClick={collapseAll} style={{ padding: "6px 12px", borderRadius: 6, border: "1px solid #D1D5DB", background: "white", cursor: "pointer", fontSize: 12 }}>
          全部收起
        </button>
      </div>

      {view === "domain" && (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
          {domains.map((domain) => (
            <div
              key={domain.name}
              style={{
                border: `1px solid ${domain.color}40`,
                borderRadius: 12,
                padding: 16,
                background: `${domain.color}05`,
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 12 }}>
                <div style={{ width: 12, height: 12, borderRadius: 3, background: domain.color }} />
                <span style={{ fontSize: 16, fontWeight: 700, color: domain.color }}>{domain.name}</span>
                <span style={{ fontSize: 12, color: "#9CA3AF", background: "#F3F4F6", padding: "2px 8px", borderRadius: 10 }}>
                  {domain.tables.length} 表
                </span>
              </div>
              {domain.tables.map((table) => (
                <TableCard
                  key={table.name}
                  table={table}
                  color={domain.color}
                  isExpanded={expandedTables.has(table.name)}
                  onClick={() => toggleTable(table.name)}
                />
              ))}
            </div>
          ))}
        </div>
      )}

      {view === "overview" && (
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700, color: "#111827", marginBottom: 16, textAlign: "center" }}>
            跨域数据流
          </h2>
          <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", gap: 12, marginBottom: 24 }}>
            {domains.map((d) => (
              <div
                key={d.name}
                onClick={() => { setActiveDomain(activeDomain === d.name ? null : d.name); setView("domain"); }}
                style={{
                  padding: "12px 20px", borderRadius: 10, cursor: "pointer",
                  border: `2px solid ${d.color}`,
                  background: activeDomain === d.name ? d.color : `${d.color}15`,
                  color: activeDomain === d.name ? "white" : d.color,
                  fontWeight: 700, fontSize: 14, textAlign: "center",
                  transition: "all 0.2s",
                }}
              >
                <div>{d.name}</div>
                <div style={{ fontSize: 11, opacity: 0.8, marginTop: 2 }}>{d.tables.length} 张表</div>
              </div>
            ))}
          </div>

          <h3 style={{ fontSize: 15, fontWeight: 700, color: "#374151", marginBottom: 12 }}>跨域关联</h3>
          <div style={{ background: "#F9FAFB", borderRadius: 10, padding: 16 }}>
            {crossDomainRelations.map((rel, i) => {
              const fromDomain = domains.find((d) => d.name === rel.fromDomain);
              const toDomain = domains.find((d) => d.name === rel.toDomain);
              return (
                <div
                  key={i}
                  style={{
                    display: "flex", alignItems: "center", gap: 12, padding: "8px 0",
                    borderBottom: i < crossDomainRelations.length - 1 ? "1px solid #E5E7EB" : "none",
                  }}
                >
                  <span style={{ fontFamily: "monospace", fontSize: 12, fontWeight: 600, color: fromDomain?.color, background: `${fromDomain?.color}15`, padding: "3px 8px", borderRadius: 4 }}>
                    {rel.from}
                  </span>
                  <div style={{ flex: 1, display: "flex", alignItems: "center", gap: 4 }}>
                    <div style={{ flex: 1, height: 2, background: "#D1D5DB" }} />
                    <span style={{ fontSize: 11, color: "#6B7280", whiteSpace: "nowrap" }}>{rel.label}</span>
                    <div style={{ flex: 1, height: 2, background: "#D1D5DB" }} />
                  </div>
                  <span style={{ fontFamily: "monospace", fontSize: 12, fontWeight: 600, color: toDomain?.color, background: `${toDomain?.color}15`, padding: "3px 8px", borderRadius: 4 }}>
                    {rel.to}
                  </span>
                </div>
              );
            })}
          </div>

          <h3 style={{ fontSize: 15, fontWeight: 700, color: "#374151", marginTop: 20, marginBottom: 12 }}>核心业务流程</h3>
          <div style={{ background: "#F9FAFB", borderRadius: 10, padding: 16, fontSize: 13, color: "#374151", lineHeight: 2 }}>
            <div>
              <strong style={{ color: "#3B82F6" }}>产品配置域</strong> 定义产品模板 →
              <strong style={{ color: "#F59E0B" }}>渠道域</strong> 授权可售 →
              <strong style={{ color: "#10B981" }}>客户域</strong> 提供投保人信息 →
              <strong style={{ color: "#EF4444" }}>承保域</strong> 投保/核保/出单
            </div>
            <div>
              <strong style={{ color: "#EF4444" }}>承保域</strong> 生成保单 →
              <strong style={{ color: "#8B5CF6" }}>保单服务域</strong> 续期/保全/借款 →
              <strong style={{ color: "#EC4899" }}>理赔域</strong> 报案/审核/赔付
            </div>
            <div>
              <strong style={{ color: "#6B7280" }}>基础服务层</strong> 贯穿全局：用户/角色/权限/数据隔离
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
