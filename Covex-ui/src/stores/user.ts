import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function decodeJwt(token: string): Record<string, any> {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return {}
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('covex_token') || '')
  const username = ref(localStorage.getItem('covex_username') || '')
  const roles = ref<string[]>(JSON.parse(localStorage.getItem('covex_roles') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  function setLogin(data: { token: string; username?: string; roles?: string[] }) {
    token.value = data.token

    // 从 JWT token 解析 username 和 roles
    const payload = decodeJwt(data.token)
    const uname = data.username || payload.sub || payload.username || ''
    const userRoles = data.roles || payload.roles || []

    username.value = uname
    roles.value = userRoles.map((r: string) => r.toLowerCase()) // ADMIN -> admin

    localStorage.setItem('covex_token', data.token)
    localStorage.setItem('covex_username', uname)
    localStorage.setItem('covex_roles', JSON.stringify(roles.value))
  }

  function logout() {
    token.value = ''
    username.value = ''
    roles.value = []
    localStorage.removeItem('covex_token')
    localStorage.removeItem('covex_username')
    localStorage.removeItem('covex_roles')
  }

  function hasAnyRole(requiredRoles: string[]): boolean {
    return roles.value.some((role) => requiredRoles.includes(role))
  }

  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  return { token, username, roles, isLoggedIn, setLogin, logout, hasAnyRole, hasRole }
})
