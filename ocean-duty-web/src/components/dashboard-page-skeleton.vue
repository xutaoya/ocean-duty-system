<template>
  <!-- start 监控首页骨架屏 -->
  <div class="dashboard-skeleton">
    <div class="stats-row">
      <div v-for="i in 4" :key="`stat-${i}`" class="stat-card-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="stat-skeleton-inner">
              <el-skeleton-item variant="circle" class="stat-icon-skeleton" />
              <div class="stat-text-skeleton">
                <el-skeleton-item variant="h1" class="stat-value-skeleton" />
                <el-skeleton-item variant="text" class="stat-label-skeleton" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <div class="section">
      <div class="section-header-skeleton">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="text" class="section-title-skeleton" />
          </template>
        </el-skeleton>
      </div>
      <el-row :gutter="16" class="card-grid">
        <el-col
          v-for="i in 5"
          :key="`site-${i}`"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="card-col"
        >
          <div class="site-card-skeleton">
            <el-skeleton animated>
              <template #template>
                <div class="site-card-top">
                  <el-skeleton-item variant="circle" class="avatar-skeleton" />
                  <div class="site-card-info">
                    <el-skeleton-item variant="text" class="site-name-skeleton" />
                    <el-skeleton-item variant="text" class="site-status-skeleton" />
                  </div>
                </div>
                <div class="site-metrics-skeleton">
                  <el-skeleton-item variant="text" class="metric-skeleton" />
                  <el-skeleton-item variant="text" class="metric-skeleton" />
                  <el-skeleton-item variant="text" class="metric-skeleton" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </el-col>
      </el-row>
    </div>

    <div v-for="section in listSections" :key="section.key" class="section">
      <div class="section-header-skeleton">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="text" class="section-title-skeleton" />
          </template>
        </el-skeleton>
      </div>
      <div class="panel-skeleton">
        <div v-for="i in section.rows" :key="`${section.key}-${i}`" class="panel-row-skeleton">
          <el-skeleton animated>
            <template #template>
              <div class="panel-row-inner">
                <el-skeleton-item variant="text" class="panel-type-skeleton" />
                <el-skeleton-item variant="text" class="panel-content-skeleton" />
                <el-skeleton-item variant="button" class="panel-status-skeleton" />
              </div>
              <div class="panel-meta-skeleton">
                <el-skeleton-item variant="text" class="meta-skeleton" />
                <el-skeleton-item variant="text" class="meta-skeleton" />
                <el-skeleton-item variant="text" class="meta-skeleton" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>
  </div>
  <!-- end 监控首页骨架屏 -->
</template>

<script>
export default {
  name: 'DashboardPageSkeleton',
  setup() {
    const listSections = [
      { key: 'disaster', rows: 4 },
      { key: 'env', rows: 3 },
      { key: 'grid', rows: 5 }
    ]

    return { listSections }
  }
}
</script>

<style scoped>
.dashboard-skeleton {
  animation: skeleton-fade-in 0.2s ease;
}

@keyframes skeleton-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card-skeleton {
  padding: 20px 22px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
}

.stat-skeleton-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-skeleton {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.stat-text-skeleton {
  flex: 1;
}

.stat-value-skeleton {
  width: 48px;
  height: 28px;
}

.stat-label-skeleton {
  width: 72px;
  margin-top: 8px;
}

.section {
  margin-bottom: 32px;
}

.section-header-skeleton {
  margin-bottom: 16px;
}

.section-title-skeleton {
  width: 180px;
  height: 20px;
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

.site-card-skeleton,
.module-card-skeleton {
  height: 100%;
  min-height: 188px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
}

.site-card-top,
.module-card-top {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}

.avatar-skeleton,
.module-icon-skeleton {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
}

.site-card-info,
.module-info-skeleton {
  flex: 1;
}

.site-name-skeleton,
.module-name-skeleton {
  width: 72%;
  height: 18px;
}

.site-status-skeleton,
.module-status-skeleton {
  width: 40%;
  height: 14px;
  margin-top: 8px;
}

.module-group-skeleton {
  width: 48%;
  height: 12px;
  margin-bottom: 6px;
}

.site-metrics-skeleton {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.metric-skeleton {
  flex: 1;
  height: 36px;
}

.module-meta-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.module-meta-line {
  width: 100%;
  height: 32px;
}

.panel-skeleton {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  overflow: hidden;
}

.panel-row-skeleton {
  padding: 18px 20px;
}

.panel-row-skeleton + .panel-row-skeleton {
  border-top: 1px solid #f0f0f0;
}

.panel-row-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 10px;
}

.panel-type-skeleton {
  width: 120px;
  height: 18px;
  flex-shrink: 0;
}

.panel-content-skeleton {
  flex: 1;
  height: 20px;
}

.panel-status-skeleton {
  width: 56px;
  height: 24px;
  border-radius: 999px;
  flex-shrink: 0;
}

.panel-meta-skeleton {
  display: flex;
  gap: 24px;
}

.meta-skeleton {
  width: 120px;
  height: 14px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card-skeleton {
    padding: 16px;
  }

  .panel-row-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .panel-meta-skeleton {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
