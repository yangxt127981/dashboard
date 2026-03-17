import http from './axios.js'

export const getList = (params) => http.get('/requirements', { params })
export const create = (data) => http.post('/requirements', data)
export const update = (id, data) => http.put(`/requirements/${id}`, data)
export const remove = (id) => http.delete(`/requirements/${id}`)
