<template>
  <!-- start 值班日志列表 -->
  <div class="duty-log-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">值班日志</h1>
        <p class="page-desc">记录值班期间网站与模块运行状态、异常处理过程</p>
      </div>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        填写日志
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-icon"><el-icon><Document /></el-icon></div>
        <div>
          <span class="stat-value">{{ total }}</span>
          <span class="stat-label">日志总数</span>
        </div>
      </div>
      <div class="stat-card stat-card--issue">
        <div class="stat-icon"><el-icon><WarningFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ issueCount }}</span>
          <span class="stat-label">含异常记录</span>
        </div>
      </div>
      <div class="stat-card stat-card--recover">
        <div class="stat-icon"><el-icon><CircleCheckFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ recoveredCount }}</span>
          <span class="stat-label">已恢复</span>
        </div>
      </div>
    </div>

    <div class="toolbar-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent="handleQuery">
        <el-form-item label="值班人员">
          <el-input
            v-model="queryForm.userName"
            placeholder="搜索值班人员"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="值班时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :default-time="defaultTime"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">日志列表</span>
        <span class="table-count">共 {{ total }} 条记录</span>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        class="log-table"
        :header-cell-style="tableHeaderStyle"
      >
        <el-table-column label="值班信息" width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar">{{ userInitial(row.userName) }}</div>
              <div>
                <div class="user-name">{{ row.userName || '-' }}</div>
                <div class="duty-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(row.dutyTime) }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <span class="type-badge">{{ actionTypeLabel(row) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="运行状态" min-width="220">
          <template #default="{ row }">
            <div class="status-cell">
              <div class="status-line">
                <el-icon><Monitor /></el-icon>
                <span class="status-label">网站</span>
                <span class="status-text">{{ row.siteStatus || '未填写' }}</span>
              </div>
              <div class="status-line">
                <el-icon><Grid /></el-icon>
                <span class="status-label">模块</span>
                <span class="status-text">{{ row.moduleStatus || '未填写' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="异常情况" width="100" align="center">
          <template #default="{ row }">
            <span :class="['issue-badge', hasProblem(row) ? 'issue-badge--yes' : 'issue-badge--no']">
              {{ issueLabel(row) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="异常闭环" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.closureSummary }">
              {{ row.closureSummary || (row.logSource === 'snapshot' ? '全部正常' : '-') }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="故障原因" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.problem }">{{ row.problem || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="处理措施" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.solution }">{{ row.solution || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="恢复时间" width="170">
          <template #default="{ row }">
            <span v-if="row.recoverTime" class="recover-time">
              <el-icon><CircleCheckFilled /></el-icon>
              {{ formatTime(row.recoverTime) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <el-icon :size="48"><Document /></el-icon>
            <p>暂无值班日志</p>
            <el-button type="primary" link @click="handleAdd">立即填写</el-button>
          </div>
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @current-change="handleQuery"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      class="log-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="log-form">
        <div class="form-section">
          <div class="form-section-title">基本信息</div>
          <el-form-item label="值班时间" prop="dutyTime">
            <el-date-picker
              v-model="form.dutyTime"
              type="datetime"
              placeholder="选择值班时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">运行状态</div>
          <el-form-item label="网站状态" prop="siteStatus">
            <el-input
              v-model="form.siteStatus"
              type="textarea"
              :rows="2"
              placeholder="网站运行状态摘要"
            />
          </el-form-item>
          <el-form-item label="模块状态" prop="moduleStatus">
            <el-input
              v-model="form.moduleStatus"
              type="textarea"
              :rows="2"
              placeholder="模块更新状态摘要"
            />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">异常处理</div>
          <el-form-item label="故障原因" prop="problem">
            <el-input
              v-model="form.problem"
              type="textarea"
              :rows="2"
              placeholder="如有异常请填写原因"
            />
          </el-form-item>
          <el-form-item label="处理措施" prop="solution">
            <el-input
              v-model="form.solution"
              type="textarea"
              :rows="2"
              placeholder="处理过程与结果"
            />
          </el-form-item>
          <el-form-item label="恢复时间" prop="recoverTime">
            <el-date-picker
              v-model="form.recoverTime"
              type="datetime"
              placeholder="选择恢复时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
              clearable
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <DutyLogDetailDialog v-model="detailVisible" :log-id="detailLogId" />
  </div>
  <!-- end 值班日志列表 -->
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDutyLog, deleteDutyLog, queryDutyLog, updateDutyLog } from '@/api/duty'
import DutyLogDetailDialog from '@/components/duty-log-detail-dialog.vue'

export default {
  name: 'DutyLogList',
  components: { DutyLogDetailDialog },
  setup() {
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const detailVisible = ref(false)
    const detailLogId = ref(null)
    const isEdit = ref(false)
    const formRef = ref(null)
    const tableData = ref([])
    const total = ref(0)
    const dateRange = ref([])

    const defaultTime = [
      new Date(2000, 0, 1, 0, 0, 0),
      new Date(2000, 0, 1, 23, 59, 59)
    ]
    const tableHeaderStyle = { background: '#fafbfc', color: '#595959', fontWeight: '600' }

    const queryForm = reactive({
      userName: '',
      pageNum: 1,
      pageSize: 10
    })

    const form = reactive({
      id: null,
      dutyTime: '',
      siteStatus: '',
      moduleStatus: '',
      problem: '',
      solution: '',
      recoverTime: ''
    })

    const dialogTitle = computed(() => (isEdit.value ? '编辑值班日志' : '填写值班日志'))

    const issueCount = computed(() => tableData.value.filter(row => hasProblem(row)).length)
    const recoveredCount = computed(() => tableData.value.filter(row =>
      (row.recoveredCount != null && row.recoveredCount > 0) || !!row.recoverTime
    ).length)

    const rules = {
      dutyTime: [{ required: true, message: '请选择值班时间', trigger: 'change' }]
    }

    const resolveAbnormalCount = (row) => {
      if (row.abnormalCount != null && row.abnormalCount > 0) {
        return row.abnormalCount
      }
      const countFromStatus = (status, prefix) => {
        if (!status || status.includes('全部正常') || !status.startsWith(prefix)) {
          return 0
        }
        const body = status.slice(prefix.length).trim()
        if (!body) {
          return 0
        }
        return body.split('、').filter(Boolean).length
      }
      return countFromStatus(row.siteStatus, '异常站点:')
        + countFromStatus(row.moduleStatus, '异常模块:')
    }

    const hasProblem = (row) => {
      if (resolveAbnormalCount(row) > 0) {
        return true
      }
      return !!(row.problem && row.problem.trim())
    }

    const issueLabel = (row) => {
      const count = resolveAbnormalCount(row)
      if (count > 0) {
        return `${count}项异常`
      }
      if (row.problem?.trim()) {
        return '有异常'
      }
      return '正常'
    }

    const actionTypeLabel = (row) => {
      const map = {
        record: '自动记录',
        update: '自动更新',
        manual: '手工填写'
      }
      return map[row.actionType] || (row.logSource === 'snapshot' ? '自动记录' : '手工填写')
    }

    const userInitial = (name) => (name || '?').charAt(0)

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const buildQueryParams = () => ({
      userName: queryForm.userName,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      startTime: dateRange.value?.[0] || undefined,
      endTime: dateRange.value?.[1] || undefined
    })

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await queryDutyLog(buildQueryParams())
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
    }

    const handleReset = () => {
      queryForm.userName = ''
      queryForm.pageNum = 1
      dateRange.value = []
      handleQuery()
    }

    const handleSizeChange = () => {
      queryForm.pageNum = 1
      handleQuery()
    }

    const resetForm = () => {
      form.id = null
      form.dutyTime = ''
      form.siteStatus = ''
      form.moduleStatus = ''
      form.problem = ''
      form.solution = ''
      form.recoverTime = ''
    }

    const handleAdd = () => {
      isEdit.value = false
      resetForm()
      dialogVisible.value = true
    }

    const handleView = (row) => {
      detailLogId.value = row.id
      detailVisible.value = true
    }

    const handleEdit = (row) => {
      isEdit.value = true
      form.id = row.id
      form.dutyTime = row.dutyTime
      form.siteStatus = row.siteStatus || ''
      form.moduleStatus = row.moduleStatus || ''
      form.problem = row.problem || ''
      form.solution = row.solution || ''
      form.recoverTime = row.recoverTime || ''
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      submitting.value = true
      try {
        if (isEdit.value) {
          await updateDutyLog({ ...form })
          ElMessage.success('更新成功')
        } else {
          await addDutyLog({ ...form })
          ElMessage.success('保存成功')
        }
        dialogVisible.value = false
        handleQuery()
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm(
        `确定删除「${row.userName}」${formatTime(row.dutyTime)} 的值班日志吗？`,
        '删除确认',
        { type: 'warning' }
      )
      await deleteDutyLog(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    }

    onMounted(() => {
      handleQuery()
    })

    return {
      loading,
      submitting,
      dialogVisible,
      detailVisible,
      detailLogId,
      formRef,
      tableData,
      total,
      queryForm,
      form,
      rules,
      dialogTitle,
      dateRange,
      defaultTime,
      tableHeaderStyle,
      issueCount,
      recoveredCount,
      hasProblem,
      issueLabel,
      actionTypeLabel,
      userInitial,
      formatTime,
      handleQuery,
      handleReset,
      handleSizeChange,
      resetForm,
      handleAdd,
      handleView,
      handleEdit,
      handleSubmit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.duty-log-page {
  min-height: 100%;
  padding: 28px 32px;
  background: linear-gradient(180deg, #f0f5ff 0%, #f4f7fb 240px, #f4f7fb 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
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

.add-btn {
  border-radius: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.04);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  font-size: 20px;
}

.stat-card--total .stat-icon {
  background: linear-gradient(135deg, #e6f4ff, #bae0ff);
  color: #1890ff;
}

.stat-card--issue .stat-icon {
  background: linear-gradient(135deg, #fffbe6, #ffe58f);
  color: #faad14;
}

.stat-card--recover .stat-icon {
  background: linear-gradient(135deg, #f6ffed, #d9f7be);
  color: #52c41a;
}

.stat-value {
  display: block;
  font-size: 24px;
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

.toolbar-card,
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.04);
}

.toolbar-card {
  padding: 20px 20px 4px;
  margin-bottom: 16px;
}

.table-card {
  padding: 0 0 16px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.table-count {
  font-size: 13px;
  color: #bfbfbf;
}

.log-table {
  --el-table-border-color: #f0f0f0;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e6f4ff, #f0f5ff);
  color: #1890ff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.duty-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

.status-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-line {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  line-height: 1.5;
}

.status-line .el-icon {
  color: #bfbfbf;
  margin-top: 3px;
  flex-shrink: 0;
}

.status-label {
  color: #8c8c8c;
  flex-shrink: 0;
}

.status-text {
  color: #595959;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #595959;
  background: #f5f5f5;
}

.issue-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.issue-badge--no {
  background: #f6ffed;
  color: #52c41a;
}

.issue-badge--yes {
  background: #fff7e6;
  color: #faad14;
}

.recover-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #52c41a;
}

.text-muted {
  color: #bfbfbf;
}

.pagination {
  margin-top: 16px;
  padding: 0 20px;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
  color: #bfbfbf;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.form-section {
  margin-bottom: 8px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #1890ff;
}

@media (max-width: 960px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .duty-log-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .toolbar-card {
    padding: 16px 16px 0;
  }
}
</style>
