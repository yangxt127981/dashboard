<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <div class="login-header">
        <el-icon class="logo-icon"><Grid /></el-icon>
        <h2>项目计划看板</h2>
        <p>请登录以继续</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
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

      <el-divider>或</el-divider>

      <el-button
        size="large"
        :loading="ioaLoading"
        @click="handleIoaLogin"
        style="width: 100%;"
      >
        IOA 一键登录
      </el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, ioaLogin } from '../api/auth.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)
const ioaLoading = ref(false)

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
    // 调用本机 IOA 客户端获取 Ticket
    const ssoRes = await fetch(
      `https://sso.wawo.cc:54339/api/public/clientlogin/auth_login?app_id=f22a6b50-f44b-4ae3-81d8-f9100a36405a&sole_id=${crypto.randomUUID()}`
    ).then(r => r.json())

    if (!ssoRes.ticket) {
      ElMessage.error('获取 IOA Ticket 失败，请确认已登录 IOA 客户端')
      return
    }

    // 解析 Ticket（JWT payload）获取工号
    const payload = JSON.parse(atob(ssoRes.ticket.split('.')[1]))
    const userId = payload.sub

    if (!userId) {
      ElMessage.error('IOA Ticket 解析失败')
      return
    }

    const res = await ioaLogin({ userId, ticket: ssoRes.ticket })
    authStore.setUser(res.data)
    ElMessage.success('IOA 登录成功')
    router.push('/board')
  } catch (e) {
    ElMessage.error('IOA 登录失败，请确认已登录 IOA 客户端后重试')
  } finally {
    ioaLoading.value = false
  }
}
</script>

<style scoped>
/* ── 页面容器 ── */
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

/* ── 登录卡片 ── */
.login-card {
  position: relative;
  z-index: 10;
  width: 380px;
  border-radius: 16px !important;
  border: 1px solid rgba(255,255,255,0.95) !important;
  padding: 12px;
  background: rgba(255,255,255,0.92) !important;
  backdrop-filter: blur(20px);
  box-shadow: 0 12px 40px rgba(74,144,217,0.16), 0 2px 12px rgba(0,0,0,0.06) !important;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.logo-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 8px;
}
.login-header h2 {
  margin: 8px 0 4px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}
.login-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
</style>
