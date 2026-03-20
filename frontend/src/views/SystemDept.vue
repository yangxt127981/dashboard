<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">需求方部门维护</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('dept:create')"
          type="primary"
          size="small"
          @click="openDictForm()"
        >+ 新增</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshList" />
      </div>
    </div>

    <div v-if="!authStore.hasPermission('dept:view')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <el-table
      v-else
      :data="deptList"
      border
      size="small"
      style="width:100%;"
    >
      <el-table-column prop="name" label="部门名称" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('dept:edit')"
            size="small"
            @click="openDictForm(row)"
          >编辑</el-button>
          <el-popconfirm
            v-if="authStore.hasPermission('dept:delete')"
            title="确认删除该部门？"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dictFormVisible" :title="dictFormTitle" width="400px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="dictForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dictForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const deptList = ref([])
const dictFormVisible = ref(false)
const dictFormTitle = ref('')
const dictFormId = ref(null)
const dictForm = reactive({ name: '', sortOrder: 0 })

onMounted(async () => {
  await refreshList()
})

async function refreshList() {
  const res = await getDepartments()
  deptList.value = res.data || []
}

function openDictForm(row = null) {
  dictFormId.value = row?.id ?? null
  dictForm.name = row?.name ?? ''
  dictForm.sortOrder = row?.sortOrder ?? 0
  dictFormTitle.value = row ? '编辑部门' : '新增部门'
  dictFormVisible.value = true
}

async function handleSave() {
  if (!dictForm.name.trim()) { ElMessage.error('名称不能为空'); return }
  const payload = { name: dictForm.name, sortOrder: dictForm.sortOrder }
  if (dictFormId.value) await updateDepartment(dictFormId.value, payload)
  else await createDepartment(payload)
  const res = await getDepartments()
  deptList.value = res.data || []
  dictFormVisible.value = false
  ElMessage.success('保存成功')
}

async function handleDelete(id) {
  await deleteDepartment(id)
  const res = await getDepartments()
  deptList.value = res.data || []
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
