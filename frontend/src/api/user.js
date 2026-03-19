import http from './axios'

export const getUsers = () => http.get('/system/users')
export const createUser = (data) => http.post('/system/users', data)
export const updateUser = (id, data) => http.put(`/system/users/${id}`, data)
export const deleteUser = (id) => http.delete(`/system/users/${id}`)
