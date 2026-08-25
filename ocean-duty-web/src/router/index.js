import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/login-page.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/main-layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/dashboard-page.vue'),
        meta: { title: '监控首页' }
      },
      {
        path: '/duty/duty-log-list',
        name: 'DutyLogList',
        component: () => import('@/views/duty/duty-log-list.vue'),
        meta: { title: '值班日志' }
      },
      {
        path: '/monitor/monitor-site-list',
        name: 'MonitorSiteList',
        component: () => import('@/views/monitor/monitor-site-list.vue'),
        meta: { title: '网站管理', requireAdmin: true }
      },
      {
        path: '/monitor/monitor-module-list',
        name: 'MonitorModuleList',
        component: () => import('@/views/monitor/monitor-module-list.vue'),
        meta: { title: '模块管理', requireAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 海洋预报值班监控` : '海洋预报值班监控'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  if (to.meta.requireAdmin && localStorage.getItem('role') !== 'admin') {
    next('/dashboard')
    return
  }
  next()
})

export default router
