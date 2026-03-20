<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">角色管理</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('role:create')"
          type="primary"
          size="small"
          @click="openRoleForm()"
        >+ 新增角色</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshRoles" />
      </div>
    </div>

    <div class="role-layout">
      <!-- 左侧角色列表 -->
      <div class="role-left">
        <div style="font-size:13px;font-weight:600;color:#606266;margin-bottom:8px;padding-bottom:8px;border-bottom:1px solid #ebeef5;">
          角色列表
        </div>
        <div
          v-for="role in roleList"
          :key="role.id"
          class="role-item"
          :class="{ active: selectedRoleId === role.id }"
          @click="selectRole(role)"
        >
          <span style="flex:1;font-size:13px;">{{ role.name }}</span>
          <el-tag v-if="role.builtIn" size="small" type="info" style="margin-right:4px;">内置</el-tag>
          <template v-if="!role.builtIn">
            <el-button
              v-if="authStore.hasPermission('role:edit')"
              size="small"
              link
              style="padding:0 4px;"
              @click.stop="openRoleForm(role)"
            >编辑</el-button>
            <el-popconfirm
              v-if="authStore.hasPermission('role:delete')"
              title="确认删除该角色？"
              @confirm="handleDeleteRole(role.id)"
            >
              <template #reference>
                <el-button
                  size="small"
                  link
                  type="danger"
                  style="padding:0 4px;"
                  @click.stop
                >删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </div>
        <div v-if="roleList.length === 0" style="color:#c0c4cc;font-size:13px;text-align:center;padding:20px 0;">
          暂无角色
        </div>
      </div>

      <!-- 右侧权限树 -->
      <div class="role-right">
        <template v-if="selectedRoleId">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid #ebeef5;">
            <span style="font-size:13px;font-weight:600;color:#606266;">权限配置</span>
            <el-button
              v-if="authStore.hasPermission('role:edit') && !selectedRoleBuiltIn"
              type="primary"
              size="small"
              @click="saveRolePerms"
            >保存权限</el-button>
          </div>
          <el-tree
            ref="permTreeRef"
            :data="permTree"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermIds"
            :check-strictly="false"
            :props="{ label: 'name', children: 'children' }"
            :disabled="selectedRoleBuiltIn || !authStore.hasPermission('role:edit')"
            style="font-size:13px;"
          />
        </template>
        <div v-else style="padding:60px 0;text-align:center;color:#c0c4cc;font-size:13px;">
          请在左侧选择角色查看权限
        </div>
      </div>
    </div>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="roleFormVisible"
      :title="roleFormId ? '编辑角色' : '新增角色'"
      width="420px"
    >
      <el-form label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="roleForm.code" placeholder="如：CUSTOM_VIEWER" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getRoles, createRole, updateRole, deleteRole } from '../api/role.js'
import { getPermissionTree, getRolePermissions, saveRolePermissions } from '../api/permission.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const roleList = ref([])
const selectedRoleId = ref(null)
const selectedRoleBuiltIn = ref(false)
const permTree = ref([])
const checkedPermIds = ref([])
const permTreeRef = ref()
const roleFormVisible = ref(false)
const roleFormId = ref(null)
const roleForm = reactive({ name: '', code: '', remark: '' })

onMounted(async () => {
  const [rolesRes, treeRes] = await Promise.all([getRoles(), getPermissionTree()])
  roleList.value = rolesRes.data || []
  permTree.value = treeRes.data || []
})

async function refreshRoles() {
  const res = await getRoles()
  roleList.value = res.data || []
}

async function selectRole(role) {
  selectedRoleId.value = role.id
  selectedRoleBuiltIn.value = !!role.builtIn
  const res = await getRolePermissions(role.id)
  checkedPermIds.value = res.data || []
}

async function saveRolePerms() {
  const checked = permTreeRef.value?.getCheckedKeys() || []
  await saveRolePermissions(selectedRoleId.value, checked)
  ElMessage.success('权限保存成功')
}

function openRoleForm(row = null) {
  roleFormId.value = row?.id ?? null
  roleForm.name = row?.name ?? ''
  roleForm.code = row?.code ?? ''
  roleForm.remark = row?.remark ?? ''
  roleFormVisible.value = true
}

async function handleSaveRole() {
  if (!roleForm.name.trim()) { ElMessage.error('角色名称不能为空'); return }
  if (!roleForm.code.trim()) { ElMessage.error('角色编码不能为空'); return }
  if (roleFormId.value) await updateRole(roleFormId.value, roleForm)
  else await createRole(roleForm)
  ElMessage.success('保存成功')
  roleFormVisible.value = false
  const res = await getRoles()
  roleList.value = res.data || []
}

async function handleDeleteRole(id) {
  await deleteRole(id)
  ElMessage.success('删除成功')
  if (selectedRoleId.value === id) { selectedRoleId.value = null; checkedPermIds.value = [] }
  const res = await getRoles()
  roleList.value = res.data || []
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }

.role-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 140px);
}

.role-left {
  width: 300px;
  min-width: 300px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  overflow-y: auto;
  background: #fff;
}

.role-right {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  overflow-y: auto;
  background: #fff;
}

.role-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}

.role-item:hover { background: #f0f5ff; }
.role-item.active { background: #e6f0ff; font-weight: 600; color: #2d7cf6; }
</style>
