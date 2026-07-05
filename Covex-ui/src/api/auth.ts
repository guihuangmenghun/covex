import request from '@/utils/request'
import type { Result, LoginRequest, LoginResponse } from '@/types'

// 用户登录
export function login(data: LoginRequest) {
  return request.post<any, Result<LoginResponse>>('/user/login', data)
}
