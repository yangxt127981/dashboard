<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">登录日志</span>
      <el-button size="small" :icon="Refresh" circle @click="fetchLoginLogs" />
    </div>

    <div v-if="!authStore.hasPermission('system:login-log')" style="padding:40px;text-align:center;color:#909399;">
      暂无权限
    </div>

    <template v-else>
      <!-- 筛选栏 -->
      <div style="display:flex;align-items:center;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
        <el-input
          v-model="logQuery.username"
          placeholder="账号名"
          clearable
          size="small"
          style="width:180px;"
          @keyup.enter="fetchLoginLogs"
        />
        <el-select
          v-model="logQuery.loginType"
          placeholder="登录方式"
          clearable
          size="small"
          style="width:140px;"
        >
          <el-option label="账号密码" value="账号密码" />
          <el-option label="IOA" value="IOA" />
        </el-select>
        <el-button type="primary" size="small" @click="fetchLoginLogs">查询</el-button>
        <el-button size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="logLoading"
        :data="loginLogList"
        border
        size="small"
        style="width:100%;"
      >
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column label="登录方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.loginType === 'IOA' ? 'success' : 'primary'" size="small">
              {{ row.loginType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginIp" label="登录IP" width="140" />
        <el-table-column prop="loginTime" label="登录时间" min-width="160" />
        <el-table-column prop="logoutTime" label="退出时间" min-width="160">
          <template #default="{ row }">
            {{ row.logoutTime || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="停留时长" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.durationMinutes != null">{{ row.durationMinutes }} 分钟</span>
            <span v-else style="color:#c0c4cc;">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '在线' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览器" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-size:12px;color:#909399;">{{ row.userAgent || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="display:flex;justify-content:flex-end;margin-top:14px;">
        <el-pagination
          v-model:current-page="logQuery.page"
          v-model:page-size="logQuery.size"
          :total="logTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          small
          @size-change="fetchLoginLogs"
          @current-change="fetchLoginLogs"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getLoginLogs } from '../api/loginLog.js'
import { useAuthStore } from '../stores/auth.js'

const authStore = useAuthStore()

const logLoading = ref(false)
const loginLogList = ref([])
const logTotal = ref(0)
const logQuery = reactive({ username: '', loginType: '', page: 1, size: 20 })

onMounted(fetchLoginLogs)

async function fetchLoginLogs() {
  if (!authStore.hasPermission('system:login-log')) return
  logLoading.value = true
  try {
    const res = await getLoginLogs(logQuery)
    loginLogList.value = res.data.list || []
    logTotal.value = res.data.total || 0
  } finally {
    logLoading.value = false
  }
}

function resetQuery() {
  logQuery.username = ''
  logQuery.loginType = ''
  logQuery.page = 1
  fetchLoginLogs()
}
</script>

<style scoped>
.page-container { padding: 20px 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; padding-left: 10px; border-left: 3px solid #409eff; }
</style>
