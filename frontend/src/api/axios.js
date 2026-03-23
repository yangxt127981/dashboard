import axios from 'axios'
import { ElMessage } from 'element-plus'

let maintenanceShown = false
function showMaintenance() {
  if (maintenanceShown) return
  maintenanceShown = true
  const overlay = document.createElement('div')
  overlay.id = 'maintenance-overlay'
  overlay.innerHTML = `
    <div style="position:fixed;inset:0;z-index:99999;background:#f0f2f5;display:flex;align-items:center;justify-content:center;">
      <div style="text-align:center;padding:40px;">
        <div style="font-size:64px;margin-bottom:24px;animation:pulse 2s ease-in-out infinite;">🔧</div>
        <h1 style="font-size:24px;color:#1d2129;margin-bottom:12px;font-weight:600;">系统部署中，请稍后</h1>
        <p style="font-size:15px;color:#86909c;line-height:1.6;">正在进行版本更新，预计 1 分钟内完成<br>页面将自动刷新，请耐心等待</p>
      </div>
    </div>
    <style>@keyframes pulse{0%,100%{opacity:1}50%{opacity:.5}}</style>`
  document.body.appendChild(overlay)
  const check = () => {
    fetch('/api/dict/departments', { headers: { Authorization: localStorage.getItem('token') || '' } })
      .then(r => { if (r.ok) location.reload(); else setTimeout(check, 3000) })
      .catch(() => setTimeout(check, 3000))
  }
  setTimeout(check, 5000)
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

instance.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    } else if (status === 502 || status === 503 || !error.response) {
      showMaintenance()
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default instance
