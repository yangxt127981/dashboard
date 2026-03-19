import http from './axios'

export const getLoginLogs = (params) => http.get('/system/login-logs', { params })
