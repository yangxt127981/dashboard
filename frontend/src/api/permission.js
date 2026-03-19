import axios from './axios.js'

export const getPermissionTree = () => axios.get('/system/permissions/tree')
export const getRolePermissions = (roleId) => axios.get(`/system/permissions/role/${roleId}`)
export const saveRolePermissions = (roleId, permissionIds) =>
  axios.post(`/system/permissions/role/${roleId}`, { permissionIds })
