import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * v-permission 自定义指令
 * 
 * 用法：
 * - v-permission="'product:publish'" - 单个权限
 * - v-permission="['product:edit', 'product:publish']" - 多个权限（满足任一即可）
 * 
 * 注意：当前基于角色控制，permission 值实际检查的是角色
 * 后续可扩展为权限编码检查
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    
    if (!value) return
    
    const userStore = useUserStore()
    const userRoles = userStore.roles
    
    // 将权限值转为数组
    const requiredPermissions = Array.isArray(value) ? value : [value]
    
    // 检查用户是否拥有任一所需权限（当前基于角色匹配）
    const hasPermission = requiredPermissions.some((perm: string) => {
      return userRoles.includes(perm.toLowerCase())
    })
    
    // 如果没有权限，隐藏元素
    if (!hasPermission) {
      el.style.display = 'none'
    }
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    
    if (!value) {
      el.style.display = ''
      return
    }
    
    const userStore = useUserStore()
    const userRoles = userStore.roles
    const requiredPermissions = Array.isArray(value) ? value : [value]
    
    const hasPermission = requiredPermissions.some((perm: string) => {
      return userRoles.includes(perm.toLowerCase())
    })
    
    el.style.display = hasPermission ? '' : 'none'
  },
}
