import axios from 'axios'
import { ElMessage } from 'element-plus'

// In Electron production mode, use direct backend URL
// In browser dev mode, use relative path with Vite proxy
const isElectron = window.location.protocol === 'file:'
const baseURL = isElectron ? 'http://localhost:8080/api' : '/api'

const request = axios.create({
  baseURL,
  timeout: 30000,
})

request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
