import request from '@/utils/request'
import type { Result, Commission } from '@/types'

// 查询佣金列表
export function getCommissionList(params: { channelId?: number; month?: string; status?: number }) {
  return request.get<any, Result<Commission[]>>('/commission', { params })
}

// 计算佣金
export function calculateCommission(data: {
  tenantId?: number
  policyId: number
  channelId: number
  channelUserId?: number | null
  premiumAmount: number
  commissionType: number
  commissionRate: number
}) {
  return request.post<any, Result<Commission>>('/commission/calculate', data)
}

// 触发月度结算
export function settleCommission(data: { yearMonth: string }) {
  return request.post<any, Result<{ settledCount: number; totalAmount: number }>>('/commission/settle', data)
}

// 月度汇总统计
export function getCommissionSummary(params: { channelId: number; yearMonth: string }) {
  return request.get<any, Result<{ totalCommission: number; settledCount: number; unsettledCount: number; paidCount: number }>>('/commission/summary', { params })
}

// 确认支付
export function confirmCommission(commissionId: number) {
  return request.put<any, Result<Commission>>('/commission/confirm', { commissionId })
}

// 驳回佣金
export function rejectCommission(commissionId: number) {
  return request.put<any, Result<Commission>>('/commission/reject', { commissionId })
}
