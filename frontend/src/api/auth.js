import http from './axios.js'

export const login = (data) => http.post('/auth/login', data)
export const logout = () => http.post('/auth/logout')
export const ioaLogin = (data) => http.post('/auth/ioa/login', data)
