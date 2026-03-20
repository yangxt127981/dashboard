import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  { path: '/', redirect: '/board' },
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'board', component: () => import('../views/Board.vue') },
      { path: 'inbox', component: () => import('../views/InboxBoard.vue') },
      { path: 'system/request-owner', component: () => import('../views/SystemRequestOwner.vue') },
      { path: 'system/dept', component: () => import('../views/SystemDept.vue') },
      { path: 'system/module', component: () => import('../views/SystemModule.vue') },
      { path: 'system/users', component: () => import('../views/SystemUsers.vue') },
      { path: 'system/roles', component: () => import('../views/SystemRoles.vue') },
      { path: 'system/login-log', component: () => import('../views/SystemLoginLog.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.token) {
    next('/login')
  } else if (to.path === '/login' && auth.token) {
    next('/board')
  } else {
    next()
  }
})

export default router
