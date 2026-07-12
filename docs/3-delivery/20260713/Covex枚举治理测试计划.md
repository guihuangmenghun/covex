# Covex 枚举治理测试计划

> **创建日期**：2026-07-13
> **需求文档**：`1-specs/data-model/枚举值一致性审计与前端硬编码治理.md`
> **配套开发**：`Covex枚举治理开发计划.md`
> **配套报告**：`Covex枚举治理报告.md`
> **执行方式**：每完成一项立即将 `[ ]` 改为 `[x]`

---

## TS1：三方数据链路验证

> 对应开发计划 Task 1-2

- [ ] **TS1.1 DB 层**：GET `/api/dict` 返回 39 组字典，每组条目数与 `项目枚举值.md` 一致
- [ ] **TS1.2 Redis 层**：dict API 首次调用后 Redis 存在 dict 缓存 key
- [ ] **TS1.3 缓存刷新**：修改 DB 一条 dict 记录 → 再次调 API → 返回新值
- [ ] **TS1.4 恢复数据**：验证完毕后恢复原始数据

---

## TS2：后端代码审计 API 测试

> 对应开发计划 Task 1.5（后端优先原则）
> **强制要求**：B类/C类修改后必须补充 API 测试

- [ ] **TS2.1**：后端代码硬编码状态值审计完成，输出 A/B/C 分类清单
- [ ] **TS2.2**：B类修改（更新 ins_dict）后，相关 API 返回值语义不变
- [ ] **TS2.3**：C类修改（修正后端代码）后，相关 API 返回值符合 DB 定义
- [ ] **TS2.4**：涉及修改的每个 API 至少 1 条正向测试 + 1 条边界测试

---

## TS3：前端 P0 Bug 修复验证

> 对应开发计划 Task 3

- [ ] **TS3.1 Dashboard claim_status**：浏览器访问首页，理赔统计状态标签与 DB ins_dict 定义一致（逐条核对 11 个状态码）
- [ ] **TS3.2 ProductList versionStatus**：浏览器访问产品列表
  - 状态标签显示正确（草稿/待审批/已发布/已冻结/已驳回）
  - 草稿状态下"编辑""提交审批"按钮可见
  - 已发布状态下"编辑"按钮不可见

---

## TS4：前端 P1 硬编码治理验证

> 对应开发计划 Task 4

- [ ] **TS4.1 Dashboard proposal_status**：投保单统计状态标签正确
- [ ] **TS4.2 ProductDetail typeTagMap + vsMap**：产品详情页类型/状态标签来自字典
- [ ] **TS4.3 ProposalDetail statusOptions**：投保单详情页状态标签正确
- [ ] **TS4.4 ChannelList 状态下拉**：渠道列表状态变更下拉框显示全部 6 个选项
- [ ] **TS4.5 ChannelDetail 状态下拉**：渠道详情页同上
- [ ] **TS4.6 全局搜索**：前端代码中无 `Record<number, {label` 硬编码状态映射

---

## TS5：AGENTS.md 规范验证

> 对应开发计划 Task 4.6

- [ ] AGENTS.md 编码前检查清单包含 dictStore 强制规范
- [ ] 新增一个测试枚举值到 ins_dict → 前端 dictStore 能自动获取（无需改前端代码）
