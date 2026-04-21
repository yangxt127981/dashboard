<template>
  <div class="inbox-page">
    <main class="main-content">
      <section class="section-block">
        <div class="section-title">需求提报</div>

        <!-- 筛选区 -->
        <el-card class="filter-card" shadow="never">
          <el-row :gutter="12" align="middle">
            <el-col :span="5">
              <el-input v-model="query.functionName" placeholder="需求名称" clearable :prefix-icon="Search" />
            </el-col>
            <el-col :span="4">
              <el-select v-model="query.moduleName" placeholder="所属模块" clearable filterable style="width:100%;">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="query.requestDepartment" placeholder="需求方部门" clearable filterable style="width:100%;">
                <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="query.productOwner" placeholder="产品对接人" clearable filterable style="width:100%;">
                <el-option v-for="p in productOwnerOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="query.priority" placeholder="优先级" multiple collapse-tags clearable style="width:100%;">
                <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="3" style="display:flex;gap:8px;">
              <el-button type="primary" @click="fetchList">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 表格 -->
        <el-card class="table-card" shadow="never">
          <div class="table-header">
            <!-- Tab 页签 -->
            <el-tabs v-model="activeTab" class="inbox-tabs" @tab-change="handleTabChange">
              <el-tab-pane :label="`全部（${tabCounts.all || 0}）`" name="all" />
              <el-tab-pane :label="`已创建（${tabCounts['已创建'] || 0}）`" name="已创建" />
              <el-tab-pane :label="`待评估（${tabCounts['待评估'] || 0}）`" name="待评估" />
              <el-tab-pane :label="`已驳回（${tabCounts['已驳回'] || 0}）`" name="已驳回" />
              <el-tab-pane :label="`进入需求池（${tabCounts['进入需求池'] || 0}）`" name="进入需求池" />
              <el-tab-pane :label="`已取消（${tabCounts['已取消'] || 0}）`" name="已取消" />
            </el-tabs>
            <div class="table-toolbar">
              <el-button v-if="!authStore.isManager()" type="primary" size="small" @click="openForm()">新建需求</el-button>
              <el-tooltip content="刷新" placement="top">
                <el-icon class="toolbar-icon" @click="fetchList"><Refresh /></el-icon>
              </el-tooltip>
            </div>
          </div>

          <el-table :data="tableData" v-loading="loading" border style="width:100%;" @sort-change="handleSortChange">
            <el-table-column prop="functionName" label="需求名称" min-width="260" show-overflow-tooltip />
            <el-table-column prop="moduleName" label="所属模块" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.moduleName && moduleColorMap[row.moduleName]"
                  :style="{ background: moduleColorMap[row.moduleName], padding: '2px 8px', borderRadius: '3px', display: 'inline-block' }">
                  {{ row.moduleName }}
                </span>
                <span v-else>{{ row.moduleName || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="requestDepartment" label="需求方部门" width="120" show-overflow-tooltip sortable="custom" />
            <el-table-column prop="requestOwner" label="需求对接人" width="120" show-overflow-tooltip sortable="custom" />
            <el-table-column prop="productOwner" label="产品对接人" width="120" show-overflow-tooltip />
            <el-table-column prop="priority" label="优先级" width="90" align="center" sortable="custom">
              <template #default="{ row }">
                <el-tag :type="priorityType(row.priority)" size="small" effect="plain">{{ row.priority || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submissionStatus" label="提报状态" width="130" align="center" sortable="custom">
              <template #default="{ row }">
                <el-tag :type="submissionStatusType(row.submissionStatus)" size="small">{{ row.submissionStatus }}</el-tag>
                <el-tooltip v-if="row.submissionStatus === '已驳回' && row.rejectReason" :content="row.rejectReason" placement="top" :show-after="200">
                  <el-icon style="margin-left:4px;vertical-align:-2px;color:#f56c6c;cursor:pointer;"><WarningFilled /></el-icon>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="submittedBy" label="提报人" width="100">
              <template #default="{ row }">{{ displayName(row.submittedBy) }}</template>
            </el-table-column>
            <el-table-column prop="expectedOnlineDate" label="期望上线" width="115" />
            <el-table-column prop="description" label="需求价值" min-width="160" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="创建时间" width="165">
              <template #default="{ row }">{{ formatDatetime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="230" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
                <el-button type="info" link size="small" @click="openLog(row)">日志</el-button>
                <!-- 编辑：已创建 / 已驳回，或 admin 无限制 -->
                <el-button v-if="authStore.isAdmin() || ['已创建','已驳回'].includes(row.submissionStatus)" type="primary" link size="small" @click="openForm(row)">编辑</el-button>
                <!-- 提交：已创建 / 已驳回（MANAGER 隐藏） -->
                <el-popconfirm v-if="['已创建','已驳回'].includes(row.submissionStatus) && !authStore.isManager()" title="确认提交评估？" @confirm="handleSubmitEval(row)">
                  <template #reference>
                    <el-button type="success" link size="small">提交</el-button>
                  </template>
                </el-popconfirm>
                <!-- 删除：已创建，或 admin 无限制 -->
                <el-popconfirm v-if="authStore.isAdmin() || row.submissionStatus === '已创建'" title="确认删除？" @confirm="handleDelete(row)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
                </el-popconfirm>
                <!-- 撤销：待评估（MANAGER 隐藏） -->
                <el-popconfirm v-if="row.submissionStatus === '待评估' && !authStore.isManager()" title="确认撤销？" @confirm="handleWithdraw(row)">
                  <template #reference>
                    <el-button type="warning" link size="small">撤销</el-button>
                  </template>
                </el-popconfirm>
                <!-- 需求评估：待评估（MANAGER / ADMIN） -->
                <el-button v-if="row.submissionStatus === '待评估' && (authStore.isManager() || authStore.isAdmin())" type="primary" link size="small" @click="openEvaluate(row)">需求评估</el-button>
                <!-- 取消：已驳回 -->
                <el-popconfirm v-if="row.submissionStatus === '已驳回'" title="确认取消需求？" @confirm="handleArchive(row)">
                  <template #reference>
                    <el-button type="danger" link size="small">取消</el-button>
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
              @change="fetchList(false)"
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="需求详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="需求名称" :span="2">{{ detailRow.functionName }}</el-descriptions-item>
        <el-descriptions-item label="所属模块">{{ detailRow.moduleName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求方部门">{{ detailRow.requestDepartment || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求对接人">{{ detailRow.requestOwner || '—' }}</el-descriptions-item>
        <el-descriptions-item label="产品对接人">{{ detailRow.productOwner || '—' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ detailRow.priority || '—' }}</el-descriptions-item>
        <el-descriptions-item label="提报状态">
          <el-tag :type="submissionStatusType(detailRow.submissionStatus)" size="small">{{ detailRow.submissionStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提报人">{{ displayName(detailRow.submittedBy) }}</el-descriptions-item>
        <el-descriptions-item label="期望上线日期">{{ detailRow.expectedOnlineDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求价值" :span="2">{{ detailRow.description || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDatetime(detailRow.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDatetime(detailRow.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <!-- 附件 -->
      <div v-if="detailAttachments.length > 0" style="margin-top:16px;">
        <div style="font-weight:500; margin-bottom:8px;">图片附件</div>
        <div class="attachment-list">
          <div v-for="att in detailAttachments" :key="att.id" class="attachment-item">
            <el-image :src="att.fileUrl" fit="cover" class="attachment-thumb" @click="openPreview(att.fileUrl)" />
            <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="editingId ? '编辑需求' : '新建需求'"
      width="700px"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="需求名称" prop="functionName">
              <el-input v-model="formData.functionName" placeholder="请输入需求名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属模块">
              <el-select v-model="formData.moduleName" placeholder="请选择模块" clearable filterable style="width:100%;">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求方部门">
              <el-select v-model="formData.requestDepartment" placeholder="请选择部门" clearable filterable style="width:100%;">
                <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求对接人">
              <el-select v-model="formData.requestOwner" placeholder="请选择" multiple collapse-tags clearable filterable style="width:100%;">
                <el-option v-for="o in requestOwnerOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品对接人">
              <el-select v-model="formData.productOwner" placeholder="请选择" clearable filterable style="width:100%;">
                <el-option v-for="p in productOwnerOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="formData.priority" placeholder="请选择" style="width:100%;">
                <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望上线日期">
              <el-date-picker v-model="formData.expectedOnlineDate" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="需求价值" prop="description">
              <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请详细描述需求带来的价值" />
            </el-form-item>
          </el-col>
          <!-- 图片附件 -->
          <el-col :span="24">
            <el-form-item label="图片附件">
              <div class="attachment-list">
                <div v-for="att in existingAttachments" :key="att.id" class="attachment-item">
                  <el-image :src="att.fileUrl" fit="cover" class="attachment-thumb" @click="openPreview(att.fileUrl)" />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button size="small" type="danger" text class="attachment-del" @click="removeExistingAtt(att)">删除</el-button>
                </div>
                <div v-for="(att, i) in newAttachments" :key="'new-' + i" class="attachment-item">
                  <el-image :src="att.fileUrl" fit="cover" class="attachment-thumb" @click="openPreview(att.fileUrl)" />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button size="small" type="danger" text class="attachment-del" @click="removeNewAtt(i)">删除</el-button>
                </div>
                <el-upload
                  v-if="(existingAttachments.length + newAttachments.length) < 5"
                  action="/api/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  accept="image/png,image/jpeg"
                  :before-upload="beforeUpload"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  class="upload-trigger"
                >
                  <div class="upload-btn"><el-icon><Plus /></el-icon></div>
                </el-upload>
              </div>
              <div class="upload-tip">
                <el-icon style="vertical-align:-2px;margin-right:3px;"><InfoFilled /></el-icon>
                最多上传 5 张，每张不超过 10MB，支持 PNG / JPG；可 Ctrl+V 粘贴上传
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
        <el-button v-if="!editingId" type="success" :loading="submitting" @click="handleSaveAndSubmit">提交</el-button>
        <el-button v-if="editingId" type="success" :loading="submitting" @click="handleSaveAndSubmit">提交评估</el-button>
      </template>
    </el-dialog>

    <!-- 评估弹窗（MANAGER） -->
    <el-dialog v-model="evaluateVisible" title="需求评估" width="440px" destroy-on-close>
      <el-form :model="evaluateForm" label-width="80px">
        <el-form-item label="评估结论">
          <el-radio-group v-model="evaluateForm.pass">
            <el-radio :value="true">通过，进入需求池</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!evaluateForm.pass" label="驳回意见">
          <el-input v-model="evaluateForm.rejectReason" type="textarea" :rows="3" placeholder="请输入驳回意见（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluating" @click="handleEvaluate">确认</el-button>
      </template>
    </el-dialog>

    <!-- 操作日志弹窗 -->
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
              <span class="log-operator">{{ displayName(log.operator) }}</span>
            </div>
            <div class="log-body">
              <template v-if="log.operationType === '创建'">
                <div class="log-section-title">创建内容</div>
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item v-for="(label, key) in INBOX_LOG_FIELD_MAP" :key="key" :label="label">
                    {{ parseLogJson(log.afterContent)[key] || '—' }}
                  </el-descriptions-item>
                </el-descriptions>
              </template>
              <template v-else-if="log.operationType === '删除'">
                <div class="log-section-title">删除前内容</div>
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item v-for="(label, key) in INBOX_LOG_FIELD_MAP" :key="key" :label="label">
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
                    <template v-for="(label, key) in INBOX_LOG_FIELD_MAP" :key="key">
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

    <!-- 完善需求信息弹框（评估通过后） -->
    <el-dialog v-model="poolFormVisible" title="完善需求信息" width="700px" destroy-on-close>
      <el-form :model="poolFormData" :rules="poolFormRules" ref="poolFormRef" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="需求名称" prop="functionName">
              <el-input v-model="poolFormData.functionName" placeholder="请输入需求名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属模块">
              <el-select v-model="poolFormData.moduleName" placeholder="请选择模块" clearable filterable style="width:100%;">
                <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求方部门">
              <el-select v-model="poolFormData.requestDepartment" placeholder="请选择部门" clearable filterable style="width:100%;">
                <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求对接人">
              <el-select v-model="poolFormData.requestOwner" placeholder="请选择" multiple collapse-tags clearable filterable style="width:100%;">
                <el-option v-for="o in requestOwnerOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品对接人">
              <el-select v-model="poolFormData.productOwner" placeholder="请选择" clearable filterable style="width:100%;">
                <el-option v-for="p in productOwnerOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="poolFormData.priority" placeholder="请选择" style="width:100%;">
                <el-option v-for="p in priorityOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="poolFormData.status" placeholder="请选择" style="width:100%;">
                <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划开始时间">
              <el-date-picker v-model="poolFormData.plannedStartTime" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划完成时间">
              <el-date-picker v-model="poolFormData.plannedEndTime" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际开始时间">
              <el-date-picker v-model="poolFormData.actualStartTime" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际完成时间">
              <el-date-picker v-model="poolFormData.actualEndTime" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望上线日期">
              <el-date-picker v-model="poolFormData.expectedOnlineDate" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="需求价值">
              <el-input v-model="poolFormData.description" type="textarea" :rows="3" placeholder="请详细描述需求带来的价值" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图片附件">
              <div class="attachment-list">
                <div v-for="att in poolExistingAttachments" :key="att.id" class="attachment-item">
                  <el-image :src="att.fileUrl" fit="cover" class="attachment-thumb" @click="openPreview(att.fileUrl)" />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button size="small" type="danger" text class="attachment-del" @click="poolExistingAttachments.splice(poolExistingAttachments.indexOf(att),1); poolDeletedAttachmentIds.push(att.id)">删除</el-button>
                </div>
                <div v-for="(att, i) in poolNewAttachments" :key="'pnew-'+i" class="attachment-item">
                  <el-image :src="att.fileUrl" fit="cover" class="attachment-thumb" @click="openPreview(att.fileUrl)" />
                  <div class="attachment-name" :title="att.fileName">{{ att.fileName }}</div>
                  <el-button size="small" type="danger" text class="attachment-del" @click="poolNewAttachments.splice(i,1)">删除</el-button>
                </div>
                <el-upload
                  v-if="(poolExistingAttachments.length + poolNewAttachments.length) < 5"
                  action="/api/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  accept="image/png,image/jpeg"
                  :before-upload="beforeUpload"
                  :on-success="(res) => { if(res.code===200) poolNewAttachments.push({fileName:res.data.fileName,fileUrl:res.data.url}) }"
                  :on-error="() => ElMessage.error('上传失败')"
                  class="upload-trigger"
                >
                  <div class="upload-btn"><el-icon><Plus /></el-icon></div>
                </el-upload>
              </div>
              <div class="upload-tip">
                <el-icon style="vertical-align:-2px;margin-right:3px;"><InfoFilled /></el-icon>
                最多上传 5 张，每张不超过 10MB，支持 PNG / JPG
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="poolFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="poolSubmitting" @click="handlePoolSubmit">确定，进入需求列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Search, Refresh, Plus, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getInboxList, getInboxTabCounts, createInbox, updateInbox, deleteInbox, submitInbox, withdrawInbox, evaluateInbox, archiveInbox, getInboxLogs } from '../api/inbox.js'
import { update as updateRequirement } from '../api/requirement.js'
import { getAttachments, addAttachment, deleteAttachment } from '../api/attachment.js'
import { getDepartments, getModules, getRequestOwners, getProductOwners } from '../api/dict.js'
import { useAuthStore } from '../stores/auth.js'
import { displayName, priorityType, formatDatetime } from '../utils/format.js'

const authStore = useAuthStore()

// ── 字典数据 ──
const departmentOptions = ref([])
const moduleOptions = ref([])
const moduleColorMap = ref({})
const requestOwnerOptions = ref([])
const priorityOptions = ['紧急', '高', '中', '低']
const productOwnerOptions = ref([])
const statusOptions = ['未开始', '设计中', '开发中', '测试中', '已上线', '已取消']
const submissionStatusOptions = ['已创建', '待评估', '已驳回', '进入需求池', '已取消']

async function loadDicts() {
  const [deptRes, moduleRes, ownerRes, productOwnerRes] = await Promise.all([getDepartments(), getModules(), getRequestOwners(), getProductOwners()])
  departmentOptions.value = (deptRes.data || []).map(d => d.name)
  const modules = moduleRes.data || []
  moduleOptions.value = modules.map(m => m.name)
  moduleColorMap.value = Object.fromEntries(modules.filter(m => m.bgColor).map(m => [m.name, m.bgColor]))
  requestOwnerOptions.value = (ownerRes.data || []).map(o => o.name)
  productOwnerOptions.value = (productOwnerRes.data || []).map(o => o.name)
}

// ── 列表 ──
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeTab = ref('all')
const tabCounts = reactive({})

const query = reactive({
  functionName: '',
  moduleName: '',
  requestDepartment: '',
  productOwner: '',
  submissionStatus: '',
  priority: [],
  page: 1,
  size: 10,
  sortField: '',
  sortOrder: ''
})

function handleTabChange(tab) {
  query.submissionStatus = tab === 'all' ? '' : tab
  query.page = 1
  fetchList()
}

async function fetchList(refreshCounts = true) {
  loading.value = true
  try {
    const promises = [getInboxList(query)]
    if (refreshCounts) promises.push(getInboxTabCounts())
    const [listRes, countRes] = await Promise.all(promises)
    tableData.value = listRes.data.list
    total.value = listRes.data.total
    if (countRes) Object.assign(tabCounts, countRes.data || {})
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.functionName = ''
  query.moduleName = ''
  query.requestDepartment = ''
  query.productOwner = ''
  query.submissionStatus = ''
  query.priority = []
  query.page = 1
  query.sortField = ''
  query.sortOrder = ''
  activeTab.value = 'all'
  fetchList()
}

function handleSortChange({ prop, order }) {
  query.sortField = prop || ''
  query.sortOrder = order || ''
  query.page = 1
  fetchList(false)
}

// ── 操作日志 ──
const logVisible = ref(false)
const logLoading = ref(false)
const logList = ref([])

const INBOX_LOG_FIELD_MAP = {
  functionName: '需求名称',
  moduleName: '所属模块',
  requestDepartment: '需求方部门',
  requestOwner: '需求对接人',
  productOwner: '产品对接人',
  priority: '优先级',
  submissionStatus: '提报状态',
  expectedOnlineDate: '期望上线日期',
  description: '需求价值'
}

function logTagType(type) {
  return { '创建': 'success', '编辑': 'primary', '删除': 'danger', '状态变更': 'warning' }[type] || 'info'
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
    const res = await getInboxLogs(row.id)
    logList.value = res.data || []
  } finally {
    logLoading.value = false
  }
}

// ── 权限判断 ──
function canOperate(row) {
  if (authStore.isAdmin()) return true
  return authStore.username?.toLowerCase() === row.submittedBy?.toLowerCase()
}

// ── 颜色/工具 ──
function submissionStatusType(s) {
  return { '已创建': 'info', '待评估': 'warning', '已驳回': 'danger', '进入需求池': 'success', '已取消': '' }[s] || 'info'
}
// ── 详情 ──
const detailVisible = ref(false)
const detailRow = ref({})
const detailAttachments = ref([])

async function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
  const res = await getAttachments(row.id)
  detailAttachments.value = res.data || []
}

// ── 图片预览 ──
const previewVisible = ref(false)
const previewUrl = ref('')
function openPreview(url) { previewUrl.value = url; previewVisible.value = true }

// ── 附件上传 ──
const uploadHeaders = computed(() => ({ Authorization: authStore.token }))
const existingAttachments = ref([])
const newAttachments = ref([])
const deletedAttachmentIds = ref([])

function beforeUpload(file) {
  if (!['image/png', 'image/jpeg'].includes(file.type)) {
    ElMessage.error('仅支持 PNG / JPG 格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  const total = existingAttachments.value.length + newAttachments.value.length
  if (total >= 5) {
    ElMessage.error('最多上传 5 张图片')
    return false
  }
  return true
}
function handleUploadSuccess(res) {
  if (res.code === 200) {
    newAttachments.value.push({ fileName: res.data.fileName, fileUrl: res.data.url })
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}
function handleUploadError() { ElMessage.error('上传失败，请重试') }
function removeExistingAtt(att) {
  deletedAttachmentIds.value.push(att.id)
  existingAttachments.value = existingAttachments.value.filter(a => a.id !== att.id)
}
function removeNewAtt(i) { newAttachments.value.splice(i, 1) }

// ── 新建/编辑 ──
const formVisible = ref(false)
const formRef = ref()
const submitting = ref(false)
const editingId = ref(null)

// 粘贴上传
watch(formVisible, (val) => {
  if (val) document.addEventListener('paste', handlePaste)
  else document.removeEventListener('paste', handlePaste)
})
async function handlePaste(e) {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (!['image/png', 'image/jpeg'].includes(item.type)) continue
    const total = existingAttachments.value.length + newAttachments.value.length
    if (total >= 5) { ElMessage.error('最多上传 5 张图片'); return }
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
    } catch { ElMessage.error('上传失败，请重试') }
    break
  }
}

const emptyForm = () => ({
  functionName: '',
  moduleName: '',
  requestDepartment: '',
  requestOwner: [],
  productOwner: '',
  priority: '中',
  expectedOnlineDate: null,
  description: ''
})
const formData = reactive(emptyForm())
const formRules = {
  functionName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }],
  description: [{ required: true, message: '请填写需求价值', trigger: 'blur' }]
}

async function openForm(row = null) {
  Object.assign(formData, emptyForm())
  existingAttachments.value = []
  newAttachments.value = []
  deletedAttachmentIds.value = []
  if (row) {
    editingId.value = row.id
    Object.assign(formData, row)
    formData.requestOwner = row.requestOwner ? row.requestOwner.split(',').map(s => s.trim()).filter(Boolean) : []
    const res = await getAttachments(row.id)
    existingAttachments.value = res.data || []
  } else {
    editingId.value = null
    const name = displayName(authStore.username)
    if (requestOwnerOptions.value.includes(name)) formData.requestOwner = [name]
  }
  formVisible.value = true
}

async function doSave() {
  const payload = {
    ...formData,
    requestOwner: Array.isArray(formData.requestOwner) ? formData.requestOwner.join(',') : formData.requestOwner
  }
  let reqId = editingId.value
  if (reqId) {
    await updateInbox(reqId, payload)
  } else {
    const res = await createInbox(payload)
    reqId = res.data
  }
  await Promise.all(deletedAttachmentIds.value.map(id => deleteAttachment(id)))
  await Promise.all(newAttachments.value.map(att => addAttachment(reqId, att)))
  return reqId
}

async function handleSave() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await doSave()
    ElMessage.success(editingId.value ? '保存成功' : '新建成功')
    formVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleSaveAndSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const reqId = await doSave()
    await submitInbox(reqId)
    ElMessage.success('已提交评估')
    formVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

// ── 状态操作 ──
async function handleSubmitEval(row) {
  await submitInbox(row.id)
  ElMessage.success('已提交评估')
  fetchList()
}

async function handleWithdraw(row) {
  await withdrawInbox(row.id)
  ElMessage.success('已撤回')
  fetchList()
}

async function handleDelete(row) {
  await deleteInbox(row.id)
  ElMessage.success('已删除')
  fetchList()
}

async function handleArchive(row) {
  await archiveInbox(row.id)
  ElMessage.success('已取消')
  fetchList()
}

// ── 评估 ──
const evaluateVisible = ref(false)
const evaluating = ref(false)
const evaluateForm = reactive({ pass: true, rejectReason: '' })
const evaluatingRow = ref(null)

function openEvaluate(row) {
  evaluatingRow.value = row
  evaluateForm.pass = true
  evaluateForm.rejectReason = ''
  evaluateVisible.value = true
}

async function handleEvaluate() {
  if (!evaluateForm.pass && !evaluateForm.rejectReason.trim()) {
    ElMessage.warning('请填写驳回意见')
    return
  }
  evaluating.value = true
  try {
    await evaluateInbox(evaluatingRow.value.id, evaluateForm.pass, evaluateForm.rejectReason)
    evaluateVisible.value = false
    if (evaluateForm.pass) {
      await openPoolForm(evaluatingRow.value)
    } else {
      ElMessage.success('已驳回')
      fetchList()
    }
  } finally {
    evaluating.value = false
  }
}

// ── 完善需求信息（评估通过后） ──
const poolFormVisible = ref(false)
const poolSubmitting = ref(false)
const poolFormRef = ref()
const poolEditingId = ref(null)
const poolExistingAttachments = ref([])
const poolNewAttachments = ref([])
const poolDeletedAttachmentIds = ref([])

const poolFormData = reactive({
  functionName: '', moduleName: '', requestDepartment: '', requestOwner: [],
  productOwner: '', priority: '中', status: '未开始',
  plannedStartTime: null, plannedEndTime: null,
  actualStartTime: null, actualEndTime: null, expectedOnlineDate: null, description: ''
})
const poolFormRules = {
  functionName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }]
}

async function openPoolForm(row) {
  poolEditingId.value = row.id
  Object.assign(poolFormData, {
    functionName: row.functionName || '',
    moduleName: row.moduleName || '',
    requestDepartment: row.requestDepartment || '',
    requestOwner: row.requestOwner ? row.requestOwner.split(',').map(s => s.trim()).filter(Boolean) : [],
    productOwner: row.productOwner || '',
    priority: row.priority || '中',
    status: row.status || '未开始',
    plannedStartTime: row.plannedStartTime || null,
    plannedEndTime: row.plannedEndTime || null,
    actualStartTime: row.actualStartTime || null,
    actualEndTime: row.actualEndTime || null,
    expectedOnlineDate: row.expectedOnlineDate || null,
    description: row.description || ''
  })
  poolExistingAttachments.value = []
  poolNewAttachments.value = []
  poolDeletedAttachmentIds.value = []
  const res = await getAttachments(row.id)
  poolExistingAttachments.value = res.data || []
  poolFormVisible.value = true
}

async function handlePoolSubmit() {
  await poolFormRef.value.validate()
  poolSubmitting.value = true
  try {
    const payload = {
      ...poolFormData,
      requestOwner: Array.isArray(poolFormData.requestOwner) ? poolFormData.requestOwner.join(',') : poolFormData.requestOwner
    }
    await updateRequirement(poolEditingId.value, payload)
    await Promise.all(poolDeletedAttachmentIds.value.map(id => deleteAttachment(id)))
    await Promise.all(poolNewAttachments.value.map(att => addAttachment(poolEditingId.value, att)))
    ElMessage.success('需求已进入需求列表')
    poolFormVisible.value = false
    fetchList()
  } finally {
    poolSubmitting.value = false
  }
}

onMounted(() => {
  loadDicts()
  fetchList()
})
</script>

<style scoped>
/* ── 按钮高度统一 ── */
.inbox-page :deep(.el-button) {
  height: 32px;
  padding-top: 0;
  padding-bottom: 0;
  line-height: 32px;
}
.inbox-page :deep(.el-button.is-text) {
  height: 32px;
}

/* ── 全局布局 ── */
.inbox-page {
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

.inbox-tabs {
  flex: 1;
}
.inbox-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}
.inbox-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.inbox-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}
.inbox-tabs :deep(.el-tabs__item.is-active) {
  color: #2d7cf6;
  font-weight: 600;
}
.inbox-tabs :deep(.el-tabs__active-bar) {
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

/* ── 分页 ── */
.pagination {
  padding: 14px 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
}
/* 附件 */
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}
.attachment-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 88px;
}
.attachment-thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
}
.attachment-name {
  width: 80px;
  font-size: 11px;
  color: #606266;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 3px;
}
.attachment-del {
  margin-top: 2px;
  padding: 0;
  font-size: 12px;
}
.upload-trigger {
  display: inline-block;
}
.upload-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 22px;
  color: #909399;
  transition: border-color 0.2s;
}
.upload-btn:hover {
  border-color: #409eff;
  color: #409eff;
}
.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

/* ── 日志 ── */
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
</style>
