<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">需求模块维护</span>
      <div style="display:flex;align-items:center;gap:8px;">
        <el-button
          v-if="authStore.hasPermission('module:create')"
          type="primary"
          size="small"
          @click="openDictForm()"
        >+ 新增</el-button>
        <el-button size="small" :icon="Refresh" circle @click="refreshList" />
      </div>
    </div>

    <div v-if="!authStore.hasPermission('module:view')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <el-table
      v-else
      :data="moduleList"
      border
      size="small"
      style="width:100%;"
    >
      <el-table-column label="模块名称" min-width="160">
        <template #default="{ row }">
          <span
            v-if="row.bgColor"
            :style="{ background: row.bgColor, padding: '2px 8px', borderRadius: '4px', fontSize: '12px' }"
          >{{ row.name }}</span>
          <span v-else>{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="背景色" width="100" align="center">
        <template #default="{ row }">
          <span
            v-if="row.bgColor"
            :style="{ display:'inline-block', width:'20px', height:'20px', borderRadius:'3px', background: row.bgColor, border:'1px solid #ddd', verticalAlign:'middle' }"
          />
          <span v-else style="color:#ccc;">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('module:edit')"
            size="small"
            @click="openDictForm(row)"
          >编辑</el-button>
          <el-popconfirm
            v-if="authStore.hasPermission('module:delete')"
            title="确认删除该模块？"
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
    <el-dialog v-model="dictFormVisible" :title="dictFormTitle" width="440px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="dictForm.name" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dictForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="背景色">
          <div style="display:flex;flex-wrap:wrap;gap:8px;align-items:center;">
            <div
              v-for="c in MODULE_COLORS"
              :key="c.value"
              :title="c.label"
              :style="{
                width: '24px',
                height: '24px',
                borderRadius: '4px',
                background: c.value,
                border: dictForm.bgColor === c.value ? '2px solid #409eff' : '1px solid #ddd',
                cursor: 'pointer',
                boxSizing: 'border-box'
              }"
              @click="dictForm.bgColor = c.value"
            />
            <el-button
              size="small"
              style="height:24px;padding:0 8px;font-size:12px;"
              @click="dictForm.bgColor = ''"
            >清除</el-button>
            <span v-if="dictForm.bgColor" style="font-size:12px;color:#606266;">
              已选：
              <span :style="{ background: dictForm.bgColor, padding:'1px 6px', borderRadius:'3px' }">
                {{ MODULE_COLORS.find(c => c.value === dictForm.bgColor)?.label || dictForm.bgColor }}
              </span>
            </span>
          </div>
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
import { getModules, createModule, updateModule, deleteModule } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const MODULE_COLORS = [
  { label: '浅红', value: '#FFCCCC' },
  { label: '浅橙', value: '#FFE0B2' },
  { label: '浅黄', value: '#FFF9C4' },
  { label: '浅绿', value: '#C8E6C9' },
  { label: '浅青', value: '#B2EBF2' },
  { label: '浅蓝', value: '#BBDEFB' },
  { label: '浅紫', value: '#E1BEE7' },
  { label: '浅粉', value: '#F8BBD0' },
  { label: '浅灰', value: '#ECEFF1' },
  { label: '薄荷', value: '#B2DFDB' },
]

const moduleList = ref([])
const dictFormVisible = ref(false)
const dictFormTitle = ref('')
const dictFormId = ref(null)
const dictForm = reactive({ name: '', sortOrder: 0, bgColor: '' })

onMounted(async () => {
  await refreshList()
})

async function refreshList() {
  const res = await getModules()
  moduleList.value = res.data || []
}

function openDictForm(row = null) {
  dictFormId.value = row?.id ?? null
  dictForm.name = row?.name ?? ''
  dictForm.sortOrder = row?.sortOrder ?? 0
  dictForm.bgColor = row?.bgColor ?? ''
  dictFormTitle.value = row ? '编辑模块' : '新增模块'
  dictFormVisible.value = true
}

async function handleSave() {
  if (!dictForm.name.trim()) { ElMessage.error('名称不能为空'); return }
  const payload = { name: dictForm.name, sortOrder: dictForm.sortOrder, bgColor: dictForm.bgColor || null }
  if (dictFormId.value) await updateModule(dictFormId.value, payload)
  else await createModule(payload)
  const res = await getModules()
  moduleList.value = res.data || []
  dictFormVisible.value = false
  ElMessage.success('保存成功')
}

async function handleDelete(id) {
  await deleteModule(id)
  const res = await getModules()
  moduleList.value = res.data || []
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
