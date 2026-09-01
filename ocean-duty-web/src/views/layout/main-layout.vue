<template>
  <!-- start 主布局 -->
  <el-container class="main-layout">
    <el-aside :width="isMobile ? '0' : '220px'" class="main-aside">
      <div class="logo">海洋预报值班监控</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>监控首页</span>
        </el-menu-item>
        <el-menu-item index="/duty/duty-log-list">
          <el-icon><Document /></el-icon>
          <span>值班日志</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/monitor/monitor-site-list">
          <el-icon><Setting /></el-icon>
          <span>网站管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/monitor/monitor-module-list">
          <el-icon><Grid /></el-icon>
          <span>模块管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/monitor/monitor-datasource-list">
          <el-icon><Coin /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/user/user-list">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-body">
    <el-header class="main-header">
      <div class="header-left">
        <el-button
          v-if="isMobile"
          class="header-menu-btn"
          text
          @click="drawerVisible = true"
        >
          <el-icon :size="22"><Menu /></el-icon>
        </el-button>
        <span class="page-title">{{ currentTitle }}</span>
      </div>
        <div class="header-right">
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <RouterView />
      </el-main>
    </el-container>

    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="260px"
      :with-header="false"
      class="mobile-nav-drawer"
    >
      <div class="mobile-drawer-logo">海洋预报值班监控</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
        @select="drawerVisible = false"
      >
        <el-menu-item
          v-for="item in navItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
  <!-- end 主布局 -->
</template>

<script>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { isAdminRole } from '@/constants/user'

export default {
  name: 'MainLayout',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    const isMobile = ref(window.innerWidth <= 768)
    const drawerVisible = ref(false)

    const activeMenu = computed(() => route.path)
    const currentTitle = computed(() => route.meta.title || '')
    const isAdmin = computed(() => isAdminRole(userStore.role))

    const navItems = computed(() => {
      const items = [
        { path: '/dashboard', label: '监控首页', icon: 'Monitor' },
        { path: '/duty/duty-log-list', label: '值班日志', icon: 'Document' }
      ]
      if (isAdmin.value) {
        items.push(
          { path: '/monitor/monitor-site-list', label: '网站管理', icon: 'Setting' },
          { path: '/monitor/monitor-module-list', label: '模块管理', icon: 'Grid' },
          { path: '/monitor/monitor-datasource-list', label: '数据源管理', icon: 'Coin' },
          { path: '/user/user-list', label: '用户管理', icon: 'User' }
        )
      }
      return items
    })

    const handleResize = () => {
      isMobile.value = window.innerWidth <= 768
    }

    /**
     * 退出登录
     */
    const handleLogout = () => {
      userStore.logout()
      router.push('/login')
    }

    onMounted(() => {
      window.addEventListener('resize', handleResize)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
    })

    return {
      userStore,
      isMobile,
      isAdmin,
      drawerVisible,
      activeMenu,
      currentTitle,
      navItems,
      handleLogout
    }
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.main-aside {
  background-color: #001529;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff1a;
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.header-menu-btn {
  flex-shrink: 0;
  padding: 4px;
  margin-left: -8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #606266;
}

.main-content {
  padding: 0;
}

.main-body {
  flex: 1;
  min-width: 0;
}

.mobile-drawer-logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  background-color: #001529;
}

:deep(.mobile-nav-drawer .el-drawer__body) {
  padding: 0;
  background-color: #001529;
}

@media (max-width: 768px) {
  .main-aside {
    display: none;
  }

  .page-title {
    font-size: 16px;
  }

  .user-name {
    max-width: 96px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
  }
}
</style>
