import request from '@/utils/request'
import type {
  Result,
  PageQuery,
  PageResult,
  Product,
  ProductCoverage,
  ProductPremium,
  ProductRule,
  ProductDocument,
  ProductRiderRel,
  ProductChangelog,
  ProductTemplate,
  FromTemplateRequest,
} from '@/types'

// ============ 产品 ============

export function createProduct(data: Partial<Product>) {
  return request.post<any, Result<Product>>('/product', data)
}

export function getProductById(id: number) {
  return request.get<any, Result<Product>>(`/product/${id}`)
}

export function getProductPage(params: PageQuery) {
  return request.get<any, Result<PageResult<Product>>>('/product', { params })
}

export function getProductList() {
  return request.get<any, Result<PageResult<Product>>>('/product', { params: { page: 1, size: 999 } })
}

export function updateProduct(id: number, data: Partial<Product>) {
  return request.put<any, Result<Product>>(`/product/${id}`, data)
}

export function cloneProduct(id: number) {
  return request.post<any, Result<Product>>(`/product/${id}/clone`)
}

export function publishProduct(id: number) {
  return request.put<any, Result<void>>(`/product/${id}/publish`)
}

export function freezeProduct(id: number) {
  return request.put<any, Result<void>>(`/product/${id}/freeze`)
}

export function getProductChangelog(id: number) {
  return request.get<any, Result<ProductChangelog[]>>(`/product/${id}/changelog`)
}

// ============ 保障 ============

export function createCoverage(productId: number, data: Partial<ProductCoverage>) {
  return request.post<any, Result<ProductCoverage>>(`/product/${productId}/coverage`, data)
}

export function getCoverageList(productId: number) {
  return request.get<any, Result<ProductCoverage[]>>(`/product/${productId}/coverage`)
}

export function getCoverageDetail(productId: number, coverageId: number) {
  return request.get<any, Result<ProductCoverage>>(`/product/${productId}/coverage/${coverageId}`)
}

export function updateCoverage(productId: number, coverageId: number, data: Partial<ProductCoverage>) {
  return request.put<any, Result<ProductCoverage>>(`/product/${productId}/coverage/${coverageId}`, data)
}

export function deleteCoverage(productId: number, coverageId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/coverage/${coverageId}`)
}

export function linkPremium(productId: number, coverageId: number, premiumId: number) {
  return request.post<any, Result<void>>(`/product/${productId}/coverage/${coverageId}/link-premium`, { premiumId })
}

export function unlinkPremium(productId: number, coverageId: number, premiumId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/coverage/${coverageId}/unlink-premium/${premiumId}`)
}

export function getCoveragePremiums(productId: number, coverageId: number) {
  return request.get<any, Result<ProductPremium[]>>(`/product/${productId}/coverage/${coverageId}/premiums`)
}

// ============ 缴费计划 ============

export function createPremium(productId: number, data: Partial<ProductPremium>) {
  return request.post<any, Result<ProductPremium>>(`/product/${productId}/premium`, data)
}

export function getPremiumList(productId: number) {
  return request.get<any, Result<ProductPremium[]>>(`/product/${productId}/premium`)
}

export function getPremiumDetail(productId: number, premiumId: number) {
  return request.get<any, Result<ProductPremium>>(`/product/${productId}/premium/${premiumId}`)
}

export function updatePremium(productId: number, premiumId: number, data: Partial<ProductPremium>) {
  return request.put<any, Result<ProductPremium>>(`/product/${productId}/premium/${premiumId}`, data)
}

export function deletePremium(productId: number, premiumId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/premium/${premiumId}`)
}

// ============ 规则 ============

export function createRule(productId: number, data: Partial<ProductRule>) {
  return request.post<any, Result<ProductRule>>(`/product/${productId}/rule`, data)
}

export function getRuleList(productId: number) {
  return request.get<any, Result<ProductRule[]>>(`/product/${productId}/rule`)
}

export function getRuleDetail(productId: number, ruleId: number) {
  return request.get<any, Result<ProductRule>>(`/product/${productId}/rule/${ruleId}`)
}

export function updateRule(productId: number, ruleId: number, data: Partial<ProductRule>) {
  return request.put<any, Result<ProductRule>>(`/product/${productId}/rule/${ruleId}`, data)
}

export function deleteRule(productId: number, ruleId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/rule/${ruleId}`)
}

// ============ 文档 ============

export function createDocument(productId: number, data: Partial<ProductDocument>) {
  return request.post<any, Result<ProductDocument>>(`/product/${productId}/document`, data)
}

export function getDocumentList(productId: number) {
  return request.get<any, Result<ProductDocument[]>>(`/product/${productId}/document`)
}

export function getDocumentDetail(productId: number, documentId: number) {
  return request.get<any, Result<ProductDocument>>(`/product/${productId}/document/${documentId}`)
}

export function updateDocument(productId: number, documentId: number, data: Partial<ProductDocument>) {
  return request.put<any, Result<ProductDocument>>(`/product/${productId}/document/${documentId}`, data)
}

export function deleteDocument(productId: number, documentId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/document/${documentId}`)
}

// ============ 附险 ============

export function createRider(productId: number, data: Partial<ProductRiderRel>) {
  return request.post<any, Result<ProductRiderRel>>(`/product/${productId}/rider`, data)
}

export function getRiderList(productId: number) {
  return request.get<any, Result<ProductRiderRel[]>>(`/product/${productId}/rider`)
}

export function deleteRider(productId: number, relId: number) {
  return request.delete<any, Result<void>>(`/product/${productId}/rider/${relId}`)
}

// ============ 产品模板 ============

export function getTemplateList(tenantId = 0) {
  return request.get<any, Result<ProductTemplate[]>>('/product-template', { params: { tenantId } })
}

export function getTemplateDetail(code: string) {
  return request.get<any, Result<ProductTemplate>>(`/product-template/${code}`)
}

export function createProductFromTemplate(data: FromTemplateRequest) {
  return request.post<any, Result<Record<string, any>>>('/product-template/create-product', data)
}
