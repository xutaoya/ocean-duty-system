<template>
  <!-- start 灾害预警面板 -->
  <div class="disaster-panel">
    <div
      v-for="mod in modules"
      :key="mod.id"
      class="disaster-row"
      :class="{ 'disaster-row--static': !isAlarmModule(mod) }"
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
            <span v-if="displayTitle(mod)" class="alarm-title">{{ displayTitleText(mod) }}</span>
            <span v-else :class="['alarm-empty', { 'alarm-empty--hint': mod.remark && !isContentCurrent(mod) }]">{{ emptyLabel(mod) }}</span>
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
          <span v-if="shouldShowCode(mod)" class="meta-item">
            <span class="meta-label">{{ codeLabel(mod) }}</span>
            {{ mod.alarmCode }}
          </span>
          <span class="meta-item">
            <span class="meta-label">{{ isGridModule(mod) ? '最近更新' : '最近发布' }}</span>
            {{ formatTime(mod.updateTime) }}
          </span>
          <span class="meta-item">
            <span class="meta-label">周期</span>
            {{ cycleLabel(mod) }}
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
        <el-icon v-if="isAlarmModule(mod)" class="row-arrow"><ArrowRight /></el-icon>
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
import { MODULE_CHECK_TYPE } from '@/constants/module'
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

    const parseCheckParam = (checkParam) => {
      if (!checkParam) return {}
      try {
        return JSON.parse(checkParam)
      } catch {
        return {}
      }
    }

    const isAlarmModule = (mod) => mod.checkType === MODULE_CHECK_TYPE.CMS_FORECAST_ALARM.value

    const isGridModule = (mod) => mod.checkType === MODULE_CHECK_TYPE.CMS_GRID_UPDATE.value

    const GRID_CYCLE_LABELS = {
      wind: '07-19时/19-次日7时 · 13h',
      wave: '08-22时/22-次日8时 · 15h/11h',
      current: '08:30-17:30/17:30-次日8:30 · 12h/16h',
      sst: '08:30-17:30/17:30-次日8:30 · 12h/16h',
      storm_tide: '24小时内'
    }

    const parseAlarmType = (checkParam) => parseCheckParam(checkParam).type || ''

    const isCmsTablePublish = (mod) => mod.checkType === MODULE_CHECK_TYPE.CMS_TABLE_PUBLISH.value

    const isPublishedToday = (time) => {
      if (!time) return false
      const publishDate = time.substring(0, 10)
      const today = new Date()
      const todayStr = [
        today.getFullYear(),
        String(today.getMonth() + 1).padStart(2, '0'),
        String(today.getDate()).padStart(2, '0')
      ].join('-')
      return publishDate === todayStr
    }

    const titleMatchesCurrentMonth = (title) => {
      if (!title) return false
      const match = title.match(/(\d{4})年0?(\d{1,2})月/)
      if (!match) return false
      const now = new Date()
      return Number(match[1]) === now.getFullYear() && Number(match[2]) === now.getMonth() + 1
    }

    const isPublishedCurrentPeriod = (mod) => {
      const scheduleType = parseCheckParam(mod.checkParam).scheduleType
      if (scheduleType === 'monthly') {
        if (titleMatchesCurrentMonth(mod.alarmTitle)) return true
        if (!mod.updateTime) return false
        const publishDate = new Date(mod.updateTime)
        const now = new Date()
        return publishDate.getFullYear() === now.getFullYear()
          && publishDate.getMonth() === now.getMonth()
      }
      return isPublishedToday(mod.updateTime)
    }

    const isContentCurrent = (mod) => (
      isCmsTablePublish(mod) ? isPublishedCurrentPeriod(mod) : isPublishedToday(mod.updateTime)
    )

    const effectiveStatus = (mod) => {
      if (isGridModule(mod)) {
        return mod.status
      }
      if (isContentCurrent(mod)) {
        return MONITOR_STATUS.NORMAL.value
      }
      return mod.status
    }

    const emptyLabel = (mod) => {
      if (isGridModule(mod)) {
        if (mod.remark) {
          return mod.remark
        }
        return mod.updateTime ? '' : '暂无最新更新数据'
      }
      if (mod.remark && !isContentCurrent(mod)) {
        return mod.remark
      }
      if (!isAlarmModule(mod)) {
        if (isContentCurrent(mod)) {
          return ''
        }
        if (mod.updateTime) {
          return parseCheckParam(mod.checkParam).scheduleType === 'monthly'
            ? '等待本月发布'
            : '等待今日发布'
        }
        return '暂无最新发布数据'
      }
      if (isPublishedToday(mod.updateTime)) {
        return ''
      }
      return '暂无最新警报数据'
    }

    const cycleLabel = (mod) => {
      const preset = parseCheckParam(mod.checkParam).windowPreset
      if (preset && GRID_CYCLE_LABELS[preset]) {
        return GRID_CYCLE_LABELS[preset]
      }
      const scheduleType = parseCheckParam(mod.checkParam).scheduleType
      const time = mod.expectedTime || '-'
      if (scheduleType === 'monthly') {
        return `每月首日 ${time}`
      }
      return `每天 ${time}`
    }

    const codeLabel = (mod) => (parseAlarmType(mod.checkParam) === 'bore' ? '海域' : '编号')

    const shouldShowCode = (mod) => {
      if (mod.moduleName === '近岸预报') return false
      return !!mod.alarmCode
    }

    const displayTitle = (mod) => {
      if (isGridModule(mod)) {
        return !!mod.updateTime
      }
      if (!mod.alarmTitle || isCmsTablePublish(mod)) return false
      return true
    }

    const displayTitleText = (mod) => {
      if (isGridModule(mod)) {
        return `更新时间 ${formatTime(mod.updateTime)}`
      }
      return mod.alarmTitle
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 16)
    }

    const rowAccent = (mod) => {
      if (mod.alarmLevel) {
        return getAlarmLevelTheme(mod.alarmLevel)
      }
      const theme = getMonitorStatusTheme(effectiveStatus(mod), MONITOR_STATUS)
      return { accent: theme.color, tagType: 'info' }
    }

    const statusTheme = (mod) => getMonitorStatusTheme(effectiveStatus(mod), MONITOR_STATUS)

    const handleRowClick = async (mod) => {
      if (!isAlarmModule(mod)) {
        return
      }
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
      isAlarmModule,
      isGridModule,
      isPublishedToday,
      isContentCurrent,
      effectiveStatus,
      emptyLabel,
      cycleLabel,
      codeLabel,
      shouldShowCode,
      displayTitle,
      displayTitleText,
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

.disaster-row--static {
  cursor: default;
}

.disaster-row--static:hover {
  background: #fff;
}

.disaster-row--static:hover .row-arrow {
  color: #d9d9d9;
  transform: none;
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

.alarm-empty--hint {
  color: #d48806;
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
