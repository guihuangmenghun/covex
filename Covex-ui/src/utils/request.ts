import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { ref } from 'vue'

// 全局 loading 计数器
export const globalLoadingCount = ref(0)

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器 - 自动带 token + loading 计数
request.interceptors.request.use(
  (config) => {
    globalLoadingCount.value++
    const token = localStorage.getItem('covex_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    globalLoadingCount.value--
    return Promise.reject(error)
  },
)

// 响应拦截器 - 统一错误处理 + loading 计数
request.interceptors.response.use(
  (response) => {
    globalLoadingCount.value--
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    globalLoadingCount.value--
    if (error.response?.status === 401) {
      localStorage.removeItem('covex_token')
      localStorage.removeItem('covex_username')
      localStorage.removeItem('covex_roles')
      router.push('/login')
    } else if (error.response?.status === 403) {
      router.push('/403')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export default request
