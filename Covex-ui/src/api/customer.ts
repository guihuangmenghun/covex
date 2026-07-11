import request from '@/utils/request'
import type { Result, PageQuery, PageResult, Customer, CustomerAddress, CustomerBankAccount } from '@/types'

// ============ 客户 ============

export function createCustomer(data: Partial<Customer>) {
  return request.post<any, Result<Customer>>('/customer', data)
}

export function getCustomerById(id: number) {
  return request.get<any, Result<Customer>>(`/customer/${id}`)
}

export function getCustomerPage(params: PageQuery) {
  return request.get<any, Result<PageResult<Customer>>>('/customer', { params })
}

export function updateCustomer(id: number, data: Partial<Customer>) {
  return request.put<any, Result<Customer>>(`/customer/${id}`, data)
}

export function ensureApplicant(id: number) {
  return request.post<any, Result<void>>(`/customer/${id}/ensure-applicant`)
}

export function ensureInsured(id: number) {
  return request.post<any, Result<void>>(`/customer/${id}/ensure-insured`)
}

export function getHealth(id: number) {
  return request.get<any, Result<Record<string, any>>>(`/customer/${id}/health`)
}

export function updateHealth(id: number, data: Record<string, any>) {
  return request.put<any, Result<void>>(`/customer/${id}/health`, data)
}

// ============ 地址 ============

export function createAddress(customerId: number, data: Partial<CustomerAddress>) {
  return request.post<any, Result<CustomerAddress>>(`/customer/${customerId}/address`, data)
}

export function getAddressList(customerId: number) {
  return request.get<any, Result<CustomerAddress[]>>(`/customer/${customerId}/address`)
}

export function updateAddress(customerId: number, id: number, data: Partial<CustomerAddress>) {
  return request.put<any, Result<CustomerAddress>>(`/customer/${customerId}/address/${id}`, data)
}

export function deleteAddress(customerId: number, id: number) {
  return request.delete<any, Result<void>>(`/customer/${customerId}/address/${id}`)
}

export function setDefaultAddress(customerId: number, id: number) {
  return request.put<any, Result<void>>(`/customer/${customerId}/address/${id}/default`)
}

// ============ 银行账户 ============

export function createBankAccount(customerId: number, data: Partial<CustomerBankAccount>) {
  return request.post<any, Result<CustomerBankAccount>>(`/customer/${customerId}/bank-account`, data)
}

export function getBankAccountList(customerId: number) {
  return request.get<any, Result<CustomerBankAccount[]>>(`/customer/${customerId}/bank-account`)
}

export function updateBankAccount(customerId: number, id: number, data: Partial<CustomerBankAccount>) {
  return request.put<any, Result<CustomerBankAccount>>(`/customer/${customerId}/bank-account/${id}`, data)
}

export function deleteBankAccount(customerId: number, id: number) {
  return request.delete<any, Result<void>>(`/customer/${customerId}/bank-account/${id}`)
}

export function setDefaultBankAccount(customerId: number, id: number) {
  return request.put<any, Result<void>>(`/customer/${customerId}/bank-account/${id}/default`)
}
