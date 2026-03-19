import axios from './axios.js'

export const getRoles = () => axios.get('/system/roles')
export const createRole = (data) => axios.post('/system/roles', data)
export const updateRole = (id, data) => axios.put(`/system/roles/${id}`, data)
export const deleteRole = (id) => axios.delete(`/system/roles/${id}`)
