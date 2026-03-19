<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <div class="login-header">
        <el-icon class="logo-icon"><Grid /></el-icon>
        <h2>ONE家项目计划看板</h2>
      </div>

      <!-- 切换 Tab -->
      <div class="login-tabs">
        <div :class="['tab-item', mode === 'ioa' ? 'active' : '']" @click="mode = 'ioa'">IOA 登录</div>
        <div :class="['tab-item', mode === 'account' ? 'active' : '']" @click="mode = 'account'">账号密码</div>
      </div>

      <!-- IOA 登录 -->
      <div v-if="mode === 'ioa'" class="login-body">
        <div class="ioa-desc">
          <el-icon style="font-size:48px; color:#409eff;"><Avatar /></el-icon>
          <p>使用企业 IOA 账号一键登录</p>
          <p class="ioa-tip">请确保已在本机登录 IOA 客户端</p>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="ioaLoading"
          @click="handleIoaLogin"
          style="width: 100%; margin-top: 8px;"
        >
          IOA 一键登录
        </el-button>
      </div>

      <!-- 账号密码登录 -->
      <div v-else class="login-body">
        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%; margin-top: 8px;"
          >
            登录
          </el-button>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import { Grid } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, ioaLogin } from '../api/auth.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const ioaLoading = ref(false)
const mode = ref('ioa') // 默认 IOA 登录

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    authStore.setUser(res.data)
    ElMessage.success('登录成功')
    router.push('/board')
  } finally {
    loading.value = false
  }
}

async function handleIoaLogin() {
  ioaLoading.value = true
  try {
    const ssoRes = await fetch(
      `https://sso.wawo.cc:54339/api/public/clientlogin/auth_login?app_id=f22a6b50-f44b-4ae3-81d8-f9100a36405a&sole_id=${crypto.randomUUID()}`
    ).then(r => r.json())

    if (!ssoRes.ticket) {
      ElMessage.error('获取 IOA Ticket 失败，请确认已登录 IOA 客户端')
      return
    }

    const payload = JSON.parse(atob(ssoRes.ticket.split('.')[1]))
    const userId = payload.sub
    if (!userId) { ElMessage.error('IOA Ticket 解析失败'); return }

    const res = await ioaLogin({ userId, ticket: ssoRes.ticket })
    authStore.setUser(res.data)
    ElMessage.success('IOA 登录成功')
    router.push('/board')
  } catch {
    ElMessage.error('IOA 登录失败，请确认已登录 IOA 客户端后重试')
  } finally {
    ioaLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-image: url('/login-bg.svg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
  border-radius: 16px !important;
  border: 1px solid rgba(255,255,255,0.95) !important;
  padding: 8px 4px;
  background: rgba(255,255,255,0.92) !important;
  backdrop-filter: blur(20px);
  box-shadow: 0 12px 40px rgba(74,144,217,0.16), 0 2px 12px rgba(0,0,0,0.06) !important;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.logo-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 6px;
}
.login-header h2 {
  margin: 6px 0 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

/* ── Tab 切换 ── */
.login-tabs {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 24px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 15px;
  color: #909399;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.2s, border-color 0.2s;
}
.tab-item:hover { color: #409eff; }
.tab-item.active {
  color: #409eff;
  font-weight: 600;
  border-bottom-color: #409eff;
}

/* ── IOA 区域 ── */
.login-body { padding: 0 4px; }
.ioa-desc {
  text-align: center;
  margin-bottom: 24px;
}
.ioa-desc p {
  margin: 10px 0 0;
  color: #303133;
  font-size: 15px;
}
.ioa-tip {
  color: #909399 !important;
  font-size: 13px !important;
  margin-top: 4px !important;
}
</style>
