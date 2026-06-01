import axios from 'axios'
import { getToken, setToken, removeToken, getRefreshToken } from './auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/admin',
  timeout: 15000,
})

// Token 刷新状态
let isRefreshing = false
let pendingRequests = []

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 刷新 Token
async function doRefreshToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  try {
    const res = await axios.post('/admin/auth/refresh', { refreshToken })
    if (res.data.code === 200) {
      setToken(res.data.data.token)
      return true
    }
  } catch (e) {
    // ignore
  }
  return false
}

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  async (error) => {
    const originalRequest = error.config
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      // 尝试刷新 Token
      if (isRefreshing) {
        // 已在刷新中，将请求加入队列
        return new Promise((resolve) => {
          pendingRequests.push(() => resolve(service(originalRequest)))
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const success = await doRefreshToken()
      isRefreshing = false

      if (success) {
        // 刷新成功，重试所有排队的请求
        pendingRequests.forEach(cb => cb())
        pendingRequests = []
        // 重试当前请求
        return service(originalRequest)
      } else {
        // 刷新失败，跳转登录
        pendingRequests = []
        removeToken()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      }
    }

    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      if (status === 403) {
        ElMessage.error(data?.msg || '无权限访问')
      } else if (status === 405) {
        ElMessage.error(data?.msg || '请求方法不支持')
      } else if (status !== 401) {
        ElMessage.error(data?.msg || '服务器错误')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service
