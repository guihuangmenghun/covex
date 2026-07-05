import request from '@/utils/request'
import type { Result, Payment } from '@/types'

// 计算保费
export function calculatePremium(proposalId: number) {
  return request.post<any, Result<{ totalPremium: number; details: any[] }>>(`/payment/calculate/${proposalId}`)
}

// 创建支付记录
export function createPayment(data: { proposalId: number; payChannel?: number }) {
  return request.post<any, Result<Payment>>('/payment/create', data)
}

// 支付回调
export function paymentCallback(data: any) {
  return request.post<any, Result<void>>('/payment/callback', data)
}

// 按投保单ID查询支付记录
export function queryPaymentByProposal(proposalId: number) {
  return request.get<any, Result<Payment[]>>(`/payment/query/${proposalId}`)
}

// 超时扫描
export function triggerTimeoutScan() {
  return request.post<any, Result<{ processedCount: number }>>('/payment/timeout-scan')
}
