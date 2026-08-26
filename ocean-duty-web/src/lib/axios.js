import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '@/config'
import router from '@/router'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: config.baseApi,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (requestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      requestConfig.headers.Authorization = `Bearer ${token}`
    }
    return requestConfig
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 10003) {
        useUserStore().logout()
        router.push('/login')
      }
      if (res.code === 10004) {
        router.push('/dashboard')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export const getAxios = (url, params) => service.get(url, { params })
export const postAxios = (url, data) => service.post(url, data)

export default service
