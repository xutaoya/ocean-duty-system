<template>
  <!-- start 模块监控卡片 -->
  <el-card :class="['module-card', statusClass]" shadow="hover">
    <div class="module-name">{{ module.moduleName }}</div>
    <div class="module-info">
      <div class="info-item">
        <span class="label">更新时间</span>
        <span class="value">{{ formatTime(module.updateTime) }}</span>
      </div>
      <div class="info-item">
        <span class="label">更新周期</span>
        <span class="value">每天 {{ module.expectedTime || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">状态</span>
        <el-tag :type="tagType" size="small">{{ statusText }}</el-tag>
      </div>
    </div>
  </el-card>
  <!-- end 模块监控卡片 -->
</template>

<script>
import { computed } from 'vue'
import { MONITOR_STATUS } from '@/constants/monitor'

export default {
  name: 'MonitorModuleCard',
  props: {
    // 模块监控数据
    module: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const statusText = computed(() => {
      const item = Object.values(MONITOR_STATUS).find(s => s.value === props.module.status)
      return item ? item.desc : '未知'
    })

    const tagType = computed(() => {
      if (props.module.status === MONITOR_STATUS.ERROR.value) return 'danger'
      if (props.module.status === MONITOR_STATUS.WARNING.value) return 'warning'
      return 'success'
    })

    const statusClass = computed(() => {
      if (props.module.status === MONITOR_STATUS.ERROR.value) return 'is-error'
      if (props.module.status === MONITOR_STATUS.WARNING.value) return 'is-warning'
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
.module-card {
  margin-bottom: 16px;
}

.module-card.is-error {
  border-left: 4px solid #F56C6C;
}

.module-card.is-warning {
  border-left: 4px solid #E6A23C;
}

.module-card.is-normal {
  border-left: 4px solid #67C23A;
}

.module-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 14px;
}

.label {
  color: #909399;
}
</style>
