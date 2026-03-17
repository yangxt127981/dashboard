<template>
  <div class="board-page">
    <!-- 顶部导航 -->
    <header class="top-bar">
      <div class="top-bar-left">
        <el-icon class="logo"><Grid /></el-icon>
        <span class="title">项目计划看板</span>
      </div>
      <div class="top-bar-right">
        <el-tag type="info" effect="plain" style="margin-right: 12px;">
          {{ authStore.role === 'ADMIN' ? '管理员' : '普通用户' }}
        </el-tag>
        <span class="user-name">{{ authStore.username }}</span>
        <el-divider direction="vertical" />
        <el-button text type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="main-content">
      <!-- 筛选区 -->
      <el-card class="filter-card" shadow="never">
        <el-row :gutter="16" align="middle">
          <el-col :span="6">
            <el-input v-model="query.functionName" placeholder="功能名称" clearable :prefix-icon="Search" />
          </el-col>
          <el-col :span="6">
            <el-input v-model="query.requestDepartment" placeholder="需求方部门" clearable :prefix-icon="OfficeBuilding" />
          </el-col>
          <el-col :span="5">
            <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 100%;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-col>
          <el-col :span="7">
            <el-button type="primary" @click="fetchList">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button v-if="authStore.isAdmin()" type="success" :icon="Plus" @click="openForm()">新增需求</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 表格 -->
      <el-card class="table-card" shadow="never">
        <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="functionName" label="功能名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="moduleName" label="所属模块" width="110" show-overflow-tooltip />
          <el-table-column prop="requestDepartment" label="需求方部门" width="120" show-overflow-tooltip />
          <el-table-column prop="requestOwner" label="需求对接人" width="100" />
          <el-table-column prop="productOwner" label="产品对接人" width="100" />
          <el-table-column label="优先级" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="priorityType(row.priority)" size="small" effect="plain">{{ row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="plannedStartTime" label="计划开始" width="105" />
          <el-table-column prop="plannedEndTime" label="计划完成" width="105" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="需求描述" min-width="150" show-overflow-tooltip />
          <el-table-column v-if="authStore.isAdmin()" label="操作" width="130" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openForm(row)">编辑</el-button>
              <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @change="fetchList"
          />
        </div>
      </el-card>
    </main>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑需求' : '新增需求'"
      width="700px"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="dialogFormRef" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="功能名称" prop="functionName">
              <el-input v-model="formData.functionName" placeholder="请输入功能名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属模块">
              <el-input v-model="formData.moduleName" placeholder="请输入模块名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求方部门">
              <el-input v-model="formData.requestDepartment" placeholder="请输入部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求对接人">
              <el-input v-model="formData.requestOwner" placeholder="业务负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品对接人">
              <el-input v-model="formData.productOwner" placeholder="产品经理" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="formData.priority" placeholder="请选择" style="width: 100%;">
                <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="请选择" style="width: 100%;">
                <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划开始时间">
              <el-date-picker v-model="formData.plannedStartTime" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划完成时间">
              <el-date-picker v-model="formData.plannedEndTime" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际开始时间">
              <el-date-picker v-model="formData.actualStartTime" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际完成时间">
              <el-date-picker v-model="formData.actualEndTime" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="需求描述">
              <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入需求详细描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, OfficeBuilding, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getList, create, update, remove } from '../api/requirement.js'
import { logout } from '../api/auth.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  functionName: '',
  requestDepartment: '',
  status: '',
  page: 1,
  size: 10
})

const statusOptions = ['未开始', '设计中', '开发中', '测试中', '已上线']
const priorityOptions = ['紧急', '高', '中', '低']

function statusType(status) {
  const map = { '未开始': 'info', '设计中': 'warning', '开发中': 'primary', '测试中': '', '已上线': 'success' }
  return map[status] || 'info'
}

function priorityType(priority) {
  const map = { '紧急': 'danger', '高': 'warning', '中': 'primary', '低': 'info' }
  return map[priority] || 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.functionName = ''
  query.requestDepartment = ''
  query.status = ''
  query.page = 1
  fetchList()
}

// 弹窗表单
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const dialogFormRef = ref()

const emptyForm = () => ({
  functionName: '',
  moduleName: '',
  requestDepartment: '',
  requestOwner: '',
  productOwner: '',
  priority: '中',
  status: '未开始',
  plannedStartTime: null,
  plannedEndTime: null,
  actualStartTime: null,
  actualEndTime: null,
  description: ''
})

const formData = reactive(emptyForm())
const formRules = {
  functionName: [{ required: true, message: '请输入功能名称', trigger: 'blur' }]
}

function openForm(row = null) {
  Object.assign(formData, emptyForm())
  if (row) {
    editingId.value = row.id
    Object.assign(formData, row)
  } else {
    editingId.value = null
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await update(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await create(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await remove(id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleLogout() {
  await logout()
  authStore.clearUser()
  router.push('/login')
}

onMounted(fetchList)
</script>

<style scoped>
.board-page {
  min-height: 100vh;
  background: #f0f6ff;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 24px;
  color: #409EFF;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.top-bar-right {
  display: flex;
  align-items: center;
}

.user-name {
  color: #606266;
  font-size: 14px;
}

.main-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 0;
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
