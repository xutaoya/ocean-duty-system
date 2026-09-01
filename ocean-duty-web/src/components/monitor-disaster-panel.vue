<template>
  <!-- start 灾害预警面板 -->
  <div class="disaster-panel">
    <div
      v-for="mod in modules"
      :key="mod.id"
      class="disaster-row"
      :class="{
        'disaster-row--static': !isAlarmModule(mod) && !isChainDetailRow(mod),
        'disaster-row--grid': isChainDetailRow(mod),
        'disaster-row--surge': isTyphoonSurgeModule(mod) && isChainDetailRow(mod)
      }"
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
            <template v-if="displayTitle(mod) && (isGridModule(mod) || isTyphoonSurgeModule(mod))">
              <div class="grid-update-headline">
                <span class="grid-update-label">更新时间</span>
                <time class="grid-update-time">{{ formatTime(mod.updateTime) }}</time>
              </div>
            </template>
            <span v-else-if="displayTitle(mod)" class="alarm-title">{{ displayTitleText(mod) }}</span>
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

          <div
            v-if="isChainDetailRow(mod)"
            class="row-grid-detail"
            :class="{ 'row-grid-detail--surge': isTyphoonSurgeModule(mod) }"
            @click.stop="handleChainDetailClick(mod)"
          >
            <template v-if="isGridDetailLoading(mod.id)">
              <div class="grid-detail-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>加载中</span>
              </div>
            </template>
            <template v-else-if="getGridDetail(mod.id)">
              <div
                class="grid-detail-board"
                :class="{ 'grid-detail-board--surge': isTyphoonSurgeModule(mod) }"
              >
                <div
                  v-for="(group, index) in getChainDetailGroups(mod)"
                  :key="group.key"
                  class="grid-detail-group"
                >
                  <span class="grid-group-tag">
                    <span class="grid-group-index">{{ index + 1 }}</span>
                    {{ group.label }}
                  </span>
                  <div class="grid-metric-list">
                    <div
                      v-for="field in group.fields"
                      :key="field.key"
                      class="grid-metric"
                    >
                      <span class="grid-metric-label">{{ field.label }}</span>
                      <component
                        :is="field.type === 'time' ? 'time' : 'span'"
                        :class="[
                          'grid-metric-value',
                          { 'grid-metric-value--alert': isChainTimeAlert(mod, group, field) }
                        ]"
                      >{{ formatGridDetailValue(getGridDetail(mod.id)[field.key], field) }}</component>
                    </div>
                  </div>
                </div>
              </div>
              <p v-if="getGridDetail(mod.id).remark" class="grid-detail-remark">
                <el-icon><WarningFilled /></el-icon>
                {{ getGridDetail(mod.id).remark }}
              </p>
            </template>
            <div v-else class="grid-detail-empty">
              <el-icon><Clock /></el-icon>
              <span class="grid-detail-empty-text">{{ getGridDetailEmptyText(mod) }}</span>
            </div>
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
          v-if="shouldShowStatus(mod)"
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getModuleAlarmDetail, getSmartGridDetail, getTyphoonSurgeDetail } from '@/api/monitor'
import { MONITOR_STATUS } from '@/constants/monitor'
import { MODULE_CHECK_TYPE } from '@/constants/module'
import { getAlarmLevelTheme, getMonitorStatusTheme } from '@/lib/alarm-theme'
import { isDisasterWarningModule, shouldShowModuleStatus } from '@/lib/monitor-effective-status'
import MonitorAlarmDetailDialog from '@/components/monitor-alarm-detail-dialog.vue'

export default {
  name: 'MonitorDisasterPanel',
  components: { MonitorAlarmDetailDialog },
  props: {
    modules: {
      type: Array,
      default: () => []
    },
    gridDetailEnabled: {
      type: Boolean,
      default: false
    },
    typhoonSurgeDetailEnabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const detailVisible = ref(false)
    const detailLoading = ref(false)
    const alarmDetail = ref(null)
    const gridDetailMap = reactive({})
    const gridDetailLoadingMap = reactive({})

    const parseCheckParam = (checkParam) => {
      if (!checkParam) return {}
      try {
        return JSON.parse(checkParam)
      } catch {
        return {}
      }
    }

    const isAlarmModule = (mod) => mod.checkType === MODULE_CHECK_TYPE.CMS_FORECAST_ALARM.value

    const shouldShowStatus = shouldShowModuleStatus

    const isGridModule = (mod) => mod.checkType === MODULE_CHECK_TYPE.CMS_GRID_UPDATE.value

    const isTyphoonSurgeModule = (mod) => mod.checkType === MODULE_CHECK_TYPE.TYPHOON_STORM_SURGE_CHAIN.value

    const isChainDetailRow = (mod) =>
      (props.gridDetailEnabled && isGridModule(mod))
      || (props.typhoonSurgeDetailEnabled && isTyphoonSurgeModule(mod))

    const isGridDetailRow = isChainDetailRow

    const getGridDetail = (moduleId) => gridDetailMap[moduleId]

    const isGridDetailLoading = (moduleId) => !!gridDetailLoadingMap[moduleId]

    const GRID_CYCLE_LABELS = {
      wind: '07-19时/19-次日7时 · 13h',
      wave: '08-22时/22-次日8时 · 15h/11h',
      current: '08:30-17:30/17:30-次日8:30 · 12h/16h',
      sst: '08:30-17:30/17:30-次日8:30 · 12h/16h',
      storm_tide: '24小时内'
    }

    const gridDetailGroups = [
      {
        key: 'report',
        label: '数据库',
        fields: [{ key: 'reportStartTime', label: '起报时间', type: 'time' }]
      },
      {
        key: 'output',
        label: '处理后',
        fields: [
          { key: 'outputDataTime', label: '处理后时间', type: 'time' },
          { key: 'outputModifiedTime', label: '修改时间', type: 'time' },
          { key: 'outputFileSizeBytes', label: '文件大小', type: 'size' }
        ]
      },
      {
        key: 'element',
        label: '处理前',
        fields: [
          { key: 'elementDataTime', label: '处理前时间', type: 'time' },
          { key: 'elementModifiedTime', label: '修改时间', type: 'time' },
          { key: 'elementFileSizeBytes', label: '文件大小', type: 'size' }
        ]
      }
    ]

    const typhoonSurgeDetailGroups = [
      {
        key: 'database',
        label: '数据库',
        fields: [
          { key: 'initialTime', label: '起报时间', type: 'time' },
          { key: 'updateTime', label: '更新时间', type: 'time' }
        ]
      },
      {
        key: 'processed',
        label: '处理后',
        fields: [{ key: 'pgDoneStamp', label: '完成时间', type: 'time' }]
      },
      {
        key: 'beforeProcess',
        label: '处理前',
        fields: [{ key: 'ftpModifiedTime', label: '修改时间', type: 'time' }]
      },
      {
        key: 'raw',
        label: '原始文件',
        fields: [{ key: 'rawModifiedTime', label: '修改时间', type: 'time' }]
      }
    ]

    const typhoonSurgeChainKeys = [
      'initialTime',
      'updateTime',
      'pgDoneStamp',
      'ftpModifiedTime',
      'rawModifiedTime'
    ]

    const getChainDetailGroups = (mod) => {
      if (isTyphoonSurgeModule(mod)) {
        return typhoonSurgeDetailGroups
      }
      return getGridDetailGroups(mod)
    }

    const getGridDetailGroups = (mod) => {
      const detail = getGridDetail(mod.id)
      if (detail?.showOutput === false) {
        return gridDetailGroups.filter(group => group.key !== 'output')
      }
      const preset = parseCheckParam(mod.checkParam).windowPreset
      if (preset === 'storm_tide') {
        return gridDetailGroups.filter(group => group.key !== 'output')
      }
      return gridDetailGroups
    }

    const getGridDetailEmptyText = (mod) => {
      
      return '查看数据链路'
    }

    const parseAlarmType = (checkParam) => parseCheckParam(checkParam).type || ''

    const isCmsTablePublish = (mod) =>
      mod.checkType === MODULE_CHECK_TYPE.CMS_TABLE_PUBLISH.value
      || mod.checkType === MODULE_CHECK_TYPE.TYPHOON_STORM_SURGE_CHAIN.value

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
      if (isTyphoonSurgeModule(mod)) {
        return MONITOR_STATUS.NORMAL.value
      }
      if (isContentCurrent(mod)) {
        return MONITOR_STATUS.NORMAL.value
      }
      return mod.status
    }

    const emptyLabel = (mod) => {
      if (isGridModule(mod) || isTyphoonSurgeModule(mod)) {
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
      if (isGridModule(mod) || isTyphoonSurgeModule(mod)) {
        return !!mod.updateTime
      }
      if (!mod.alarmTitle || isCmsTablePublish(mod)) return false
      return true
    }

    const displayTitleText = (mod) => {
      if (isGridModule(mod) || isTyphoonSurgeModule(mod)) {
        return `更新时间 ${formatTime(mod.updateTime)}`
      }
      return mod.alarmTitle
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 16)
    }

    const formatFileSize = (bytes) => {
      if (bytes == null || bytes < 0) return '-'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let size = Number(bytes)
      let unitIndex = 0
      while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024
        unitIndex += 1
      }
      const digits = unitIndex === 0 ? 0 : size >= 100 ? 0 : size >= 10 ? 1 : 2
      return `${size.toFixed(digits)} ${units[unitIndex]}`
    }

    const formatGridDetailValue = (value, field) => {
      if (field.type === 'size') {
        return formatFileSize(value)
      }
      if (field.type === 'text') {
        return value || '-'
      }
      return formatTime(value)
    }

    const parseDetailTime = (time) => {
      if (!time) return null
      const normalized = time.replace('T', ' ').substring(0, 16)
      const timestamp = Date.parse(normalized.replace(' ', 'T'))
      return Number.isNaN(timestamp) ? null : timestamp
    }

    const getChainTimeAlertKeys = (detail, mod) => {
      const alerts = new Set()
      const chainKeys = isTyphoonSurgeModule(mod)
        ? typhoonSurgeChainKeys
        : getChainDetailGroups(mod)
            .map(group => group.fields[0]?.key)
            .filter(Boolean)
      for (let i = 1; i < chainKeys.length; i += 1) {
        const previous = parseDetailTime(detail[chainKeys[i - 1]])
        const current = parseDetailTime(detail[chainKeys[i]])
        if (previous != null && current != null && current > previous) {
          alerts.add(chainKeys[i])
        }
      }
      return alerts
    }

    const isChainTimeAlert = (mod, group, field) => {
      if (field.key !== group.fields[0]?.key) {
        return false
      }
      const detail = getGridDetail(mod.id)
      if (!detail) {
        return false
      }
      return getChainTimeAlertKeys(detail, mod).has(field.key)
    }

    const handleChainDetailClick = async (mod) => {
      if (isTyphoonSurgeModule(mod)) {
        if (gridDetailLoadingMap[mod.id]) {
          return
        }
        gridDetailLoadingMap[mod.id] = true
        try {
          const res = await getTyphoonSurgeDetail(mod.id)
          gridDetailMap[mod.id] = res.data
        } catch (error) {
          ElMessage.error(error.message || '加载数据链路失败')
        } finally {
          gridDetailLoadingMap[mod.id] = false
        }
        return
      }
      await handleGridDetailClick(mod)
    }

    const rowAccent = (mod) => {
      if (mod.alarmLevel) {
        return getAlarmLevelTheme(mod.alarmLevel)
      }
      if (isDisasterWarningModule(mod) || isTyphoonSurgeModule(mod)) {
        return { accent: '#1890ff', tagType: 'info' }
      }
      const theme = getMonitorStatusTheme(effectiveStatus(mod), MONITOR_STATUS)
      return { accent: theme.color, tagType: 'info' }
    }

    const statusTheme = (mod) => getMonitorStatusTheme(effectiveStatus(mod), MONITOR_STATUS)

    const getLinkedModuleIds = (mod, detail) => {
      if (detail?.linkedModuleIds?.length) {
        return detail.linkedModuleIds
      }
      const clickedPreset = parseCheckParam(mod.checkParam).windowPreset
      if (['current', 'sst'].includes(clickedPreset)) {
        return props.modules
          .filter(item => ['current', 'sst'].includes(parseCheckParam(item.checkParam).windowPreset))
          .map(item => item.id)
      }
      return [mod.id]
    }

    const handleGridDetailClick = async (mod) => {
      const linkedIds = getLinkedModuleIds(mod, null)
      if (linkedIds.some(id => gridDetailLoadingMap[id])) {
        return
      }
      linkedIds.forEach(id => {
        gridDetailLoadingMap[id] = true
      })
      try {
        const res = await getSmartGridDetail(mod.id)
        const targetIds = getLinkedModuleIds(mod, res.data)
        targetIds.forEach(id => {
          gridDetailMap[id] = res.data
        })
      } catch (error) {
        ElMessage.error(error.message || '加载智能网格详情失败')
      } finally {
        linkedIds.forEach(id => {
          gridDetailLoadingMap[id] = false
        })
      }
    }

    const handleRowClick = async (mod) => {
      if (isChainDetailRow(mod)) {
        await handleChainDetailClick(mod)
        return
      }
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
      isGridDetailRow,
      isChainDetailRow,
      isTyphoonSurgeModule,
      gridDetailGroups,
      typhoonSurgeDetailGroups,
      getChainDetailGroups,
      getGridDetailGroups,
      getGridDetailEmptyText,
      getGridDetail,
      isGridDetailLoading,
      handleChainDetailClick,
      handleGridDetailClick,
      isAlarmModule,
      isDisasterWarningModule,
      shouldShowStatus,
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
      formatGridDetailValue,
      isChainTimeAlert,
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

.disaster-row--grid {
  cursor: pointer;
}

.disaster-row--grid:hover {
  background: #fafbfc;
}

.disaster-row--surge {
  overflow-x: auto;
}

.disaster-row--surge .row-body {
  min-width: 0;
}

.disaster-row--grid .row-primary {
  align-items: center;
}

.disaster-row--surge .row-primary {
  align-items: stretch;
  gap: 20px;
}

.disaster-row--surge .row-type {
  width: 108px;
}

.disaster-row--surge .grid-update-headline {
  min-width: 118px;
}

.row-grid-detail {
  flex: 1.4;
  min-width: 0;
  margin-left: auto;
  padding: 10px 14px;
  border: 1px solid #e4eaf2;
  border-radius: 8px;
  background: linear-gradient(180deg, #fafbfd 0%, #f5f8fc 100%);
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.row-grid-detail--surge {
  flex: 1 1 480px;
  min-width: 480px;
  max-width: 100%;
  margin-left: auto;
  padding: 12px 14px;
  overflow-x: auto;
}

.row-grid-detail:hover {
  border-color: #c5d4e8;
  box-shadow: 0 2px 8px rgba(26, 54, 93, 0.06);
}

.grid-detail-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #64748b;
}

.grid-detail-board {
  display: flex;
  align-items: stretch;
  gap: 0;
}

.grid-detail-board--surge {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  column-gap: 0;
}

.grid-detail-board--surge .grid-detail-group {
  position: relative;
  flex: none;
  min-width: 0;
  padding: 0 12px 0 10px;
  gap: 8px;
}

.grid-detail-board--surge .grid-detail-group:first-child {
  padding-left: 0;
}

.grid-detail-board--surge .grid-detail-group:last-child {
  padding-right: 0;
}

.grid-detail-board--surge .grid-detail-group + .grid-detail-group {
  border-left: 1px solid #e8edf3;
}

.grid-detail-board--surge .grid-metric {
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  white-space: normal;
}

.grid-detail-board--surge .grid-metric-label {
  font-size: 10px;
  color: #94a3b8;
  letter-spacing: 0.02em;
}

.grid-detail-board--surge .grid-metric-value {
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
}

.grid-detail-board--surge .grid-group-tag {
  width: 100%;
  justify-content: flex-start;
  padding: 3px 8px;
  font-size: 10px;
}

.grid-detail-board--surge .grid-group-index {
  min-width: 14px;
  height: 14px;
  font-size: 9px;
}

.grid-detail-group {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 12px;
}

.grid-detail-group:first-child {
  padding-left: 0;
}

.grid-detail-group:last-child {
  padding-right: 0;
}

.grid-detail-group + .grid-detail-group {
  border-left: 1px solid #e8edf3;
}

.grid-group-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: #1e3a5f;
  background: rgba(30, 58, 95, 0.08);
}

.grid-group-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  border-radius: 50%;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  color: #fff;
  background: #1e3a5f;
}

.grid-metric-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.grid-metric {
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
}

.grid-metric-label {
  flex-shrink: 0;
  font-size: 11px;
  color: #94a3b8;
}

.grid-metric-value {
  font-size: 12px;
  font-weight: 500;
  color: #1e293b;
  font-variant-numeric: tabular-nums;
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
  line-height: 1.4;
}

.grid-metric-value--alert {
  color: #cf1322;
  font-weight: 700;
}

.grid-detail-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 4px 0;
  color: #64748b;
}

.grid-detail-empty .el-icon {
  font-size: 15px;
  color: #94a3b8;
}

.grid-detail-empty-text {
  font-size: 13px;
  color: #475569;
}

.grid-detail-empty-hint {
  font-size: 12px;
  color: #94a3b8;
}

.grid-detail-remark {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 12px 0 0;
  padding-top: 12px;
  border-top: 1px solid #e8edf3;
  font-size: 12px;
  color: #b45309;
}

.grid-detail-remark .el-icon {
  font-size: 14px;
  flex-shrink: 0;
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
  flex: 0 0 auto;
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

.grid-update-headline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
  min-width: 140px;
}

.grid-update-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.08em;
  color: #94a3b8;
  text-transform: uppercase;
}

.grid-update-time {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a5f;
  font-variant-numeric: tabular-nums;
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
  line-height: 1.3;
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
  .disaster-row--grid .row-primary,
  .disaster-row--surge .row-primary {
    flex-direction: column;
    align-items: stretch;
  }

  .row-grid-detail,
  .row-grid-detail--surge {
    margin-left: 0;
    margin-top: 10px;
    min-width: 0;
    flex: 1 1 auto;
  }

  .grid-detail-board,
  .grid-detail-board--surge {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .grid-detail-board--surge .grid-detail-group + .grid-detail-group {
    border-left: none;
    padding-top: 14px;
    border-top: 1px solid #e8edf3;
  }

  .grid-detail-board--surge .grid-metric {
    flex-direction: row;
    align-items: baseline;
  }

  .grid-detail-board--surge .grid-metric-value {
    white-space: normal;
  }

  .grid-detail-group {
    padding: 0;
  }

  .grid-detail-group + .grid-detail-group {
    border-left: none;
    padding-top: 14px;
    border-top: 1px solid #e8edf3;
  }

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
