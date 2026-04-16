<template>
  <div class="board-page">
    <!-- 主内容 -->
    <main class="main-content">

      <!-- 统计面板区域 -->
      <section class="section-block">
        <div class="section-title clickable" @click="statsCollapsed = !statsCollapsed">
          <span>数据统计</span>
          <el-icon class="collapse-icon" :class="{ collapsed: statsCollapsed }">
            <ArrowDown />
          </el-icon>
        </div>
        <el-collapse-transition>
          <el-card v-show="!statsCollapsed" class="stats-card" shadow="never">
            <div class="stats-charts">
              <div class="chart-block">
                <div ref="deptChartRef" class="chart-item"></div>
              </div>
              <div class="chart-divider"></div>
              <div class="chart-block">
                <div ref="priorityChartRef" class="chart-item"></div>
              </div>
              <div class="chart-divider"></div>
              <div class="chart-block">
                <div ref="statusChartRef" class="chart-item"></div>
              </div>
            </div>
          </el-card>
        </el-collapse-transition>
      </section>

      <el-divider class="section-divider" />

      <!-- 需求列表区域 -->
      <section class="section-block">
        <div class="section-title">需求列表</div>

        <!-- 筛选区 -->
        <el-card class="filter-card" shadow="never">
          <!-- 基础筛选行 -->
          <el-row :gutter="12" align="middle">
            <el-col :span="4">
              <el-input v-model="query.functionName" placeholder="需求名称" clearable :prefix-icon="Search" />
            </el-col>
            <el-col :span="3">
              <el-select v-model="query.moduleName" placeholder="所属模块" clearable filterable style="width: 100%;">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="query.requestDepartment" placeholder="需求方部门" clearable filterable style="width: 100%;">
                <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
            <el-col :span="3">
              <el-select v-model="query.productOwner" placeholder="产品对接人" clearable style="width: 100%;">
                <el-option v-for="p in productOwnerOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="3">
              <el-select v-model="query.requestOwner" placeholder="需求对接人" clearable filterable style="width: 100%;">
                <el-option v-for="o in requestOwnerOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-col>
            <el-col :span="7" style="display:flex; align-items:center; gap:8px; flex-wrap:nowrap;">
              <el-button type="primary" @click="fetchList">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
              <el-button text type="primary" @click="advancedVisible = !advancedVisible">
                {{ advancedVisible ? '收起' : '高级查询' }}
              </el-button>
            </el-col>
          </el-row>
          <!-- 高级筛选行 -->
          <el-collapse-transition>
            <el-row v-show="advancedVisible" :gutter="12" align="middle" style="margin-top:10px;">
              <el-col :span="5">
                <el-select v-model="query.priority" placeholder="优先级" multiple collapse-tags clearable style="width: 100%;">
                  <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
                </el-select>
              </el-col>
              <el-col :span="5">
                <el-select v-model="query.status" placeholder="状态" multiple collapse-tags clearable style="width: 100%;" :disabled="activeTab !== 'all'">
                  <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
                </el-select>
              </el-col>
            </el-row>
          </el-collapse-transition>
        </el-card>

        <!-- 表格 -->
      <el-card class="table-card" shadow="never">
        <!-- Tab 页签 + 工具栏 -->
        <div class="table-header">
          <el-tabs v-model="activeTab" class="board-tabs" @tab-change="handleTabChange">
            <el-tab-pane :label="`全部（${tabCounts.all}）`" name="all" />
            <el-tab-pane :label="`进行中（${tabCounts.inProgress}）`" name="inProgress" />
            <el-tab-pane :label="`已上线（${tabCounts.online}）`" name="online" />
            <el-tab-pane :label="`未开始（${tabCounts.notStarted}）`" name="notStarted" />
            <el-tab-pane :label="`已取消（${tabCounts.cancelled}）`" name="cancelled" />
          </el-tabs>
          <div class="table-toolbar">
          <el-button v-if="authStore.hasPermission('requirement:create')" type="primary" size="small" @click="openForm()">新增需求</el-button>
          <el-button size="small" @click="resetColumnOrder">重置列顺序</el-button>
          <el-popover placement="bottom-end" :width="220" trigger="click">
            <template #reference>
              <el-icon class="toolbar-icon" title="列配置"><Setting /></el-icon>
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
          <el-tooltip content="刷新" placement="top">
            <el-icon class="toolbar-icon" @click="fetchList"><Refresh /></el-icon>
          </el-tooltip>
          </div>
        </div>

        <el-table
          v-if="authStore.hasPermission('requirement:view')"
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
            <template v-if="col.key === 'priority' || col.key === 'status' || col.key === 'moduleName' || col.key === 'createdAt' || col.key === 'updatedAt'" #default="{ row }">
              <el-tag v-if="col.key === 'priority'" :type="priorityType(row.priority)" size="small" effect="plain">{{ row.priority }}</el-tag>
              <el-tag v-else-if="col.key === 'status'" :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
              <span v-else-if="col.key === 'moduleName'">
                <span v-if="row.moduleName && moduleColorMap[row.moduleName]"
                  :style="{ background: moduleColorMap[row.moduleName], padding: '2px 8px', borderRadius: '3px', display: 'inline-block' }">
                  {{ row.moduleName }}
                </span>
                <span v-else>{{ row.moduleName }}</span>
              </span>
              <span v-else-if="col.key === 'createdAt'">{{ formatDatetime(row.createdAt) }}</span>
              <span v-else-if="col.key === 'updatedAt'">{{ formatDatetime(row.updatedAt) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="270" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
              <el-button type="info" link size="small" @click="openLog(row)">日志</el-button>
              <el-button v-if="authStore.hasPermission('requirement:edit')" type="primary" link size="small" @click="openForm(row)">编辑</el-button>
              <el-popconfirm v-if="authStore.hasPermission('requirement:cancel') && row.status !== '已取消'" title="确认取消该需求？" @confirm="handleCancel(row)">
                <template #reference>
                  <el-button type="warning" link size="small">取消</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm v-if="authStore.hasPermission('requirement:delete')" title="确认删除？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!authStore.hasPermission('requirement:view')" style="text-align:center; padding:60px 0; color:#999;">暂无查看权限</div>

        <!-- 分页 -->
        <div class="pagination" v-if="authStore.hasPermission('requirement:view')">
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
      </section>
    </main>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[previewUrl]"
      @close="previewVisible = false"
    />

    <!-- 日志弹窗 -->
    <el-dialog v-model="logVisible" title="操作日志" width="680px" destroy-on-close>
      <div v-if="logLoading" v-loading="true" style="height: 200px;"></div>
      <el-empty v-else-if="logList.length === 0" description="暂无日志记录" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="log in logList"
          :key="log.id"
          :timestamp="log.createdAt"
          placement="top"
        >
          <el-card shadow="never" class="log-card">
            <div class="log-header">
              <el-tag :type="logTagType(log.operationType)" size="small" effect="dark">{{ log.operationType }}</el-tag>
              <span class="log-operator">{{ IOA_DISPLAY_NAMES[log.operator?.toUpperCase()] || log.operator }}</span>
            </div>
            <div class="log-body">
              <template v-if="log.operationType === '创建'">
                <div class="log-section-title">创建内容</div>
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item v-for="(label, key) in LOG_FIELD_MAP" :key="key" :label="label">
                    {{ parseLogJson(log.afterContent)[key] || '—' }}
                  </el-descriptions-item>
                </el-descriptions>
              </template>
              <template v-else-if="log.operationType === '删除'">
                <div class="log-section-title">删除前内容</div>
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item v-for="(label, key) in LOG_FIELD_MAP" :key="key" :label="label">
                    {{ parseLogJson(log.beforeContent)[key] || '—' }}
                  </el-descriptions-item>
                </el-descriptions>
              </template>
              <template v-else>
                <table class="log-diff-table">
                  <thead>
                    <tr><th>字段</th><th>修改前</th><th>修改后</th></tr>
                  </thead>
                  <tbody>
                    <template v-for="(label, key) in LOG_FIELD_MAP" :key="key">
                      <tr v-if="parseLogJson(log.beforeContent)[key] !== parseLogJson(log.afterContent)[key]" class="log-diff-row">
                        <td>{{ label }}</td>
                        <td class="log-before">{{ parseLogJson(log.beforeContent)[key] || '—' }}</td>
                        <td class="log-after">{{ parseLogJson(log.afterContent)[key] || '—' }}</td>
                      </tr>
                    </template>
                  </tbody>
                </table>
              </template>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="logVisible = false">关闭</el-button>
      </template>
    </el-dialog>

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
        <el-descriptions-item label="期望上线日期">{{ detailData.expectedOnlineDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求价值" :span="2">
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
        <el-button v-if="authStore.canEdit()" type="primary" @click="detailVisible = false; openForm(detailData)">编辑</el-button>
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
          <el-col :span="24">
            <el-form-item label="需求名称" prop="functionName">
              <el-input v-model="formData.functionName" placeholder="请输入需求名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属模块">
              <el-select v-model="formData.moduleName" placeholder="请选择模块" clearable filterable style="width:100%;">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求方部门">
              <el-select v-model="formData.requestDepartment" placeholder="请选择需求方部门" clearable filterable style="width: 100%;">
                <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求对接人">
              <el-select v-model="formData.requestOwner" placeholder="请选择需求对接人" multiple collapse-tags collapse-tags-tooltip clearable filterable style="width: 100%;">
                <el-option v-for="o in requestOwnerOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品对接人">
              <el-select v-model="formData.productOwner" placeholder="请选择产品对接人" clearable filterable allow-create style="width: 100%;">
                <el-option v-for="p in productOwnerOptions" :key="p" :label="p" :value="p" />
              </el-select>
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
          <el-col :span="12">
            <el-form-item label="期望上线日期">
              <el-date-picker v-model="formData.expectedOnlineDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="需求价值">
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
                    v-if="authStore.canEdit()"
                    size="small"
                    type="danger"
                    text
                    class="attachment-del"
                    @click="removeExistingAttachment(att)"
                  >删除</el-button>
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
                    size="small"
                    type="danger"
                    text
                    class="attachment-del"
                    @click="removeNewAttachment(i)"
                  >删除</el-button>
                </div>
                <!-- 上传按钮：未达5张时显示 -->
                <el-upload
                  v-if="authStore.canEdit() && (existingAttachments.length + newAttachments.length) < 5"
                  action="/api/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  accept="image/png,image/jpeg"
                  :before-upload="beforeUpload"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  class="upload-trigger"
                >
                  <div class="upload-btn">
                    <el-icon><Plus /></el-icon>
                  </div>
                </el-upload>
              </div>
              <div class="upload-tip"><el-icon style="vertical-align: -2px; margin-right: 3px;"><InfoFilled /></el-icon>最多上传 5 张，每张不超过 10MB，支持 PNG / JPG；可直接 Ctrl+V 粘贴图片上传</div>
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
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Plus, ArrowDown, Setting, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Sortable from 'sortablejs'
import { getList, getStats, getTabCounts, create, update, remove } from '../api/requirement.js'
import { getLogs } from '../api/log.js'
import { getAttachments, addAttachment, deleteAttachment } from '../api/attachment.js'
import { getDepartments, getModules, getRequestOwners } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'
import * as echarts from 'echarts'
import { IOA_DISPLAY_NAMES, displayName, priorityType, formatDatetime } from '../utils/format.js'

const authStore = useAuthStore()
const tableRef = ref()

// 列定义
const defaultColumns = [
  { key: 'functionName',      label: '需求名称',   minWidth: 300, showOverflowTooltip: true, visible: true },
  { key: 'moduleName',        label: '所属模块',   width: 130,   showOverflowTooltip: true, visible: true, sortable: 'custom' },
  { key: 'requestDepartment', label: '需求方部门', width: 120,   showOverflowTooltip: true, visible: true },
  { key: 'requestOwner',      label: '需求对接人', width: 100,   visible: true },
  { key: 'productOwner',      label: '产品对接人', width: 100,   visible: true },
  { key: 'priority',          label: '优先级',     width: 110,   align: 'center', sortable: 'custom', visible: true },
  { key: 'status',            label: '状态',       width: 90,    align: 'center',   visible: true },
  { key: 'plannedStartTime',  label: '计划开始',   width: 125,   sortable: 'custom', visible: true },
  { key: 'plannedEndTime',    label: '计划完成',   width: 125,   sortable: 'custom', visible: true },
  { key: 'actualStartTime',   label: '实际开始',   width: 125,   sortable: 'custom', visible: true },
  { key: 'actualEndTime',     label: '实际完成',   width: 125,   sortable: 'custom', visible: true },
  { key: 'expectedOnlineDate', label: '期望上线',   width: 125,   visible: true },
  { key: 'description',       label: '需求价值',   minWidth: 150, showOverflowTooltip: true, visible: true },
  { key: 'createdAt',         label: '创建时间',   width: 165,   sortable: 'custom', visible: true },
  { key: 'updatedAt',         label: '更新时间',   width: 165,   sortable: 'custom', visible: true },
]

const storageKey = computed(() => `dashboard_column_order_${authStore.username || 'default'}`)

function loadColumns() {
  try {
    const saved = localStorage.getItem(storageKey.value)
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
  localStorage.setItem(storageKey.value, JSON.stringify(columns.value.map(c => ({ key: c.key, visible: c.visible }))))
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
  localStorage.removeItem(storageKey.value)
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
  moduleName: '',
  productOwner: '',
  requestOwner: '',
  priority: [],
  status: [],
  page: 1,
  size: 10,
  sortField: '',
  sortOrder: ''
})

const advancedVisible = ref(false)
const activeTab = ref('all')
const tabCounts = reactive({ all: 0, inProgress: 0, online: 0, notStarted: 0, cancelled: 0 })
const tabStatusMap = {
  all: [],
  inProgress: ['设计中', '开发中', '测试中'],
  notStarted: ['未开始'],
  online: ['已上线'],
  cancelled: ['已取消']
}

function handleTabChange(tab) {
  query.status = tabStatusMap[tab]
  query.page = 1
  fetchList()
}

const statusOptions = ['未开始', '设计中', '开发中', '测试中', '已上线', '已取消']
const priorityOptions = ['紧急', '高', '中', '低']
const productOwnerOptions = ['刘秋诗', '赵轶群', '丁滢', 'Hanson', '张明洋', '邵森伟']
const departmentOptions = ref([])
const moduleOptions = ref([])
const moduleColorMap = ref({})
const requestOwnerOptions = ref([])

async function loadDictOptions() {
  const [deptRes, moduleRes, ownerRes] = await Promise.all([getDepartments(), getModules(), getRequestOwners()])
  departmentOptions.value = (deptRes.data || []).map(d => d.name)
  const modules = moduleRes.data || []
  moduleOptions.value = modules.map(m => m.name)
  moduleColorMap.value = Object.fromEntries(modules.filter(m => m.bgColor).map(m => [m.name, m.bgColor]))
  requestOwnerOptions.value = (ownerRes.data || []).map(o => o.name)
}

function statusType(status) {
  const map = { '未开始': 'info', '设计中': 'warning', '开发中': 'primary', '测试中': '', '已上线': 'success', '已取消': 'danger' }
  return map[status] ?? 'info'
}


async function fetchList() {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([getList(query), getTabCounts(query)])
    tableData.value = listRes.data.list
    total.value = listRes.data.total
    const c = countRes.data || {}
    tabCounts.all = c.all || 0
    tabCounts.inProgress = c.inProgress || 0
    tabCounts.online = c.online || 0
    tabCounts.notStarted = c.notStarted || 0
    tabCounts.cancelled = c.cancelled || 0
  } finally {
    loading.value = false
  }
  fetchStats(selectedDept.value)
}

function handleSortChange({ prop, order }) {
  query.sortField = order ? prop : ''
  query.sortOrder = order || ''
  query.page = 1
  fetchList()
}

function resetQuery() {
  query.functionName = ''
  query.moduleName = ''
  query.requestDepartment = ''
  query.productOwner = ''
  query.requestOwner = ''
  query.priority = []
  query.status = []
  query.page = 1
  activeTab.value = 'all'
  advancedVisible.value = false
  fetchList()
}

// 日志弹窗
const logVisible = ref(false)
const logLoading = ref(false)
const logList = ref([])

const LOG_FIELD_MAP = {
  functionName: '需求名称',
  moduleName: '所属模块',
  requestDepartment: '需求方部门',
  requestOwner: '需求对接人',
  productOwner: '产品对接人',
  priority: '优先级',
  status: '状态',
  plannedStartTime: '计划开始时间',
  plannedEndTime: '计划完成时间',
  actualStartTime: '实际开始时间',
  actualEndTime: '实际完成时间',
  description: '需求价值'
}

function logTagType(type) {
  return { '创建': 'success', '编辑': 'primary', '删除': 'danger' }[type] || 'info'
}

function parseLogJson(str) {
  if (!str) return {}
  try { return JSON.parse(str) } catch { return {} }
}

async function openLog(row) {
  logList.value = []
  logLoading.value = true
  logVisible.value = true
  try {
    const res = await getLogs(row.id)
    logList.value = res.data || []
  } finally {
    logLoading.value = false
  }
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

function beforeUpload(file) {
  const allowed = ['image/png', 'image/jpeg']
  if (!allowed.includes(file.type)) {
    ElMessage.error('仅支持 PNG、JPG/JPEG 格式')
    return false
  }
  const total = existingAttachments.value.length + newAttachments.value.length
  if (total >= 5) {
    ElMessage.error('最多上传 5 张图片')
    return false
  }
  return true
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

async function handlePaste(event) {
  if (!authStore.canEdit()) return
  const items = event.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (!['image/png', 'image/jpeg'].includes(item.type)) continue
    const total = existingAttachments.value.length + newAttachments.value.length
    if (total >= 5) {
      ElMessage.error('最多上传 5 张图片')
      return
    }
    const file = item.getAsFile()
    if (!file) continue
    const ext = item.type === 'image/png' ? '.png' : '.jpg'
    const namedFile = new File([file], `paste_${Date.now()}${ext}`, { type: item.type })
    const formData = new FormData()
    formData.append('file', namedFile)
    try {
      const res = await fetch('/api/upload', {
        method: 'POST',
        headers: { Authorization: authStore.token },
        body: formData
      })
      const json = await res.json()
      if (json.code === 200) {
        newAttachments.value.push({ fileName: json.data.fileName, fileUrl: json.data.url })
        ElMessage.success('图片已粘贴上传')
      } else {
        ElMessage.error(json.message || '上传失败')
      }
    } catch {
      ElMessage.error('上传失败，请重试')
    }
    break
  }
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
watch(dialogVisible, (val) => {
  if (val) {
    document.addEventListener('paste', handlePaste)
  } else {
    document.removeEventListener('paste', handlePaste)
  }
})
const submitting = ref(false)
const editingId = ref(null)
const dialogFormRef = ref()

const emptyForm = () => ({
  functionName: '',
  moduleName: '',
  requestDepartment: '',
  requestOwner: [],
  productOwner: '',
  priority: '中',
  status: '未开始',
  plannedStartTime: null,
  plannedEndTime: null,
  actualStartTime: null,
  actualEndTime: null,
  expectedOnlineDate: null,
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
    formData.requestOwner = row.requestOwner ? row.requestOwner.split(',').map(s => s.trim()).filter(Boolean) : []
    loadAttachments(row.id)
  } else {
    editingId.value = null
    const name = displayName(authStore.username)
    if (requestOwnerOptions.value.includes(name)) formData.requestOwner = [name]
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await dialogFormRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...formData, requestOwner: Array.isArray(formData.requestOwner) ? formData.requestOwner.join(',') : formData.requestOwner }
    let requirementId = editingId.value
    if (requirementId) {
      await update(requirementId, payload)
    } else {
      const res = await create(payload)
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

async function handleCancel(row) {
  await update(row.id, { ...row, status: '已取消' })
  ElMessage.success('已取消')
  fetchList()
}

async function handleDelete(id) {
  await remove(id)
  ElMessage.success('删除成功')
  fetchList()
}

// 统计折叠 & 联动
const statsCollapsed = ref(false)
const selectedDept = ref(null)

// 统计图表
const deptChartRef = ref()
const priorityChartRef = ref()
const statusChartRef = ref()
let deptChart = null
let priorityChart = null
let statusChart = null

const DEPT_LIST = computed(() => departmentOptions.value.length ? departmentOptions.value : ['商品选品部','商品合规部','美妆支持中心','财务部','时尚事业部','信息安全部','法律合规部','公共传播部','直播现场运营部','业务增长部','所有女生直播间','商品计划部','招商部','美妆国货部'])
const PRIORITY_LIST = ['紧急','高','中','低']
const STATUS_LIST = ['未开始','设计中','开发中','测试中','已上线','已取消']

const PRIORITY_COLORS = { '紧急':'#f56c6c','高':'#e6a23c','中':'#409eff','低':'#909399' }
const STATUS_COLORS   = { '未开始':'#909399','设计中':'#e6a23c','开发中':'#409eff','测试中':'#67c23a','已上线':'#67c23a','已取消':'#f56c6c' }

function buildPieOption(title, data, colorMap, manyItems = false, total = null) {
  const filtered = data.filter(d => d.value > 0)
  const center = manyItems ? ['50%', '54%'] : ['50%', '54%']
  const radius = manyItems ? ['30%', '52%'] : ['32%', '55%']
  const graphic = total != null ? [{
    type: 'group',
    left: 'center',
    top: 'center',
    children: [
      { type: 'text', style: { text: '需求总数', fill: '#999', fontSize: 11, textAlign: 'center' }, top: -14 },
      { type: 'text', style: { text: String(total), fill: '#1d2129', fontSize: 20, fontWeight: 'bold', textAlign: 'center' }, top: 4 }
    ]
  }] : []
  return {
    title: { text: title, left: 'center', top: 8, textStyle: { fontSize: 13, fontWeight: 600, color: '#1d2129' } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { show: false },
    graphic,
    series: [{
      type: 'pie',
      radius,
      center,
      data: filtered,
      label: {
        show: true,
        position: 'outside',
        formatter: (params) => `${params.name} ${params.value} (${params.percent}%)`,
        fontSize: 11,
        color: '#555',
        overflow: 'truncate',
        width: 90
      },
      labelLine: { show: true, length: 10, length2: 14 },
      emphasis: {
        label: { fontSize: 12, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      itemStyle: {
        color: colorMap ? (params) => colorMap[params.name] || undefined : undefined
      }
    }]
  }
}

async function fetchStats(dept) {
  const res = await getStats(dept)
  const raw = res.data

  // 部门：预设部门 + DB 中不在预设列表里的部门（如旧数据）
  const deptMap = Object.fromEntries((raw.department || []).map(d => [d.name, Number(d.value)]))
  const allDepts = [...DEPT_LIST.value]
  Object.keys(deptMap).forEach(name => { if (!allDepts.includes(name)) allDepts.push(name) })
  const deptData = allDepts.map(name => ({
    name,
    value: deptMap[name] || 0,
    selected: name === dept,
    itemStyle: name === dept ? { borderWidth: 3, borderColor: '#2d7cf6' } : {}
  }))

  // 优先级：保持固定顺序
  const priMap = Object.fromEntries((raw.priority || []).map(d => [d.name, Number(d.value)]))
  const priData = PRIORITY_LIST.map(name => ({ name, value: priMap[name] || 0 }))

  // 状态：保持固定顺序
  const statusMap = Object.fromEntries((raw.status || []).map(d => [d.name, Number(d.value)]))
  const statusData = STATUS_LIST.map(name => ({ name, value: statusMap[name] || 0 }))

  deptChart?.setOption(buildPieOption('部门分布统计（点击部门可联动分析）', deptData, null, true))

  const suffix = dept ? `（${dept}）` : ''
  priorityChart?.setOption(buildPieOption(`优先级分布统计${suffix}`, priData, PRIORITY_COLORS))
  const statusTotal = statusData.reduce((sum, d) => sum + d.value, 0)
  statusChart?.setOption(buildPieOption(`状态分布统计${suffix}`, statusData, STATUS_COLORS, false, statusTotal))
}

function initCharts() {
  deptChart = echarts.init(deptChartRef.value)
  priorityChart = echarts.init(priorityChartRef.value)
  statusChart = echarts.init(statusChartRef.value)

  // 部门饼图点击联动
  deptChart.on('click', (params) => {
    if (params.componentType !== 'series') return
    if (selectedDept.value === params.name) {
      // 再次点击取消选中
      selectedDept.value = null
      fetchStats(null)
    } else {
      selectedDept.value = params.name
      fetchStats(params.name)
    }
  })

  fetchStats(null)
}

onMounted(() => {
  loadDictOptions()
  fetchList()
  nextTick(() => {
    initSortable()
    initCharts()
  })
})

onUnmounted(() => {
  deptChart?.dispose()
  priorityChart?.dispose()
  statusChart?.dispose()
})

</script>

<style scoped>
/* ── 工具栏图标 ── */
.toolbar-icon {
  font-size: 17px;
  color: #606266;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s, background 0.2s;
}
.toolbar-icon:hover {
  color: #409eff;
  background: #f0f2f5;
}

/* ── 按钮高度统一 ── */
.board-page :deep(.el-button) {
  height: 32px;
  padding-top: 0;
  padding-bottom: 0;
  line-height: 32px;
}
.board-page :deep(.el-button.is-text) {
  height: 32px;
}

/* ── 全局布局 ── */
.board-page {
  background: #f0f2f5;
}

/* ── 主内容区 ── */
.main-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── 区域块 ── */
.section-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  padding-left: 10px;
  border-left: 3px solid #2d7cf6;
  line-height: 1;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title.clickable {
  cursor: pointer;
  user-select: none;
}

.section-title.clickable:hover {
  color: #2d7cf6;
}

.collapse-icon {
  font-size: 13px;
  transition: transform 0.25s;
}

.collapse-icon.collapsed {
  transform: rotate(-90deg);
}

.section-divider {
  margin: 4px 0;
}

/* ── 统计卡片 ── */
.stats-card {
  border-radius: 8px;
  border: 1px solid #e8eaed;
}

.stats-card :deep(.el-card__body) {
  padding: 8px 16px;
}

.stats-charts {
  display: flex;
  align-items: stretch;
}

.chart-block {
  flex: 1;
  min-width: 0;
}

.chart-item {
  width: 100%;
  height: 220px;
}

.chart-divider {
  width: 1px;
  background: #e8eaed;
  margin: 8px 0;
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

/* ── 日志弹窗 ── */
.log-card {
  border: 1px solid #e8eaed !important;
}

.log-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.log-operator {
  font-size: 13px;
  color: #4e5969;
  font-weight: 500;
}

.log-section-title {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 6px;
}

.log-body {
  font-size: 13px;
}

.log-diff-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.log-diff-table th {
  background: #f7f8fa;
  color: #1d2129;
  font-weight: 600;
  padding: 6px 10px;
  border: 1px solid #e8eaed;
  text-align: left;
}

.log-diff-table td {
  padding: 6px 10px;
  border: 1px solid #e8eaed;
  color: #4e5969;
  vertical-align: top;
}

.log-before {
  background: #fff2f0;
  color: #cf1322;
  text-decoration: line-through;
}

.log-after {
  background: #f6ffed;
  color: #389e0d;
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

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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
