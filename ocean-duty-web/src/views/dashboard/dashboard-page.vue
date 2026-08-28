<template>
  <!-- start 监控首页 -->
  <div class="dashboard-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">监控概览</h1>
        <p class="page-desc">实时掌握网站与模块运行状态</p>
      </div>
      <el-button type="primary" :loading="checking" class="check-btn" @click="handleCheck">
        <el-icon><Refresh /></el-icon>
        立即检测
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <DashboardPageSkeleton v-if="loading" />

    <template v-else>
    <div class="stats-row">
      <div v-for="item in statCards" :key="item.key" :class="['stat-card', `stat-card--${item.key}`]">
        <div class="stat-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 异常告警 -->
    <div v-if="abnormalSites.length" class="section alert-section">
      <div class="section-header">
        <div class="section-title-wrap">
          <span class="alert-pulse" />
          <h3 class="section-title">异常告警</h3>
          <el-tag type="danger" size="small" round>{{ abnormalSites.length }}</el-tag>
        </div>
      </div>
      <el-row :gutter="16" class="card-grid">
        <el-col v-for="site in abnormalSites" :key="site.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <MonitorSiteCard :site="site" />
        </el-col>
      </el-row>
    </div>

    <!-- 网站监控 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Monitor /></el-icon>
          网站监控
        </h3>
        <span class="section-count">共 {{ sites.length }} 个站点</span>
      </div>
      <el-row v-if="sites.length" :gutter="16" class="card-grid">
        <el-col v-for="site in sites" :key="site.id" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <MonitorSiteCard :site="site" />
        </el-col>
      </el-row>
      <div v-else class="empty-state">
        <el-icon :size="40"><Monitor /></el-icon>
        <p>暂无监控站点</p>
      </div>
    </div>

    <!-- 灾害预警模块 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><WarningFilled /></el-icon>
          灾害预警 · 最后更新时间
        </h3>
        <span class="section-count">中国海洋预报网 · {{ disasterModules.length }} 个模块</span>
      </div>
      <MonitorDisasterPanel v-if="disasterModules.length" :modules="disasterModules" />
      <div v-else class="empty-state empty-state--info">
        <el-icon :size="36"><InfoFilled /></el-icon>
        <p>暂无灾害预警模块数据</p>
      </div>
    </div>

    <!-- 环境预报模块 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Cloudy /></el-icon>
          环境预报 · 最后更新时间
        </h3>
        <span class="section-count">中国海洋预报网 · {{ envForecastModules.length }} 个模块</span>
      </div>
      <MonitorDisasterPanel v-if="envForecastModules.length" :modules="envForecastModules" />
      <div v-else class="empty-state empty-state--info">
        <el-icon :size="36"><InfoFilled /></el-icon>
        <p>暂无环境预报模块数据</p>
      </div>
    </div>

    <!-- 智能网格模块 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Grid /></el-icon>
          智能网格 · 最后更新时间
        </h3>
        <span class="section-count">中国海洋预报网 · {{ smartGridModules.length }} 个模块</span>
      </div>
      <MonitorDisasterPanel v-if="smartGridModules.length" :modules="smartGridModules" grid-detail-enabled />
      <div v-else class="empty-state empty-state--info">
        <el-icon :size="36"><InfoFilled /></el-icon>
        <p>暂无智能网格模块数据</p>
      </div>
    </div>

    </template>
  </div>
  <!-- end 监控首页 -->
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboard, checkDashboard } from '@/api/monitor'
import { MONITOR_STATUS, MODULE_CATEGORY } from '@/constants/monitor'
import MonitorSiteCard from '@/components/monitor-site-card.vue'
import MonitorDisasterPanel from '@/components/monitor-disaster-panel.vue'
import DashboardPageSkeleton from '@/components/dashboard-page-skeleton.vue'

export default {
  name: 'DashboardPage',
  components: { MonitorSiteCard, MonitorDisasterPanel, DashboardPageSkeleton },
  setup() {
    const abnormalSites = ref([])
    const sites = ref([])
    const modules = ref([])
    const loading = ref(true)
    const checking = ref(false)

    const stats = computed(() => {
      const total = sites.value.length
      const normal = sites.value.filter(s => s.status === MONITOR_STATUS.NORMAL.value).length
      const warning = sites.value.filter(s => s.status === MONITOR_STATUS.WARNING.value).length
      const error = sites.value.filter(s => s.status === MONITOR_STATUS.ERROR.value).length
      return { total, normal, warning, error }
    })

    const statCards = computed(() => [
      { key: 'total', label: '监控站点', value: stats.value.total, icon: 'Monitor' },
      { key: 'normal', label: '运行正常', value: stats.value.normal, icon: 'CircleCheckFilled' },
      { key: 'warning', label: '性能警告', value: stats.value.warning, icon: 'WarningFilled' },
      { key: 'error', label: '异常离线', value: stats.value.error, icon: 'CircleCloseFilled' }
    ])

    const disasterModules = computed(() =>
      modules.value.filter(m =>
        m.moduleCategory === MODULE_CATEGORY.DISASTER_WARNING.value
        && m.moduleGroup === '中国海洋预报网'
      )
    )

    const envForecastModules = computed(() =>
      modules.value.filter(m => m.moduleCategory === MODULE_CATEGORY.ENV_FORECAST.value)
    )

    const smartGridModules = computed(() =>
      modules.value.filter(m => m.moduleCategory === MODULE_CATEGORY.SMART_GRID.value)
    )

    /**
     * 加载仪表盘数据
     */
    const loadDashboard = async () => {
      try {
        const res = await getDashboard()
        abnormalSites.value = res.data.abnormalSites || []
        sites.value = res.data.sites || []
        modules.value = res.data.modules || []
      } finally {
        loading.value = false
      }
    }

    /**
     * 手动触发网站检测
     */
    const handleCheck = async () => {
      checking.value = true
      try {
        const res = await checkDashboard()
        abnormalSites.value = res.data.abnormalSites || []
        sites.value = res.data.sites || []
        modules.value = res.data.modules || []
        ElMessage.success('检测完成')
      } finally {
        checking.value = false
      }
    }

    onMounted(() => {
      loadDashboard()
    })

    return {
      abnormalSites,
      sites,
      modules,
      loading,
      checking,
      statCards,
      disasterModules,
      envForecastModules,
      smartGridModules,
      handleCheck
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  padding: 28px 32px;
  background: linear-gradient(180deg, #f0f5ff 0%, #f4f7fb 240px, #f4f7fb 100%);
}

/* ---- 页头 ---- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.check-btn {
  border-radius: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
}

/* ---- 统计卡片 ---- */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.04);
  transition: box-shadow 0.25s, transform 0.25s;
}

.stat-card:hover {
  box-shadow: 0 6px 20px rgba(0, 21, 41, 0.08);
  transform: translateY(-2px);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-card--total .stat-icon {
  background: linear-gradient(135deg, #e6f4ff, #bae0ff);
  color: #1890ff;
}

.stat-card--normal .stat-icon {
  background: linear-gradient(135deg, #f6ffed, #d9f7be);
  color: #52c41a;
}

.stat-card--warning .stat-icon {
  background: linear-gradient(135deg, #fffbe6, #ffe58f);
  color: #faad14;
}

.stat-card--error .stat-icon {
  background: linear-gradient(135deg, #fff1f0, #ffccc7);
  color: #ff4d4f;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  display: block;
  margin-top: 2px;
  font-size: 13px;
  color: #8c8c8c;
}

/* ---- 分区 ---- */
.section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.section-count {
  font-size: 13px;
  color: #bfbfbf;
}

:deep(.card-grid) {
  align-items: stretch;
}

:deep(.card-grid .el-col) {
  display: flex;
}

.card-col {
  margin-bottom: 16px;
}

.card-col > * {
  flex: 1;
  width: 100%;
}

/* ---- 告警区 ---- */
.alert-section {
  padding: 20px;
  background: linear-gradient(135deg, #fff1f0 0%, #fff7e6 100%);
  border-radius: 12px;
  border: 1px solid #ffccc7;
}

.alert-section .section-title {
  color: #cf1322;
}

.alert-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff4d4f;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.5); }
  50% { opacity: 0.7; box-shadow: 0 0 0 6px rgba(255, 77, 79, 0); }
}

/* ---- 空状态 ---- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px dashed #d9d9d9;
  color: #bfbfbf;
}

.empty-state--info {
  padding: 32px 20px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* ---- 响应式 ---- */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 22px;
  }

  .section-title {
    font-size: 15px;
  }
}
</style>
