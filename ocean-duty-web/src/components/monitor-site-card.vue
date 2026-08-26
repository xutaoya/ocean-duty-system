<template>
  <!-- start 监控卡片 -->
  <component
    :is="linkTag"
    :href="site.siteUrl || undefined"
    :target="site.siteUrl ? '_blank' : undefined"
    :rel="site.siteUrl ? 'noopener noreferrer' : undefined"
    :title="site.siteUrl ? `打开 ${site.siteName}` : undefined"
    :class="['monitor-card', statusClass, { 'is-link': !!site.siteUrl }]"
  >
    <div class="card-top">
      <SiteFaviconAvatar :site-url="site.siteUrl" :name="site.siteName" />
      <div class="site-info">
        <div class="site-name">{{ site.siteName }}</div>
        <div class="site-status">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
    </div>

    <div class="metrics">
      <div class="metric-item">
        <span class="metric-value">{{ site.responseTime != null ? site.responseTime : '-' }}</span>
        <span class="metric-label">响应 ms</span>
      </div>
      <div class="metric-divider" />
      <div class="metric-item">
        <span :class="['metric-value', httpStatusClass]">{{ site.httpStatus || '-' }}</span>
        <span class="metric-label">HTTP</span>
      </div>
      <div class="metric-divider" />
      <div class="metric-item">
        <span class="metric-value metric-value--sm">{{ formatTime(site.lastCheckTime) }}</span>
        <span class="metric-label">检测时间</span>
      </div>
    </div>

    <div v-if="showErrorMessage" class="error-msg">
      <el-icon><WarningFilled /></el-icon>
      <span>{{ site.errorMessage }}</span>
    </div>
  </component>
  <!-- end 监控卡片 -->
</template>

<script>
import { computed } from 'vue'
import { MONITOR_STATUS, getHttpStatusClass } from '@/constants/monitor'
import SiteFaviconAvatar from '@/components/site-favicon-avatar.vue'

export default {
  name: 'MonitorSiteCard',
  components: { SiteFaviconAvatar },
  props: {
    // 网站监控数据
    site: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const statusText = computed(() => {
      const item = Object.values(MONITOR_STATUS).find(s => s.value === props.site.status)
      return item ? item.desc : '未知'
    })

    const statusClass = computed(() => {
      if (props.site.status === MONITOR_STATUS.ERROR.value) return 'is-error'
      if (props.site.status === MONITOR_STATUS.WARNING.value) return 'is-warning'
      return 'is-normal'
    })

    const httpStatusClass = computed(() => getHttpStatusClass(props.site.httpStatus))

    const showErrorMessage = computed(() => {
      return props.site.errorMessage
          && props.site.status !== MONITOR_STATUS.NORMAL.value
    })

    const linkTag = computed(() => (props.site.siteUrl ? 'a' : 'div'))

    /**
     * 格式化时间
     */
    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(11, 19)
    }

    return {
      statusText,
      statusClass,
      httpStatusClass,
      showErrorMessage,
      linkTag,
      formatTime
    }
  }
}
</script>

<style scoped>
.monitor-card {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 188px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.04);
  transition: box-shadow 0.25s, transform 0.25s;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
}

.monitor-card.is-link {
  cursor: pointer;
}

.monitor-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.monitor-card.is-normal::before { background: linear-gradient(90deg, #52c41a, #95de64); }
.monitor-card.is-warning::before { background: linear-gradient(90deg, #faad14, #ffc53d); }
.monitor-card.is-error::before { background: linear-gradient(90deg, #ff4d4f, #ff7875); }

.monitor-card:hover {
  box-shadow: 0 8px 24px rgba(0, 21, 41, 0.08);
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  height: 72px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.site-info {
  flex: 1;
  min-width: 0;
}

.site-name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 21px;
}

.is-link:hover .site-name {
  color: #1890ff;
}

.site-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
}

.is-error .status-dot { background: #ff4d4f; }
.is-warning .status-dot { background: #faad14; }

.metrics {
  display: flex;
  align-items: center;
  height: 58px;
  margin-top: auto;
  flex-shrink: 0;
  border-top: 1px solid #f5f5f5;
}

.metric-item {
  flex: 1;
  text-align: center;
}

.metric-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  line-height: 1.3;
}

.metric-value.http-success { color: #52c41a; }
.metric-value.http-redirect { color: #1890ff; }
.metric-value.http-client-error { color: #faad14; }
.metric-value.http-server-error { color: #ff4d4f; }
.metric-value.http-unknown { color: #bfbfbf; }

.metric-value--sm {
  font-size: 13px;
  font-weight: 500;
}

.metric-label {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #bfbfbf;
  letter-spacing: 0.5px;
}

.metric-divider {
  width: 1px;
  height: 28px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.error-msg {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  height: 48px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff1f0;
  color: #cf1322;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.5;
  flex-shrink: 0;
  overflow: hidden;
}

.error-msg span {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.error-msg .el-icon {
  flex-shrink: 0;
  margin-top: 2px;
}
</style>
