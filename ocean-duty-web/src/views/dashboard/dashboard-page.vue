<template>
  <!-- start 监控首页 -->
  <div class="page-container dashboard-page">
    <!-- 异常告警区域 -->
    <div v-if="abnormalSites.length" class="alert-section">
      <h3 class="section-title alert-title">
        <el-icon><WarningFilled /></el-icon>
        异常告警
      </h3>
      <el-row :gutter="16">
        <el-col v-for="site in abnormalSites" :key="site.id" :xs="24" :sm="12" :md="8" :lg="6">
          <MonitorSiteCard :site="site" />
        </el-col>
      </el-row>
    </div>

    <!-- 网站监控 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">网站监控</h3>
        <el-button type="primary" :loading="checking" @click="handleCheck">立即检测</el-button>
      </div>
      <el-row :gutter="16">
        <el-col v-for="site in sites" :key="site.id" :xs="24" :sm="12" :md="8" :lg="6">
          <MonitorSiteCard :site="site" />
        </el-col>
      </el-row>
    </div>

    <!-- 模块监控 -->
    <div class="section">
      <h3 class="section-title">模块监控</h3>
      <el-row :gutter="16">
        <el-col v-for="mod in modules" :key="mod.id" :xs="24" :sm="12" :md="8" :lg="6">
          <MonitorModuleCard :module="mod" />
        </el-col>
      </el-row>
    </div>
  </div>
  <!-- end 监控首页 -->
</template>

<script>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboard, checkSites } from '@/api/monitor'
import MonitorSiteCard from '@/components/monitor-site-card.vue'
import MonitorModuleCard from '@/components/monitor-module-card.vue'

export default {
  name: 'DashboardPage',
  components: { MonitorSiteCard, MonitorModuleCard },
  setup() {
    const abnormalSites = ref([])
    const sites = ref([])
    const modules = ref([])
    const checking = ref(false)

    /**
     * 加载仪表盘数据
     */
    const loadDashboard = async () => {
      const res = await getDashboard()
      abnormalSites.value = res.data.abnormalSites || []
      sites.value = res.data.sites || []
      modules.value = res.data.modules || []
    }

    /**
     * 手动触发检测
     */
    const handleCheck = async () => {
      checking.value = true
      try {
        await checkSites()
        ElMessage.success('检测完成')
        await loadDashboard()
      } finally {
        checking.value = false
      }
    }

    onMounted(() => {
      loadDashboard()
    })

    return { abnormalSites, sites, modules, checking, handleCheck }
  }
}
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.alert-title {
  color: #F56C6C;
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 12px;
  }

  .section-title {
    font-size: 16px;
  }
}
</style>
