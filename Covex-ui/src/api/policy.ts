import request from '@/utils/request'
import type { Result, PageQuery, PageResult, Policy, PolicyCoverage, PolicyPremium } from '@/types'

// 出单（从已支付投保单生成保单）
export function issuePolicy(proposalId: number) {
  return request.post<any, Result<Policy>>(`/policy/issue/${proposalId}`)
}

// 查询保单详情（含险种明细+缴费计划）
export function getPolicyById(id: number) {
  return request.get<any, Result<{ policy: Policy; coverages: PolicyCoverage[]; premiums: PolicyPremium[] }>>(`/policy/${id}`)
}

// 分页查询保单列表
export function getPolicyPage(params: PageQuery & { status?: number; applicantId?: number }) {
  return request.get<any, Result<PageResult<Policy>>>('/policy', { params })
}
