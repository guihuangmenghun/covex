import request from '@/utils/request'
import type { Result, UnderwritingRecord, Proposal } from '@/types'

// 自动核保（手动触发）
export function autoUnderwrite(proposalId: number) {
  return request.post<any, Result<Proposal>>(`/underwriting/auto/${proposalId}`)
}

// 人工核保
export function manualUnderwrite(proposalId: number, data: {
  uwResult: number
  loadingAmount?: number
  exclusionDesc?: string
  comment?: string
  operator?: string
}) {
  return request.post<any, Result<UnderwritingRecord>>(`/underwriting/manual/${proposalId}`, data)
}

// 查询核保记录
export function getUnderwritingRecords(proposalId: number) {
  return request.get<any, Result<UnderwritingRecord[]>>(`/underwriting/records/${proposalId}`)
}
