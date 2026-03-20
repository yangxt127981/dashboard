<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">需求对接人维护</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('system:requestowner')"
          type="primary"
          size="small"
          @click="openRequestOwnerForm()"
        >+ 新增</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshList" />
      </div>
    </div>

    <div v-if="!authStore.hasPermission('system:requestowner')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <el-table
      v-else
      :data="requestOwnerList"
      border
      size="small"
      style="width:100%;"
    >
      <el-table-column prop="name" label="姓名" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openRequestOwnerForm(row)">编辑</el-button>
          <el-popconfirm
            title="确认删除该对接人？"
            @confirm="handleDeleteRequestOwner(row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="requestOwnerFormVisible" :title="requestOwnerFormTitle" width="400px">
      <el-form label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="requestOwnerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="requestOwnerForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requestOwnerFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRequestOwner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getRequestOwners, createRequestOwner, updateRequestOwner, deleteRequestOwner } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const requestOwnerList = ref([])
const requestOwnerFormVisible = ref(false)
const requestOwnerFormId = ref(null)
const requestOwnerForm = reactive({ name: '', sortOrder: 0 })
const requestOwnerFormTitle = ref('')

onMounted(async () => {
  await refreshList()
})

async function refreshList() {
  const res = await getRequestOwners()
  requestOwnerList.value = res.data || []
}

function openRequestOwnerForm(row = null) {
  requestOwnerFormId.value = row?.id ?? null
  requestOwnerForm.name = row?.name ?? ''
  requestOwnerForm.sortOrder = row?.sortOrder ?? 0
  requestOwnerFormTitle.value = row ? '编辑需求对接人' : '新增需求对接人'
  requestOwnerFormVisible.value = true
}

async function handleSaveRequestOwner() {
  if (!requestOwnerForm.name.trim()) return ElMessage.warning('名称不能为空')
  const payload = { name: requestOwnerForm.name, sortOrder: requestOwnerForm.sortOrder }
  try {
    if (requestOwnerFormId.value) await updateRequestOwner(requestOwnerFormId.value, payload)
    else await createRequestOwner(payload)
  } catch {
    return
  }
  const res = await getRequestOwners()
  requestOwnerList.value = res.data || []
  requestOwnerFormVisible.value = false
  ElMessage.success('保存成功')
}

async function handleDeleteRequestOwner(id) {
  await deleteRequestOwner(id)
  const res = await getRequestOwners()
  requestOwnerList.value = res.data || []
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
