import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { isAdminRole } from '@/constants/user'

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
      },
      {
        path: '/monitor/monitor-datasource-list',
        name: 'MonitorDatasourceList',
        component: () => import('@/views/monitor/monitor-datasource-list.vue'),
        meta: { title: '数据源管理', requireAdmin: true }
      },
      {
        path: '/user/user-list',
        name: 'UserList',
        component: () => import('@/views/user/user-list.vue'),
        meta: { title: '用户管理', requireAdmin: true }
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
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (userStore.token) {
      next('/dashboard')
      return
    }
    next()
    return
  }

  if (!userStore.token) {
    next('/login')
    return
  }

  if (to.meta.requireAdmin && !isAdminRole(userStore.role)) {
    next('/dashboard')
    return
  }
  next()
})

export default router
