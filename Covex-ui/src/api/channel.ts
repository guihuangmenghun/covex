import request from '@/utils/request'
import type {
  Result,
  PageQuery,
  PageResult,
  Channel,
  ChannelUser,
  ChannelProduct,
  ChannelAuthorizeRequest,
} from '@/types'

// ============ 渠道商 ============

export function createChannel(data: Partial<Channel>) {
  return request.post<any, Result<Channel>>('/channel', data)
}

export function getChannelById(id: number) {
  return request.get<any, Result<Channel>>(`/channel/${id}`)
}

export function getChannelPage(params: PageQuery) {
  return request.get<any, Result<PageResult<Channel>>>('/channel', { params })
}

export function updateChannel(id: number, data: Partial<Channel>) {
  return request.put<any, Result<Channel>>(`/channel/${id}`, data)
}

export function updateChannelStatus(id: number, status: number) {
  return request.put<any, Result<void>>(`/channel/${id}/status`, { status })
}

export function authorizeProduct(channelId: number, data: ChannelAuthorizeRequest) {
  return request.post<any, Result<void>>(`/channel/${channelId}/authorize`, data)
}

export function revokeProductAuth(channelId: number, productId: number) {
  return request.delete<any, Result<void>>(`/channel/${channelId}/authorize/${productId}`)
}

export function getChannelProducts(channelId: number) {
  return request.get<any, Result<ChannelProduct[]>>(`/channel/${channelId}/products`)
}

// ============ 渠道账号 ============

export function createChannelUser(channelId: number, data: Partial<ChannelUser> & { password?: string }) {
  return request.post<any, Result<ChannelUser>>(`/channel/${channelId}/user`, data)
}

export function getChannelUsers(channelId: number) {
  return request.get<any, Result<ChannelUser[]>>(`/channel/${channelId}/user`)
}

export function updateChannelUser(channelId: number, id: number, data: Partial<ChannelUser>) {
  return request.put<any, Result<ChannelUser>>(`/channel/${channelId}/user/${id}`, data)
}

export function toggleChannelUserStatus(channelId: number, id: number, status: number) {
  return request.put<any, Result<void>>(`/channel/${channelId}/user/${id}/status`, { status })
}
