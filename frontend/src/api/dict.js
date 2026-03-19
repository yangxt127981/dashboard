import http from './axios'

// 部门
export const getDepartments = () => http.get('/dict/departments')
export const createDepartment = (data) => http.post('/dict/departments', data)
export const updateDepartment = (id, data) => http.put(`/dict/departments/${id}`, data)
export const deleteDepartment = (id) => http.delete(`/dict/departments/${id}`)

// 模块
export const getModules = () => http.get('/dict/modules')
export const createModule = (data) => http.post('/dict/modules', data)
export const updateModule = (id, data) => http.put(`/dict/modules/${id}`, data)
export const deleteModule = (id) => http.delete(`/dict/modules/${id}`)
