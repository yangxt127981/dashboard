<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">产品对接人维护</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('system:productowner')"
          type="primary"
          size="small"
          @click="openForm()"
        >+ 新增</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshList" />
      </div>
    </div>

    <div v-if="!authStore.hasPermission('system:productowner')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <el-table
      v-else
      :data="list"
      border
      size="small"
      style="width:100%;"
    >
      <el-table-column prop="name" label="姓名" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-popconfirm
            title="确认删除该产品对接人？"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="formTitle" width="400px">
      <el-form label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getProductOwners, createProductOwner, updateProductOwner, deleteProductOwner } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const list = ref([])
const formVisible = ref(false)
const formId = ref(null)
const form = reactive({ name: '', sortOrder: 0 })
const formTitle = ref('')

onMounted(async () => {
  await refreshList()
})

async function refreshList() {
  const res = await getProductOwners()
  list.value = res.data || []
}

function openForm(row = null) {
  formId.value = row?.id ?? null
  form.name = row?.name ?? ''
  form.sortOrder = row?.sortOrder ?? 0
  formTitle.value = row ? '编辑产品对接人' : '新增产品对接人'
  formVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('名称不能为空')
  const payload = { name: form.name, sortOrder: form.sortOrder }
  try {
    if (formId.value) await updateProductOwner(formId.value, payload)
    else await createProductOwner(payload)
  } catch {
    return
  }
  await refreshList()
  formVisible.value = false
  ElMessage.success('保存成功')
}

async function handleDelete(id) {
  await deleteProductOwner(id)
  await refreshList()
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
