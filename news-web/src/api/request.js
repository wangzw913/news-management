import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

// Request interceptor — attach token
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.accessToken) {
      config.headers.Authorization = `Bearer ${userStore.accessToken}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Response interceptor — unwrap + refresh on 401
let isRefreshing = false
let refreshQueue = []

function processQueue(error, token = null) {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  refreshQueue = []
}

request.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200) return data.data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  async error => {
    const { config, response } = error
    if (response?.status === 401 && !config._retry) {
      const userStore = useUserStore()
      if (!userStore.refreshToken) {
        userStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject })
        }).then(() => {
          config.headers.Authorization = `Bearer ${userStore.accessToken}`
          return request(config)
        })
      }

      config._retry = true
      isRefreshing = true

      try {
        const refreshData = await axios.post('/api/v1/auth/refresh', {
          refreshToken: userStore.refreshToken
        })
        const { accessToken, refreshToken } = refreshData.data.data
        userStore.accessToken = accessToken
        userStore.refreshToken = refreshToken
        processQueue(null, accessToken)
        config.headers.Authorization = `Bearer ${accessToken}`
        return request(config)
      } catch (e) {
        processQueue(e)
        userStore.logout()
        router.push('/login')
        return Promise.reject(e)
      } finally {
        isRefreshing = false
      }
    }

    ElMessage.error(response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
