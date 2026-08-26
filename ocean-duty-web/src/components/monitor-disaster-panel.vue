<template>
  <!-- start 灾害预警面板 -->
  <div class="disaster-panel">
    <div
      v-for="mod in modules"
      :key="mod.id"
      class="disaster-row"
      @click="handleRowClick(mod)"
    >
      <div
        class="row-accent"
        :style="{ background: rowAccent(mod).accent }"
      />

      <div class="row-body">
        <div class="row-primary">
          <div class="row-type">
            <span class="type-name">{{ mod.moduleName }}</span>
            <span v-if="mod.moduleGroup" class="type-group">{{ mod.moduleGroup }}</span>
          </div>

          <div class="row-alarm">
            <span v-if="mod.alarmTitle" class="alarm-title">{{ mod.alarmTitle }}</span>
            <span v-else class="alarm-empty">暂无最新警报数据</span>
            <el-tag
              v-if="mod.alarmLevel"
              :type="rowAccent(mod).tagType"
              size="small"
              effect="plain"
              class="level-tag"
            >
              {{ mod.alarmLevel }}
            </el-tag>
          </div>
        </div>

        <div class="row-secondary">
          <span v-if="mod.alarmCode" class="meta-item">
            <span class="meta-label">{{ codeLabel(mod) }}</span>
            {{ mod.alarmCode }}
          </span>
          <span class="meta-item">
            <span class="meta-label">发布</span>
            {{ formatTime(mod.updateTime) }}
          </span>
          <span class="meta-item">
            <span class="meta-label">周期</span>
            每天 {{ mod.expectedTime || '-' }}
          </span>
        </div>
      </div>

      <div class="row-side">
        <span
          class="status-pill"
          :style="{ color: statusTheme(mod).color, background: statusTheme(mod).bg }"
        >
          {{ statusTheme(mod).label }}
        </span>
        <el-icon class="row-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <MonitorAlarmDetailDialog
      v-model="detailVisible"
      :loading="detailLoading"
      :alarm="alarmDetail"
    />
  </div>
  <!-- end 灾害预警面板 -->
</template>

<script>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getModuleAlarmDetail } from '@/api/monitor'
import { MONITOR_STATUS } from '@/constants/monitor'
import { getAlarmLevelTheme, getMonitorStatusTheme } from '@/lib/alarm-theme'
import MonitorAlarmDetailDialog from '@/components/monitor-alarm-detail-dialog.vue'

export default {
  name: 'MonitorDisasterPanel',
  components: { MonitorAlarmDetailDialog },
  props: {
    modules: {
      type: Array,
      default: () => []
    }
  },
  setup() {
    const detailVisible = ref(false)
    const detailLoading = ref(false)
    const alarmDetail = ref(null)

    const parseAlarmType = (checkParam) => {
      if (!checkParam) return ''
      try {
        return JSON.parse(checkParam).type || ''
      } catch {
        return ''
      }
    }

    const codeLabel = (mod) => (parseAlarmType(mod.checkParam) === 'bore' ? '海域' : '编号')

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 16)
    }

    const rowAccent = (mod) => getAlarmLevelTheme(mod.alarmLevel)

    const statusTheme = (mod) => getMonitorStatusTheme(mod.status, MONITOR_STATUS)

    const handleRowClick = async (mod) => {
      detailVisible.value = true
      detailLoading.value = true
      alarmDetail.value = null
      try {
        const res = await getModuleAlarmDetail(mod.id)
        alarmDetail.value = res.data
      } catch (error) {
        detailVisible.value = false
        ElMessage.error(error.message || '加载警报详情失败')
      } finally {
        detailLoading.value = false
      }
    }

    return {
      detailVisible,
      detailLoading,
      alarmDetail,
      codeLabel,
      formatTime,
      rowAccent,
      statusTheme,
      handleRowClick
    }
  }
}
</script>

<style scoped>
.disaster-panel {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  overflow: hidden;
}

.disaster-row {
  display: flex;
  align-items: stretch;
  gap: 0;
  cursor: pointer;
  transition: background 0.2s;
}

.disaster-row + .disaster-row {
  border-top: 1px solid #f0f0f0;
}

.disaster-row:hover {
  background: #fafbfc;
}

.row-accent {
  width: 4px;
  flex-shrink: 0;
}

.row-body {
  flex: 1;
  min-width: 0;
  padding: 18px 20px;
}

.row-primary {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 10px;
}

.row-type {
  width: 120px;
  flex-shrink: 0;
}

.type-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.type-group {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #bfbfbf;
}

.row-alarm {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.alarm-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  line-height: 1.5;
}

.alarm-empty {
  font-size: 14px;
  color: #bfbfbf;
}

.level-tag {
  flex-shrink: 0;
}

.row-secondary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  font-size: 13px;
  color: #595959;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-label {
  color: #bfbfbf;
}

.row-side {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 20px 18px 0;
  flex-shrink: 0;
}

.status-pill {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.row-arrow {
  color: #d9d9d9;
  font-size: 16px;
  transition: color 0.2s, transform 0.2s;
}

.disaster-row:hover .row-arrow {
  color: #1890ff;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .row-primary {
    flex-direction: column;
    gap: 8px;
  }

  .row-type {
    width: auto;
  }

  .row-side {
    flex-direction: column;
    align-items: flex-end;
    justify-content: center;
    padding-left: 12px;
  }

  .row-secondary {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
