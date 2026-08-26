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

    <el-container>
      <el-header class="main-header">
        <div class="header-left">
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

    const activeMenu = computed(() => route.path)
    const currentTitle = computed(() => route.meta.title || '')
    const isAdmin = computed(() => isAdminRole(userStore.role))

    /**
     * 监听窗口大小变化
     */
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
      activeMenu,
      currentTitle,
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

@media (max-width: 768px) {
  .main-aside {
    display: none;
  }

  .page-title {
    font-size: 16px;
  }
}
</style>
