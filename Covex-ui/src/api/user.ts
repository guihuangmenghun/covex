import request from '@/utils/request'
import type { Result, PageQuery, PageResult, User, Role, Permission } from '@/types'

// ============ 用户管理 ============

export function createUser(data: Partial<User> & { password?: string }) {
  return request.post<any, Result<User>>('/user', data)
}

export function getUserById(id: number) {
  return request.get<any, Result<User>>(`/user/${id}`)
}

export function getUserPage(params: PageQuery) {
  return request.get<any, Result<PageResult<User>>>('/user', { params })
}

export function updateUser(id: number, data: Partial<User>) {
  return request.put<any, Result<User>>(`/user/${id}`, data)
}

export function toggleUserStatus(id: number, status: number) {
  return request.put<any, Result<void>>(`/user/${id}/status`, { status })
}

export function assignRoles(userId: number, roleIds: number[]) {
  return request.post<any, Result<void>>(`/user/${userId}/roles`, roleIds)
}

export function getUserRoles(userId: number) {
  return request.get<any, Result<Role[]>>(`/user/${userId}/roles`)
}

export function getUserPermissions(userId: number) {
  return request.get<any, Result<Permission[]>>(`/user/${userId}/permissions`)
}

// ============ 角色管理 ============

export function createRole(data: Partial<Role>) {
  return request.post<any, Result<Role>>('/role', data)
}

export function getRoleList() {
  return request.get<any, Result<Role[]>>('/role')
}

export function updateRole(id: number, data: Partial<Role>) {
  return request.put<any, Result<Role>>(`/role/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<any, Result<void>>(`/role/${id}`)
}

export function assignPermissions(roleId: number, permIds: number[]) {
  return request.post<any, Result<void>>(`/role/${roleId}/permissions`, { permIds })
}

export function getRolePermissions(roleId: number) {
  return request.get<any, Result<Permission[]>>(`/role/${roleId}/permissions`)
}

// ============ 权限管理 ============

export function createPermission(data: Partial<Permission>) {
  return request.post<any, Result<Permission>>('/permission', data)
}

export function getPermissionList() {
  return request.get<any, Result<Permission[]>>('/permission')
}

export function getPermissionModules() {
  return request.get<any, Result<Record<string, Permission[]>>>('/permission/modules')
}

// ============ 数据范围 ============

export function setDataScope(roleId: number, data: { scopeType: number; customScopes?: string[] }) {
  return request.post<any, Result<void>>(`/data-scope/${roleId}`, data)
}

export function getDataScope(roleId: number) {
  return request.get<any, Result<{ scopeType: number; customScopes: string[] }>>(`/data-scope/${roleId}`)
}
