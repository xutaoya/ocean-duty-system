<template>
  <!-- start 监控卡片 -->
  <el-card :class="['monitor-card', statusClass]" shadow="hover">
    <div class="card-header">
      <span class="site-name">{{ site.siteName }}</span>
      <el-tag :type="tagType" size="small">{{ statusText }}</el-tag>
    </div>
    <div class="card-body">
      <div class="info-row">
        <span class="label">响应时间</span>
        <span class="value">{{ site.responseTime != null ? site.responseTime + 'ms' : '-' }}</span>
      </div>
      <div class="info-row">
        <span class="label">HTTP状态</span>
        <span class="value">{{ site.httpStatus || '-' }}</span>
      </div>
      <div class="info-row">
        <span class="label">检测时间</span>
        <span class="value">{{ formatTime(site.lastCheckTime) }}</span>
      </div>
      <div v-if="site.errorMessage" class="error-msg">{{ site.errorMessage }}</div>
    </div>
  </el-card>
  <!-- end 监控卡片 -->
</template>

<script>
import { computed } from 'vue'
import { MONITOR_STATUS } from '@/constants/monitor'

export default {
  name: 'MonitorSiteCard',
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

    const tagType = computed(() => {
      if (props.site.status === MONITOR_STATUS.ERROR.value) return 'danger'
      if (props.site.status === MONITOR_STATUS.WARNING.value) return 'warning'
      return 'success'
    })

    const statusClass = computed(() => {
      if (props.site.status === MONITOR_STATUS.ERROR.value) return 'is-error'
      if (props.site.status === MONITOR_STATUS.WARNING.value) return 'is-warning'
      return 'is-normal'
    })

    /**
     * 格式化时间
     */
    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    return { statusText, tagType, statusClass, formatTime }
  }
}
</script>

<style scoped>
.monitor-card {
  margin-bottom: 16px;
}

.monitor-card.is-error {
  border-left: 4px solid #F56C6C;
}

.monitor-card.is-warning {
  border-left: 4px solid #E6A23C;
}

.monitor-card.is-normal {
  border-left: 4px solid #67C23A;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.site-name {
  font-size: 16px;
  font-weight: 600;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
}

.label {
  color: #909399;
}

.error-msg {
  margin-top: 8px;
  padding: 8px;
  background: #fef0f0;
  color: #F56C6C;
  border-radius: 4px;
  font-size: 13px;
}
</style>
