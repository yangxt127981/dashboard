import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const roleId = ref(localStorage.getItem('roleId') || null)
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  function setUser(data) {
    token.value = data.token
    username.value = data.username
    role.value = data.role || ''
    roleId.value = data.roleId || null
    permissions.value = data.permissions || []
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role || '')
    localStorage.setItem('roleId', data.roleId || '')
    localStorage.setItem('permissions', JSON.stringify(data.permissions || []))
  }

  function clearUser() {
    token.value = ''
    username.value = ''
    role.value = ''
    roleId.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('roleId')
    localStorage.removeItem('permissions')
  }

  const isAdmin = () => role.value === 'ADMIN'
  const isManager = () => role.value === 'MANAGER'
  const canEdit = () => hasPermission('requirement:create') || hasPermission('requirement:edit')
  const hasPermission = (code) => permissions.value.includes(code)

  return { token, username, role, roleId, permissions, setUser, clearUser, isAdmin, isManager, canEdit, hasPermission }
})
