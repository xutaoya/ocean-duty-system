<template>
  <el-dialog
    :model-value="modelValue"
    title="值班日志详情"
    width="760px"
    destroy-on-close
    class="duty-log-detail-dialog"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="detail-body">
      <template v-if="detail">
        <div class="detail-head">
          <div>
            <div class="detail-user">{{ detail.userName || '-' }}</div>
            <div class="detail-time">{{ formatTime(detail.dutyTime) }}</div>
          </div>
          <el-tag size="small" effect="plain">{{ actionTypeLabel(detail.actionType) }}</el-tag>
        </div>

        <div class="detail-section">
          <div class="section-title">异常闭环摘要</div>
          <p :class="['closure-summary', { 'closure-summary--normal': !hasAbnormal(detail) }]">
            {{ detail.closureSummary || '全部正常，无异常变更' }}
          </p>
        </div>

        <div class="detail-section">
          <div class="section-title">模块异常-恢复时间线</div>
          <div v-if="moduleTimeline.length" class="timeline-list">
            <div
              v-for="(item, index) in moduleTimeline"
              :key="`${item.source}-${item.targetKey}-${index}`"
              class="timeline-item"
            >
              <span :class="['timeline-dot', `timeline-dot--${item.changeType || item.eventRole}`]" />
              <div class="timeline-content">
                <div class="timeline-desc">{{ item.description }}</div>
                <div v-if="item.eventRoleLabel" class="timeline-meta">{{ item.eventRoleLabel }}</div>
              </div>
            </div>
          </div>
          <p v-else class="empty-hint">本条日志未记录模块异常变更</p>
        </div>

        <div class="detail-section">
          <div class="section-title">当日异常事件闭环</div>
          <el-table v-if="incidentList.length" :data="incidentList" size="small" border>
            <el-table-column label="监控项" min-width="120" prop="targetName" />
            <el-table-column label="类型" width="70">
              <template #default="{ row }">{{ row.targetType === 'site' ? '站点' : '模块' }}</template>
            </el-table-column>
            <el-table-column label="生命周期" min-width="260" prop="lifecycleText" />
            <el-table-column label="本条角色" width="90" prop="eventRoleLabel" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.incidentStatus === 'recovered' ? 'success' : 'warning'" size="small">
                  {{ row.incidentStatus === 'recovered' ? '已闭环' : '进行中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <p v-else class="empty-hint">暂无异常事件记录</p>
        </div>

        <div class="detail-section">
          <div class="section-title">本次变更明细</div>
          <div v-if="changeGroups.length" class="change-groups">
            <div v-for="group in changeGroups" :key="group.key" class="change-group">
              <span class="change-group-label">{{ group.label }}</span>
              <span class="change-group-value">{{ group.value }}</span>
            </div>
          </div>
          <p v-else class="empty-hint">无状态变更</p>
        </div>

        <div class="detail-section">
          <div class="section-title">运行状态</div>
          <p class="status-text">网站：{{ detail.siteStatus || '-' }}</p>
          <p class="status-text">模块：{{ detail.moduleStatus || '-' }}</p>
        </div>
      </template>
    </div>
  </el-dialog>
</template>

<script>
import { computed, ref, watch } from 'vue'
import { getDutyLogDetail } from '@/api/duty'

export default {
  name: 'DutyLogDetailDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    logId: {
      type: [Number, String],
      default: null
    }
  },
  emits: ['update:modelValue'],
  setup(props) {
    const loading = ref(false)
    const detail = ref(null)

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const actionTypeLabel = (actionType) => {
      const map = {
        record: '自动记录',
        update: '自动更新',
        manual: '手工填写'
      }
      return map[actionType] || '值班日志'
    }

    const moduleTimeline = computed(() => {
      if (!detail.value?.timeline) {
        return []
      }
      return detail.value.timeline.filter(item => item.targetType === 'module')
    })

    const incidentList = computed(() => {
      const incidents = detail.value?.incidents || []
      return incidents.filter(item => item.targetType === 'module')
    })

    const hasAbnormal = (row) => {
      if (!row) return false
      if (row.abnormalCount != null) {
        return row.abnormalCount > 0
      }
      return !!(row.moduleStatus && !row.moduleStatus.includes('全部正常'))
    }

    const changeGroups = computed(() => {
      const summary = detail.value?.changeSummary
      if (!summary) {
        return []
      }
      const groups = [
        { key: 'recovered', label: '恢复', items: summary.recovered },
        { key: 'newAbnormals', label: '新异常', items: summary.newAbnormals },
        { key: 'changed', label: '状态变化', items: summary.changed },
        { key: 'persistent', label: '持续异常', items: summary.persistent }
      ]
      return groups
        .filter(group => group.items?.length)
        .map(group => ({
          key: group.key,
          label: group.label,
          value: group.items.map(item => item.targetName).join('、')
        }))
    })

    const loadDetail = async () => {
      if (!props.logId) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        const res = await getDutyLogDetail(props.logId)
        detail.value = res.data
      } finally {
        loading.value = false
      }
    }

    watch(
      () => [props.modelValue, props.logId],
      ([visible, logId]) => {
        if (visible && logId) {
          loadDetail()
        }
      }
    )

    return {
      loading,
      detail,
      moduleTimeline,
      incidentList,
      changeGroups,
      hasAbnormal,
      formatTime,
      actionTypeLabel
    }
  }
}
</script>

<style scoped>
.detail-body {
  min-height: 120px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.detail-user {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-time {
  margin-top: 4px;
  font-size: 13px;
  color: #8c8c8c;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  padding-left: 8px;
  border-left: 3px solid #1890ff;
}

.closure-summary {
  margin: 0;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f6ffed;
  color: #389e0d;
  line-height: 1.6;
  font-size: 13px;
}

.closure-summary--normal {
  background: #fafafa;
  color: #8c8c8c;
}

.empty-hint {
  margin: 0;
  font-size: 13px;
  color: #bfbfbf;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-item {
  display: flex;
  gap: 10px;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #bfbfbf;
}

.timeline-dot--new,
.timeline-dot--start {
  background: #ff4d4f;
}

.timeline-dot--changed,
.timeline-dot--ongoing {
  background: #faad14;
}

.timeline-dot--recovered,
.timeline-dot--recover {
  background: #52c41a;
}

.timeline-dot--persistent {
  background: #1890ff;
}

.timeline-desc {
  font-size: 13px;
  color: #262626;
  line-height: 1.5;
}

.timeline-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #8c8c8c;
}

.change-groups {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.change-group {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.change-group-label {
  flex-shrink: 0;
  color: #8c8c8c;
}

.change-group-value {
  color: #595959;
}

.status-text {
  margin: 0 0 6px;
  font-size: 13px;
  color: #595959;
  line-height: 1.5;
}
</style>
