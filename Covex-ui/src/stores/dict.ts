import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAllDicts } from '@/api/dict'
import type { DictItem } from '@/types'

export const useDictStore = defineStore('dict', () => {
  const dictMap = ref<Record<string, DictItem[]>>({})
  const loading = ref(false)
  const loaded = ref(false)

  // 加载所有字典数据
  async function loadAllDicts() {
    if (loading.value) return
    if (loaded.value && Object.keys(dictMap.value).length > 0) return
    
    loading.value = true
    try {
      const res = await getAllDicts()
      dictMap.value = res.data || {}
      loaded.value = true
    } catch (error) {
      console.error('Failed to load dicts:', error)
    } finally {
      loading.value = false
    }
  }

  // 根据类型和编码获取字典标签
  function getDictLabel(dictType: string, dictCode: string): string {
    const items = dictMap.value[dictType]
    if (!items) return dictCode
    const item = items.find(i => i.dictCode === dictCode)
    return item?.dictName || dictCode
  }

  // 获取某类型下的所有选项（供 el-select 使用）
  function getDictOptions(dictType: string): DictItem[] {
    return dictMap.value[dictType] || []
  }

  // 刷新字典缓存
  function refreshDicts() {
    loaded.value = false
    return loadAllDicts()
  }

  return {
    dictMap,
    loading,
    loaded,
    loadAllDicts,
    getDictLabel,
    getDictOptions,
    refreshDicts,
  }
})
