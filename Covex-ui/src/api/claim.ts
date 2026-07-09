import request from '@/utils/request'
import type {
  Result, PageQuery, PageResult, Claim, ClaimDocument, ClaimReview,
  ReportClaimRequest, ClaimReviewRequest, InvestigationResultRequest,
} from '@/types'

// ============ 理赔 ============

export function createClaim(data: ReportClaimRequest) {
  return request.post<any, Result<Claim>>('/claim', data)
}

export function getClaimById(id: number) {
  return request.get<any, Result<{ claim: Claim; reviews: ClaimReview[] }>>(`/claim/${id}`)
}

export function getClaimPage(params: PageQuery & { policyNo?: string; status?: number; handler?: string }) {
  return request.get<any, Result<PageResult<Claim>>>('/claim', { params })
}

export function assignClaim(id: number) {
  return request.post<any, Result<Claim>>(`/claim/${id}/assign`)
}

export function reviewClaim(id: number, data: ClaimReviewRequest) {
  return request.post<any, Result<ClaimReview>>(`/claim/${id}/review`, data)
}

export function calculateClaim(id: number) {
  return request.post<any, Result<number>>(`/claim/${id}/calculate`)
}

export function investigateClaim(id: number) {
  return request.post<any, Result<Claim>>(`/claim/${id}/investigate`)
}

export function submitInvestigationResult(id: number, data: InvestigationResultRequest) {
  return request.post<any, Result<Claim>>(`/claim/${id}/investigation-result`, data)
}

// ============ 理赔材料 ============

export function uploadClaimDocument(claimId: number, data: { documentType: number; fileUrl: string; fileName: string }) {
  return request.post<any, Result<ClaimDocument>>(`/claim/${claimId}/document`, data)
}

export function getClaimDocuments(claimId: number) {
  return request.get<any, Result<ClaimDocument[]>>(`/claim/${claimId}/document`)
}

// ============ 理赔支付 ============

export function processClaimPayment(claimId: number, data?: { beneficiaryId?: number }) {
  return request.post<any, Result<any>>(`/claim/${claimId}/payment/process`, data || {})
}

export function claimPaymentCallback(claimId: number, data: { success: boolean }) {
  return request.post<any, Result<void>>(`/claim/${claimId}/payment/callback`, data)
}

export function closeClaim(claimId: number) {
  return request.post<any, Result<Claim>>(`/claim/${claimId}/payment/close`)
}

export function disputeClaim(claimId: number) {
  return request.post<any, Result<Claim>>(`/claim/${claimId}/payment/dispute`)
}

export function supervisorApproveClaim(id: number, approvedAmount: number) {
  return request.post<any, Result<Claim>>(`/claim/${id}/supervisor-approve`, { approvedAmount })
}
