import request from '@/utils/request'
import type { Result, PageQuery, PageResult, Proposal, CreateProposalRequest } from '@/types'

export function createProposal(data: CreateProposalRequest) {
  return request.post<any, Result<Proposal>>('/proposal', data)
}

export function getProposalById(id: number) {
  return request.get<any, Result<Proposal>>(`/proposal/${id}`)
}

export function getProposalPage(params: PageQuery & { status?: number; channelId?: number }) {
  return request.get<any, Result<PageResult<Proposal>>>('/proposal', { params })
}

export function submitProposal(id: number) {
  return request.put<any, Result<Proposal>>(`/proposal/${id}/submit`)
}

