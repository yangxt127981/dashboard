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
        <el-row :gutter="12" align="middle">
          <el-col :span="6">
            <el-input v-model="query.functionName" placeholder="需求名称" clearable :prefix-icon="Search" />
          </el-col>
          <el-col :span="6">
            <el-input v-model="query.requestDepartment" placeholder="需求方部门" clearable :prefix-icon="OfficeBuilding" />
          </el-col>
          <el-col :span="4">
            <el-select v-model="query.priority" placeholder="优先级" multiple collapse-tags clearable style="width: 100%;">
              <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="query.status" placeholder="状态" multiple collapse-tags clearable style="width: 100%;" :disabled="activeTab !== 'all'">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" round @click="fetchList">查询</el-button>
            <el-button round @click="resetQuery">重置</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 表格 -->
      <el-card class="table-card" shadow="never">
        <!-- Tab 页签 + 工具栏 -->
        <div class="table-header">
          <el-tabs v-model="activeTab" class="board-tabs" @tab-change="handleTabChange">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="进行中" name="inProgress" />
            <el-tab-pane label="未开始" name="notStarted" />
          </el-tabs>
          <div class="table-toolbar">
          <el-button v-if="authStore.isAdmin()" type="primary" size="small" :icon="Plus" @click="openForm()">新增需求</el-button>
          <el-button size="small" @click="resetColumnOrder">重置列顺序</el-button>
          <el-popover placement="bottom-end" :width="220" trigger="click">
            <template #reference>
              <el-button :icon="Setting" size="small">列配置</el-button>
            </template>
            <div class="col-setting">
              <div class="col-setting-header">
                <span>显示列</span>
                <el-button text size="small" type="primary" @click="resetColumnOrder">重置</el-button>
              </div>
              <div class="col-setting-list">
                <div v-for="col in columns" :key="col.key" class="col-setting-item">
                  <el-checkbox
                    :model-value="col.visible"
                    :disabled="col.visible && visibleColumns.length === 1"
                    @change="toggleColumn(col.key)"
                  >{{ col.label }}</el-checkbox>
                </div>
              </div>
            </div>
          </el-popover>
          </div>
        </div>

        <el-table
          ref="tableRef"
          :data="tableData"
          v-loading="loading"
          border
          style="width: 100%"
          @sort-change="handleSortChange"
        >
          <el-table-column
            v-for="col in visibleColumns"
            :key="col.key"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth"
            :align="col.align"
            :show-overflow-tooltip="col.showOverflowTooltip"
            :sortable="col.sortable"
          >
            <template v-if="col.key === 'priority' || col.key === 'status'" #default="{ row }">
              <el-tag v-if="col.key === 'priority'" :type="priorityType(row.priority)" size="small" effect="plain">{{ row.priority }}</el-tag>
              <el-tag v-else :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" :width="authStore.isAdmin() ? 180 : 80" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
              <template v-if="authStore.isAdmin()">
                <el-button type="primary" link size="small" @click="openForm(row)">编辑</el-button>
                <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
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

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[previewUrl]"
      @close="previewVisible = false"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="需求详情" width="720px" destroy-on-close>
      <el-descriptions :column="2" border size="small" class="detail-desc">
        <el-descriptions-item label="需求名称" :span="2">{{ detailData.functionName }}</el-descriptions-item>
        <el-descriptions-item label="所属模块">{{ detailData.moduleName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求方部门">{{ detailData.requestDepartment || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求对接人">{{ detailData.requestOwner || '—' }}</el-descriptions-item>
        <el-descriptions-item label="产品对接人">{{ detailData.productOwner || '—' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityType(detailData.priority)" size="small" effect="plain">{{ detailData.priority || '—' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailData.status)" size="small">{{ detailData.status || '—' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划开始时间">{{ detailData.plannedStartTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划完成时间">{{ detailData.plannedEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="实际开始时间">{{ detailData.actualStartTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="实际完成时间">{{ detailData.actualEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">
          <span style="white-space: pre-wrap;">{{ detailData.description || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailAttachments.length > 0" label="图片附件" :span="2">
          <div class="attachment-list">
            <div v-for="att in detailAttachments" :key="att.id" class="attachment-item">
              <el-image
                :src="att.fileUrl"
                fit="cover"
                class="attachment-thumb"
                @click="openPreview(att.fileUrl)"
              />
              <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button v-if="authStore.isAdmin()" type="primary" @click="detailVisible = false; openForm(detailData)">编辑</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

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
            <el-form-item label="需求名称" prop="functionName">
              <el-input v-model="formData.functionName" placeholder="请输入需求名称" />
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
          <el-col :span="24">
            <el-form-item label="图片附件">
              <!-- 已有附件 -->
              <div class="attachment-list">
                <div v-for="att in existingAttachments" :key="att.id" class="attachment-item">
                  <el-image
                    :src="att.fileUrl"
                    fit="cover"
                    class="attachment-thumb"
                    @click="openPreview(att.fileUrl)"
                  />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button
                    v-if="authStore.isAdmin()"
                    :icon="Delete"
                    size="small"
                    type="danger"
                    text
                    circle
                    class="attachment-del"
                    @click="removeExistingAttachment(att)"
                  />
                </div>
                <!-- 新上传的附件 -->
                <div v-for="(att, i) in newAttachments" :key="'new-' + i" class="attachment-item">
                  <el-image
                    :src="att.fileUrl"
                    fit="cover"
                    class="attachment-thumb"
                    @click="openPreview(att.fileUrl)"
                  />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button
                    :icon="Delete"
                    size="small"
                    type="danger"
                    text
                    circle
                    class="attachment-del"
                    @click="removeNewAttachment(i)"
                  />
                </div>
                <!-- 上传按钮 -->
                <el-upload
                  v-if="authStore.isAdmin()"
                  action="/api/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  accept="image/*"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  class="upload-trigger"
                >
                  <div class="upload-btn">
                    <el-icon><Plus /></el-icon>
                  </div>
                </el-upload>
              </div>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, OfficeBuilding, Plus, Setting, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Sortable from 'sortablejs'
import { getList, create, update, remove } from '../api/requirement.js'
import { logout } from '../api/auth.js'
import { getAttachments, addAttachment, deleteAttachment } from '../api/attachment.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const tableRef = ref()

// 列定义
const defaultColumns = [
  { key: 'functionName',      label: '需求名称',   minWidth: 200, showOverflowTooltip: true, visible: true },
  { key: 'moduleName',        label: '所属模块',   width: 110,   showOverflowTooltip: true, visible: true },
  { key: 'requestDepartment', label: '需求方部门', width: 120,   showOverflowTooltip: true, visible: true },
  { key: 'requestOwner',      label: '需求对接人', width: 100,   visible: true },
  { key: 'productOwner',      label: '产品对接人', width: 100,   visible: true },
  { key: 'priority',          label: '优先级',     width: 110,   align: 'center', sortable: 'custom', visible: true },
  { key: 'status',            label: '状态',       width: 90,    align: 'center',   visible: true },
  { key: 'plannedStartTime',  label: '计划开始',   width: 125,   visible: true },
  { key: 'plannedEndTime',    label: '计划完成',   width: 125,   sortable: 'custom', visible: true },
  { key: 'actualStartTime',   label: '实际开始',   width: 125,   visible: true },
  { key: 'actualEndTime',     label: '实际完成',   width: 125,   sortable: 'custom', visible: true },
  { key: 'description',       label: '需求描述',   minWidth: 150, showOverflowTooltip: true, visible: true },
]

const STORAGE_KEY = 'dashboard_column_order'

function loadColumns() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const savedList = JSON.parse(saved) // [{ key, visible }]
      const sorted = savedList
        .map(({ key, visible }) => {
          const col = defaultColumns.find(c => c.key === key)
          return col ? { ...col, visible } : null
        })
        .filter(Boolean)
      // 补上新增的列
      defaultColumns.forEach(c => { if (!sorted.find(s => s.key === c.key)) sorted.push({ ...c }) })
      return sorted
    }
  } catch {}
  return defaultColumns.map(c => ({ ...c }))
}

const columns = ref(loadColumns())
const visibleColumns = computed(() => columns.value.filter(c => c.visible))

function saveColumns() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(columns.value.map(c => ({ key: c.key, visible: c.visible }))))
}

function toggleColumn(key) {
  const col = columns.value.find(c => c.key === key)
  if (!col) return
  // 至少保留一列
  if (col.visible && visibleColumns.value.length === 1) {
    ElMessage.warning('至少保留一列')
    return
  }
  col.visible = !col.visible
  saveColumns()
}

function resetColumnOrder() {
  columns.value = defaultColumns.map(c => ({ ...c }))
  localStorage.removeItem(STORAGE_KEY)
  ElMessage.success('已重置')
  nextTick(initSortable)
}

// 初始化拖拽
function initSortable() {
  const el = tableRef.value?.$el?.querySelector('.el-table__header-wrapper thead tr')
  if (!el) return
  Sortable.create(el, {
    animation: 150,
    ghostClass: 'col-drag-ghost',
    filter: '.el-table__column-filter-trigger, .is-right',  // 排除操作列
    onEnd({ newIndex, oldIndex }) {
      if (newIndex === oldIndex) return
      const moved = columns.value.splice(oldIndex, 1)[0]
      columns.value.splice(newIndex, 0, moved)
      saveColumns()
    }
  })
}

// 列表数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  functionName: '',
  requestDepartment: '',
  priority: [],
  status: [],
  page: 1,
  size: 10,
  sortField: '',
  sortOrder: ''
})

const activeTab = ref('all')
const tabStatusMap = {
  all: [],
  inProgress: ['设计中', '开发中', '测试中', '已上线'],
  notStarted: ['未开始']
}

function handleTabChange(tab) {
  query.status = tabStatusMap[tab]
  query.page = 1
  fetchList()
}

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

function handleSortChange({ prop, order }) {
  query.sortField = order ? prop : ''
  query.sortOrder = order || ''
  query.page = 1
  fetchList()
}

function resetQuery() {
  query.functionName = ''
  query.requestDepartment = ''
  query.priority = []
  query.status = []
  query.page = 1
  activeTab.value = 'all'
  fetchList()
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})
const detailAttachments = ref([])

async function openDetail(row) {
  detailData.value = { ...row }
  detailAttachments.value = []
  detailVisible.value = true
  const res = await getAttachments(row.id)
  detailAttachments.value = res.data || []
}

// 附件
const existingAttachments = ref([])
const newAttachments = ref([])
const deletedAttachmentIds = ref([])
const uploadHeaders = computed(() => ({ Authorization: authStore.token }))
const previewVisible = ref(false)
const previewUrl = ref('')

async function loadAttachments(requirementId) {
  const res = await getAttachments(requirementId)
  existingAttachments.value = res.data || []
}

function handleUploadSuccess(response, uploadFile) {
  if (response.code === 200) {
    newAttachments.value.push({ fileName: response.data.fileName, fileUrl: response.data.url })
  } else {
    ElMessage.error('上传失败')
    uploadFile.status = 'fail'
  }
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function removeExistingAttachment(att) {
  deletedAttachmentIds.value.push(att.id)
  existingAttachments.value = existingAttachments.value.filter(a => a.id !== att.id)
}

function removeNewAttachment(index) {
  newAttachments.value.splice(index, 1)
}

function openPreview(url) {
  previewUrl.value = url
  previewVisible.value = true
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
  functionName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }]
}

function openForm(row = null) {
  Object.assign(formData, emptyForm())
  existingAttachments.value = []
  newAttachments.value = []
  deletedAttachmentIds.value = []
  if (row) {
    editingId.value = row.id
    Object.assign(formData, row)
    loadAttachments(row.id)
  } else {
    editingId.value = null
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value.validate()
  submitting.value = true
  try {
    let requirementId = editingId.value
    if (requirementId) {
      await update(requirementId, formData)
    } else {
      const res = await create(formData)
      requirementId = res.data
    }
    // 处理附件
    await Promise.all(deletedAttachmentIds.value.map(id => deleteAttachment(id)))
    await Promise.all(newAttachments.value.map(att => addAttachment(requirementId, att)))
    ElMessage.success(editingId.value ? '更新成功' : '新增成功')
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

onMounted(() => {
  fetchList()
  nextTick(initSortable)
})
</script>

<style scoped>
/* ── 全局布局 ── */
.board-page {
  min-height: 100vh;
  background: #f0f2f5;
}

/* ── 顶部导航 ── */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: linear-gradient(90deg, #1a57c9 0%, #2d7cf6 100%);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(45, 124, 246, 0.3);
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 26px;
  color: #fff;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.top-bar-right {
  display: flex;
  align-items: center;
}

.top-bar-right :deep(.el-tag) {
  background: rgba(255,255,255,0.15);
  border-color: rgba(255,255,255,0.3);
  color: #fff;
}

.top-bar-right :deep(.el-divider--vertical) {
  border-color: rgba(255,255,255,0.3);
}

.user-name {
  color: rgba(255,255,255,0.9);
  font-size: 14px;
}

.top-bar-right :deep(.el-button) {
  color: rgba(255,255,255,0.85);
}

.top-bar-right :deep(.el-button:hover) {
  color: #fff;
  background: rgba(255,255,255,0.1);
}

/* ── 主内容区 ── */
.main-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── 筛选卡片 ── */
.filter-card {
  border-radius: 8px;
  border: 1px solid #e8eaed;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.filter-card :deep(.el-input__wrapper),
.filter-card :deep(.el-select__wrapper) {
  border-radius: 6px;
}

/* ── 表格卡片 ── */
.table-card {
  border-radius: 8px;
  border: 1px solid #e8eaed;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 0;
  font-size: 13px;
}

.table-card :deep(.el-table__header-wrapper th) {
  background-color: #f7f8fa;
  color: #1d2129;
  font-weight: 600;
  font-size: 13px;
}

.table-card :deep(.el-table__row:hover td) {
  background-color: #f0f7ff !important;
}

.table-card :deep(.el-table td) {
  color: #4e5969;
}

/* ── 表格头部（Tab + 工具栏）── */
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8eaed;
  padding: 0 16px;
  background: #fff;
  border-radius: 8px 8px 0 0;
}

.board-tabs {
  flex: 1;
}

.board-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.board-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.board-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.board-tabs :deep(.el-tabs__item.is-active) {
  color: #2d7cf6;
  font-weight: 600;
}

.board-tabs :deep(.el-tabs__active-bar) {
  background-color: #2d7cf6;
  height: 2px;
  border-radius: 2px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 0 0 16px;
}

/* ── 列配置弹窗 ── */
.col-setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 600;
  color: #1d2129;
}

.col-setting-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.col-setting-item {
  padding: 2px 0;
}

/* ── 分页 ── */
.pagination {
  padding: 14px 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
}

:global(.col-drag-ghost) {
  opacity: 0.5;
  background: #cce0ff;
}

/* ── 附件上传 ── */
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attachment-item {
  position: relative;
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.attachment-thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  border: 1px solid #e8eaed;
  cursor: pointer;
  object-fit: cover;
}

.attachment-name {
  font-size: 11px;
  color: #86909c;
  width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.attachment-del {
  position: absolute;
  top: -6px;
  right: -6px;
}

.upload-trigger {
  display: inline-block;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed #c0c4cc;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8c8c8c;
  font-size: 24px;
  transition: border-color 0.2s, color 0.2s;
}

.upload-btn:hover {
  border-color: #2d7cf6;
  color: #2d7cf6;
}
</style>
