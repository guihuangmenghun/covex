import type { Result, PageQuery, PageResult, RateTable, RateTableRow } from '@/types';
import request from '@/utils/request';

// 获取费率表分页列表
export function getRateTablePage(params: PageQuery & { productId?: number }) {
  return request.get<any, Result<PageResult<RateTable>>>('/rate-table', { params });
}

// 根据 ID 获取费率表详情
export function getRateTableById(id: number) {
  return request.get<any, Result<RateTable>>(`/rate-table/${id}`);
}

// 创建费率表
export function createRateTable(data: Partial<RateTable>) {
  return request.post<any, Result<RateTable>>('/rate-table', data);
}

// 更新费率表
export function updateRateTable(id: number, data: Partial<RateTable>) {
  return request.put<any, Result<RateTable>>(`/rate-table/${id}`, data);
}

// 删除费率表
export function deleteRateTable(id: number) {
  return request.delete<any, Result<void>>(`/rate-table/${id}`);
}

// 获取费率表行数据
export function getRateTableRows(id: number) {
  return request.get<any, Result<RateTableRow[]>>(`/rate-table/${id}/rows`);
}

// 批量导入费率表行数据
export function importRateTableRows(id: number, rows: Partial<RateTableRow>[]) {
  return request.post<any, Result<void>>(`/rate-table/${id}/import`, { rows });
}

// 加载费率表到 Redis
export function loadRateTable(data: { tableCode: string; version: string }) {
  return request.post<any, Result<void>>('/rate-table/load', data);
}

// 清除费率表 Redis 缓存
export function evictRateTable(data: { tableCode: string; version: string }) {
  return request.post<any, Result<void>>('/rate-table/evict', data);
}

// 查询费率
export function queryRate(params: { tableCode: string; version: string; dimensionKey: string }) {
  return request.get<any, Result<any>>('/rate-table/query', { params });
}

