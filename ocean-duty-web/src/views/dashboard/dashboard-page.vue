<template>
  <!-- start 监控首页 -->
  <div class="page-container dashboard-page">
    <!-- 异常告警区域 -->
    <div v-if="abnormalSites.length || abnormalModules.length" class="alert-section">
      <h3 class="section-title alert-title">
        <el-icon><WarningFilled /></el-icon>
        异常告警
      </h3>
      <el-row v-if="abnormalSites.length" :gutter="16">
        <el-col v-for="site in abnormalSites" :key="'site-' + site.id" :xs="24" :sm="12" :md="8" :lg="6">
          <MonitorSiteCard :site="site" />
        </el-col>
      </el-row>
      <el-row v-if="abnormalModules.length" :gutter="16" class="alert-modules">
        <el-col v-for="mod in abnormalModules" :key="'module-' + mod.id" :xs="24" :sm="12" :md="8" :lg="6">
          <MonitorModuleCard :module="mod" />
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

    <!-- 灾害预警 -->
    <div class="section">
      <h3 class="section-title">灾害预警 · 最后更新时间</h3>
      <el-row :gutter="16">
        <el-col
          v-for="mod in disasterModules"
          :key="mod.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <MonitorModuleCard :module="mod" />
        </el-col>
      </el-row>
    </div>

    <!-- 预报服务 -->
    <div class="section">
      <h3 class="section-title">预报服务 · 最后更新时间</h3>
      <el-row :gutter="16">
        <el-col
          v-for="mod in forecastModules"
          :key="mod.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <MonitorModuleCard :module="mod" />
        </el-col>
      </el-row>
    </div>
  </div>
  <!-- end 监控首页 -->
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboard, checkSites, checkModules } from '@/api/monitor'
import { MODULE_CATEGORY, MONITOR_STATUS } from '@/constants/monitor'
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

    const disasterModules = computed(() =>
      modules.value.filter(item => item.moduleCategory === MODULE_CATEGORY.DISASTER_WARNING.value)
    )

    const forecastModules = computed(() =>
      modules.value.filter(item => item.moduleCategory === MODULE_CATEGORY.FORECAST_SERVICE.value)
    )

    const abnormalModules = computed(() =>
      modules.value.filter(item => item.status === MONITOR_STATUS.ERROR.value)
    )

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
        await Promise.all([checkSites(), checkModules()])
        ElMessage.success('检测完成')
        await loadDashboard()
      } finally {
        checking.value = false
      }
    }

    onMounted(() => {
      loadDashboard()
    })

    return {
      abnormalSites,
      abnormalModules,
      sites,
      modules,
      disasterModules,
      forecastModules,
      checking,
      handleCheck
    }
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

.alert-modules {
  margin-top: 12px;
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
