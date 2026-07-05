import request from '@/utils/request'
import type { Result, DictItem } from '@/types'

// 查询所有字典（按类型分组）
export function getAllDicts() {
  return request.get<any, Result<Record<string, DictItem[]>>>('/dict')
}

// 按类型查询字典
export function getDictByType(dictType: string) {
  return request.get<any, Result<DictItem[]>>(`/dict/${dictType}`)
}

// 按类型+父编码查询（层级字典）
export function getDictChildren(dictType: string, parentCode?: string) {
  return request.get<any, Result<DictItem[]>>(`/dict/${dictType}/children`, {
    params: { parentCode },
  })
}

// 新增字典项
export function createDict(data: Partial<DictItem>) {
  return request.post<any, Result<DictItem>>('/dict', data)
}

// 更新字典项
export function updateDict(id: number, data: Partial<DictItem>) {
  return request.put<any, Result<DictItem>>(`/dict/${id}`, data)
}

// 删除字典项
export function deleteDict(id: number) {
  return request.delete<any, Result<void>>(`/dict/${id}`)
}

// 清空字典缓存
export function evictDictCache() {
  return request.post<any, Result<void>>('/dict/cache/evict')
}
