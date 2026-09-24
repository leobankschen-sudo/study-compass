import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('study_compass_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !String(error.config?.url).includes('/auth/')) {
      localStorage.removeItem('study_compass_token')
      localStorage.removeItem('study_compass_user')
      if (window.location.pathname !== '/login') window.location.assign('/login')
    }
    const fallback = error.response?.status === 401 ? '用户名、密码或登录状态不正确' : '服务暂时不可用，请稍后再试'
    return Promise.reject(new Error(error.response?.data?.message ?? fallback))
  },
)
