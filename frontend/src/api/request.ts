import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import type { ApiResult } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  (response): any => {
    const body = response.data as ApiResult<unknown>
    if (body.code !== 200) {
      ElMessage.error(body.message || '操作失败')
      if (body.code === 401) {
        useAuthStore().logout()
        router.push('/login')
      }
      return Promise.reject(new Error(body.message || '操作失败'))
    }
    return body.data
  },
  (error) => {
    ElMessage.error(error?.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
