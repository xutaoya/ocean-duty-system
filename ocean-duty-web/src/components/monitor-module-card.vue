<template>
  <!-- start 模块监控卡片 -->
  <div :class="['module-card', statusClass]">
    <div class="card-top">
      <div class="module-icon">
        <el-icon><Grid /></el-icon>
      </div>
      <div class="module-info">
        <div v-if="module.moduleGroup" class="module-group">{{ module.moduleGroup }}</div>
        <div class="module-name">{{ module.moduleName }}</div>
        <div class="module-status">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
    </div>

    <div class="module-meta">
      <div class="meta-item">
        <el-icon><Clock /></el-icon>
        <div>
          <span class="meta-label">更新时间</span>
          <span class="meta-value">{{ formatTime(module.updateTime) }}</span>
        </div>
      </div>
      <div class="meta-item">
        <el-icon><Timer /></el-icon>
        <div>
          <span class="meta-label">更新周期</span>
          <span class="meta-value">每天 {{ module.expectedTime || '-' }}</span>
        </div>
      </div>
    </div>
  </div>
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

    return { statusText, statusClass, formatTime }
  }
}
</script>

<style scoped>
.module-card {
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
}

.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.module-card.is-normal::before { background: linear-gradient(90deg, #52c41a, #95de64); }
.module-card.is-warning::before { background: linear-gradient(90deg, #faad14, #ffc53d); }
.module-card.is-error::before { background: linear-gradient(90deg, #ff4d4f, #ff7875); }

.module-card:hover {
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

.module-info {
  flex: 1;
  min-width: 0;
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f6ffed 0%, #e6fffb 100%);
  color: #13c2c2;
  font-size: 18px;
  flex-shrink: 0;
}

.is-error .module-icon {
  background: linear-gradient(135deg, #fff1f0 0%, #fff2e8 100%);
  color: #ff4d4f;
}

.is-warning .module-icon {
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  color: #faad14;
}

.module-group {
  font-size: 11px;
  color: #8c8c8c;
  margin-bottom: 2px;
  letter-spacing: 0.5px;
}

.module-name {
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

.module-status {
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

.module-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  height: 76px;
  margin-top: auto;
  flex-shrink: 0;
  border-top: 1px solid #f5f5f5;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.meta-item .el-icon {
  color: #bfbfbf;
  font-size: 15px;
}

.meta-label {
  display: block;
  font-size: 11px;
  color: #bfbfbf;
}

.meta-value {
  display: block;
  color: #595959;
  margin-top: 2px;
}
</style>
