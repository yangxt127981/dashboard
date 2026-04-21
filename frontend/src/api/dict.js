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

// 需求对接人
export const getRequestOwners = () => http.get('/dict/request-owners')
export const createRequestOwner = (data) => http.post('/dict/request-owners', data)
export const updateRequestOwner = (id, data) => http.put(`/dict/request-owners/${id}`, data)
export const deleteRequestOwner = (id) => http.delete(`/dict/request-owners/${id}`)

// 产品对接人
export const getProductOwners = () => http.get('/dict/product-owners')
export const createProductOwner = (data) => http.post('/dict/product-owners', data)
export const updateProductOwner = (id, data) => http.put(`/dict/product-owners/${id}`, data)
export const deleteProductOwner = (id) => http.delete(`/dict/product-owners/${id}`)
