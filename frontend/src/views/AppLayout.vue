<template>
  <div class="app-layout">
    <!-- 顶部导航 -->
    <header class="top-bar">
      <div class="top-bar-left">
        <el-icon class="logo"><Grid /></el-icon>
        <span class="title">ONE家项目计划看板</span>
      </div>
      <div class="top-bar-right">
        <el-tag type="info" effect="plain" style="margin-right: 12px;">
          {{ roleBadgeLabel }}
        </el-tag>
        <span class="user-name">{{ displayUsername }}</span>
        <el-divider direction="vertical" />
        <el-button text type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <div class="layout-body">
      <!-- 左侧导航栏 -->
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <nav class="sidebar-nav">

          <!-- 需求进度 -->
          <el-tooltip :content="sidebarCollapsed ? '需求进度' : ''" placement="right" :disabled="!sidebarCollapsed">
            <div class="nav-item" :class="{ active: currentSection === 'board' }" @click="router.push('/board')">
              <el-icon><DataBoard /></el-icon>
              <span class="nav-label">需求进度</span>
            </div>
          </el-tooltip>

          <!-- 需求提报 -->
          <el-tooltip :content="sidebarCollapsed ? '需求提报' : ''" placement="right" :disabled="!sidebarCollapsed">
            <div class="nav-item" :class="{ active: currentSection === 'inbox' }" @click="router.push('/inbox')">
              <el-icon><Promotion /></el-icon>
              <span class="nav-label">需求提报</span>
            </div>
          </el-tooltip>

          <!-- 系统管理 -->
          <template v-if="hasAnySystemPermission">
            <el-tooltip :content="sidebarCollapsed ? '系统管理' : ''" placement="right" :disabled="!sidebarCollapsed">
              <div
                class="nav-item nav-item-group"
                :class="{ active: currentSection === 'system' && sidebarCollapsed }"
                @click="toggleSystem"
              >
                <el-icon><Setting /></el-icon>
                <span class="nav-label">系统管理</span>
                <el-icon v-if="!sidebarCollapsed" class="arrow-icon" :class="{ rotated: systemExpanded }">
                  <ArrowDown />
                </el-icon>
              </div>
            </el-tooltip>

            <!-- 子菜单 -->
            <transition name="submenu">
              <div v-show="systemExpanded && !sidebarCollapsed" class="submenu">
                <div
                  v-if="authStore.hasPermission('system:requestowner')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/request-owner' }"
                  @click="router.push('/system/request-owner')"
                >
                  <el-icon><User /></el-icon>
                  <span class="nav-label">需求对接人维护</span>
                </div>
                <div
                  v-if="authStore.hasPermission('system:dept')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/dept' }"
                  @click="router.push('/system/dept')"
                >
                  <el-icon><OfficeBuilding /></el-icon>
                  <span class="nav-label">需求方部门维护</span>
                </div>
                <div
                  v-if="authStore.hasPermission('system:module')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/module' }"
                  @click="router.push('/system/module')"
                >
                  <el-icon><Menu /></el-icon>
                  <span class="nav-label">需求模块维护</span>
                </div>
                <div
                  v-if="authStore.hasPermission('system:user')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/users' }"
                  @click="router.push('/system/users')"
                >
                  <el-icon><Avatar /></el-icon>
                  <span class="nav-label">用户管理</span>
                </div>
                <div
                  v-if="authStore.hasPermission('system:role')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/roles' }"
                  @click="router.push('/system/roles')"
                >
                  <el-icon><Key /></el-icon>
                  <span class="nav-label">角色管理</span>
                </div>
                <div
                  v-if="authStore.hasPermission('system:login-log')"
                  class="nav-item nav-item-sub"
                  :class="{ active: route.path === '/system/login-log' }"
                  @click="router.push('/system/login-log')"
                >
                  <el-icon><Document /></el-icon>
                  <span class="nav-label">登录日志</span>
                </div>
              </div>
            </transition>
          </template>
        </nav>

        <!-- 收起/展开按钮 -->
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon class="toggle-icon" :class="{ rotated: sidebarCollapsed }"><DArrowLeft /></el-icon>
          <span v-show="!sidebarCollapsed" class="toggle-label">收起</span>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Grid, DataBoard, Promotion, Setting, User, OfficeBuilding, Avatar, Key,
  Document, ArrowDown, DArrowLeft, Menu
} from '@element-plus/icons-vue'
import { logout } from '../api/auth.js'
import { useAuthStore } from '../stores/auth.js'
import { IOA_DISPLAY_NAMES } from '../utils/format.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const systemExpanded = ref(true)  // 默认展开

const BUILT_IN_ROLE_LABELS = { ADMIN: '管理员', MANAGER: '产品经理', USER: '普通用户' }
const displayUsername = computed(() => IOA_DISPLAY_NAMES[authStore.username?.toUpperCase()] || authStore.username)
const roleBadgeLabel = computed(() => {
  if (authStore.role) return BUILT_IN_ROLE_LABELS[authStore.role] || authStore.role
  return '自定义角色'
})
const hasAnySystemPermission = computed(() =>
  ['system:dept', 'system:module', 'system:user', 'system:login-log', 'system:role', 'system:requestowner']
    .some(p => authStore.hasPermission(p))
)

// 当前所属导航区域：board / inbox / system
const currentSection = computed(() => {
  if (route.path.startsWith('/system')) return 'system'
  if (route.path === '/inbox') return 'inbox'
  return 'board'
})

function toggleSystem() {
  if (!sidebarCollapsed.value) systemExpanded.value = !systemExpanded.value
}

async function handleLogout() {
  try { await logout() } catch {}
  authStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* ── 顶部 ── */
.top-bar {
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 100;
}
.top-bar-left { display: flex; align-items: center; gap: 8px; }
.logo { font-size: 22px; color: #409eff; }
.title { font-size: 17px; font-weight: 600; color: #303133; white-space: nowrap; }
.top-bar-right { display: flex; align-items: center; gap: 4px; }
.user-name { font-size: 14px; color: #606266; }

/* ── 主体 ── */
.layout-body { display: flex; flex: 1; overflow: hidden; }

/* ── 侧边栏 ── */
.sidebar {
  width: 188px;
  min-width: 188px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease, min-width 0.25s ease;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar.collapsed { width: 52px; min-width: 52px; }

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

/* ── 导航项 ── */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
  transition: background 0.15s, color 0.15s;
  user-select: none;
}
.nav-item:hover { background: #f0f7ff; color: #409eff; }
.nav-item.active { background: #ecf5ff; color: #409eff; font-weight: 500; }
.nav-item .el-icon { font-size: 16px; flex-shrink: 0; }

/* 系统管理父项：箭头右对齐 */
.nav-item-group { position: relative; }
.arrow-icon {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.2s;
  color: #909399;
}
.arrow-icon.rotated { transform: rotate(-180deg); }

/* 子菜单项 */
.nav-item-sub { padding-left: 36px; font-size: 13px; }
.nav-item-sub:hover { background: #f5f7fa; color: #409eff; }
.nav-item-sub.active { background: #ecf5ff; color: #409eff; font-weight: 500; }

/* label 折叠隐藏 */
.nav-label {
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.2s;
  flex: 1;
}
.sidebar.collapsed .nav-label { opacity: 0; width: 0; flex: 0; }

/* 子菜单展开动画 */
.submenu-enter-active,
.submenu-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s;
  overflow: hidden;
  max-height: 400px;
}
.submenu-enter-from,
.submenu-leave-to { max-height: 0; opacity: 0; }

/* ── 收起按钮 ── */
.sidebar-toggle {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  border-top: 1px solid #e4e7ed;
  transition: color 0.15s, background 0.15s;
  user-select: none;
  padding: 0 16px;
  white-space: nowrap;
}
.sidebar-toggle:hover { color: #409eff; background: #f0f7ff; }
.toggle-icon {
  font-size: 15px;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}
.toggle-icon.rotated { transform: rotate(180deg); }

/* ── 主内容 ── */
.main-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: #f5f7fa;
}
</style>
