<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">用户管理</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('user:create')"
          type="primary"
          size="small"
          @click="openUserForm()"
        >+ 新增</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshUserList" />
      </div>
    </div>

    <div v-if="!authStore.hasPermission('user:view')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <el-table
      v-else
      v-loading="userLoading"
      :data="userList"
      border
      size="small"
      style="width:100%;"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column label="角色" min-width="120">
        <template #default="{ row }">
          <el-tag
            :type="row.role === 'ADMIN' ? 'danger' : row.role === 'MANAGER' ? 'warning' : 'info'"
            size="small"
          >{{ userRoleLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('user:edit')"
            size="small"
            @click="openUserForm(row)"
          >编辑</el-button>
          <el-popconfirm
            v-if="authStore.hasPermission('user:delete')"
            title="确认删除该用户？"
            @confirm="handleDeleteUser(row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="userFormVisible"
      :title="userFormId ? '编辑用户' : '新增用户'"
      width="420px"
    >
      <el-form label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item :label="userFormId ? '新密码' : '密码'" :required="!userFormId">
          <el-input
            v-model="userForm.password"
            type="password"
            :placeholder="userFormId ? '留空则不修改密码' : '请输入密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="userForm.roleCode" style="width:100%;">
            <el-option label="管理员 (ADMIN)" value="ADMIN" />
            <el-option label="经理 (MANAGER)" value="MANAGER" />
            <el-option label="普通用户 (USER)" value="USER" />
            <el-option
              v-for="r in allRoleOptions"
              :key="r.id"
              :label="r.name"
              :value="String(r.id)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getUsers, createUser, updateUser, deleteUser } from '../api/user.js'
import { getRoles } from '../api/role.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const userLoading = ref(false)
const userList = ref([])
const userFormVisible = ref(false)
const userFormId = ref(null)
const userForm = reactive({ username: '', password: '', roleCode: 'USER' })
const allRoleOptions = ref([])

function userRoleLabel(row) {
  if (row.role === 'ADMIN') return '管理员'
  if (row.role === 'MANAGER') return '经理'
  if (row.role === 'USER') return '普通用户'
  if (row.roleId) {
    const found = allRoleOptions.value.find(r => r.id === row.roleId)
    return found ? found.name : '自定义角色'
  }
  return '未知'
}

onMounted(async () => {
  if (authStore.hasPermission('user:create') || authStore.hasPermission('user:edit')) {
    const res = await getRoles()
    allRoleOptions.value = (res.data || []).filter(r => !r.builtIn)
  }
  if (authStore.hasPermission('user:view')) await refreshUserList()
})

async function refreshUserList() {
  userLoading.value = true
  try {
    const res = await getUsers()
    userList.value = res.data || []
  } finally {
    userLoading.value = false
  }
}

function openUserForm(row = null) {
  userFormId.value = row?.id ?? null
  userForm.username = row?.username ?? ''
  userForm.password = ''
  userForm.roleCode = row?.role ?? (row?.roleId ? String(row.roleId) : 'USER')
  userFormVisible.value = true
}

async function handleSaveUser() {
  if (!userForm.username.trim()) { ElMessage.error('用户名不能为空'); return }
  if (!userFormId.value && !userForm.password.trim()) { ElMessage.error('密码不能为空'); return }
  const builtIn = ['ADMIN', 'MANAGER', 'USER']
  const payload = { username: userForm.username }
  if (builtIn.includes(userForm.roleCode)) {
    payload.role = userForm.roleCode
    payload.roleId = null
  } else {
    payload.role = null
    payload.roleId = Number(userForm.roleCode)
  }
  if (userForm.password.trim()) payload.password = userForm.password
  if (userFormId.value) await updateUser(userFormId.value, payload)
  else await createUser(payload)
  ElMessage.success('保存成功')
  userFormVisible.value = false
  await refreshUserList()
}

async function handleDeleteUser(id) {
  await deleteUser(id)
  ElMessage.success('删除成功')
  await refreshUserList()
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
