<template>
  <!-- start 监控模块管理 -->
  <div class="monitor-module-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">模块管理</h1>
        <p class="page-desc">配置预报模块检测方式、更新周期与数据源关联</p>
      </div>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增模块
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-icon"><el-icon><Grid /></el-icon></div>
        <div>
          <span class="stat-value">{{ total }}</span>
          <span class="stat-label">模块总数</span>
        </div>
      </div>
      <div class="stat-card stat-card--disaster">
        <div class="stat-icon"><el-icon><WarningFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ disasterCount }}</span>
          <span class="stat-label">灾害预警</span>
        </div>
      </div>
      <div class="stat-card stat-card--forecast">
        <div class="stat-icon"><el-icon><Sunny /></el-icon></div>
        <div>
          <span class="stat-value">{{ forecastCount }}</span>
          <span class="stat-label">预报服务</span>
        </div>
      </div>
      <div class="stat-card stat-card--error">
        <div class="stat-icon"><el-icon><CircleCloseFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ errorCount }}</span>
          <span class="stat-label">异常模块</span>
        </div>
      </div>
    </div>

    <div class="toolbar-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent="handleQuery">
        <el-form-item label="模块名称">
          <el-input
            v-model="queryForm.moduleName"
            placeholder="搜索模块名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="模块分类">
          <el-select v-model="queryForm.moduleCategory" placeholder="全部分类" clearable style="width: 140px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模块分组">
          <el-input
            v-model="queryForm.moduleGroup"
            placeholder="搜索分组"
            clearable
            style="width: 140px"
            @keyup.enter="handleQuery"
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
        <span class="table-title">模块列表</span>
        <span class="table-count">共 {{ total }} 个模块</span>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        class="module-table"
        :header-cell-style="tableHeaderStyle"
      >
        <el-table-column label="模块" min-width="200">
          <template #default="{ row }">
            <div class="module-cell">
              <div :class="['module-icon', `module-icon--${categoryKey(row.moduleCategory)}`]">
                <el-icon><component :is="categoryIcon(row.moduleCategory)" /></el-icon>
              </div>
              <div class="module-cell-info">
                <span class="module-cell-name">{{ row.moduleName }}</span>
                <div class="module-cell-tags">
                  <el-tag size="small" effect="plain" round>{{ row.moduleCategoryName }}</el-tag>
                  <el-tag v-if="row.moduleGroup" size="small" type="info" effect="plain" round>
                    {{ row.moduleGroup }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="检测方式" width="150">
          <template #default="{ row }">
            <span class="check-type">{{ formatCheckType(row.checkType) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="最新数据" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.alarmTitle" class="alarm-preview">{{ row.alarmTitle }}</span>
            <span v-else class="text-muted">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="更新周期" width="110" align="center">
          <template #default="{ row }">
            <span class="cycle-text">每天 {{ row.expectedTime || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span
              v-if="shouldShowModuleStatus(row)"
              :class="['status-badge', `status-badge--${statusKey(row.status)}`]"
            >
              <span class="status-dot" />
              {{ formatStatus(row.status) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="最后更新" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ formatTime(row.updateTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
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
            <el-icon :size="48"><Grid /></el-icon>
            <p>暂无模块数据</p>
            <el-button type="primary" link @click="handleAdd">立即新增</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close class="module-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="module-form">
        <div class="form-section">
          <div class="form-section-title">基本信息</div>
          <el-form-item label="模块分类" prop="moduleCategory">
            <el-select v-model="form.moduleCategory" placeholder="请选择" style="width: 100%">
              <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.desc"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模块分组" prop="moduleGroup">
            <el-input v-model="form.moduleGroup" placeholder="如 海浪警报、海浪" />
          </el-form-item>
          <el-form-item label="模块名称" prop="moduleName">
            <el-input v-model="form.moduleName" placeholder="请输入模块名称" />
          </el-form-item>
          <el-form-item label="页面地址" prop="moduleUrl">
            <el-input v-model="form.moduleUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="关联网站" prop="siteId">
            <el-select v-model="form.siteId" placeholder="请选择" filterable style="width: 100%">
              <el-option
                v-for="site in siteOptions"
                :key="site.id"
                :label="site.siteName"
                :value="site.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="预期更新时间" prop="expectedTime">
            <el-time-select
              v-model="form.expectedTime"
              start="00:00"
              step="00:30"
              end="23:30"
              placeholder="选择时间"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">检测配置</div>
          <el-form-item label="检测方式" prop="checkType">
            <el-select v-model="form.checkType" placeholder="请选择" style="width: 100%" @change="handleCheckTypeChange">
              <el-option
                v-for="item in checkTypeOptions"
                :key="item.value"
                :label="item.desc"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-for="field in currentCheckFields"
            :key="field.key"
            :label="field.label"
          >
            <el-select
              v-if="field.options"
              v-model="paramForm[field.key]"
              :placeholder="field.placeholder"
              style="width: 100%"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <el-select
              v-else-if="field.inputType === 'datasource'"
              v-model="paramForm[field.key]"
              :placeholder="field.placeholder"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="ds in datasourceOptions"
                :key="ds.id"
                :label="formatDatasourceLabel(ds)"
                :value="String(ds.id)"
              />
            </el-select>
            <el-input
              v-else
              v-model="paramForm[field.key]"
              :placeholder="field.placeholder"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
  <!-- end 监控模块管理 -->
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryModule, addModule, updateModule, deleteModule } from '@/api/monitor-module'
import { listSites } from '@/api/monitor'
import { listDatasource } from '@/api/monitor-datasource'
import { MODULE_CATEGORY, MONITOR_STATUS } from '@/constants/monitor'
import { shouldShowModuleStatus } from '@/lib/monitor-effective-status'
import { MODULE_CHECK_TYPE, MODULE_CATEGORY_OPTIONS } from '@/constants/module'

export default {
  name: 'MonitorModuleList',
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const siteOptions = ref([])
    const datasourceOptions = ref([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增模块')
    const submitting = ref(false)
    const formRef = ref(null)
    const paramForm = reactive({})

    const categoryOptions = MODULE_CATEGORY_OPTIONS
    const checkTypeOptions = Object.values(MODULE_CHECK_TYPE)
    const tableHeaderStyle = { background: '#fafbfc', color: '#595959', fontWeight: '600' }

    const queryForm = reactive({
      moduleName: '',
      moduleCategory: '',
      moduleGroup: '',
      pageNum: 1,
      pageSize: 10
    })

    const form = reactive({
      id: null,
      siteId: null,
      moduleName: '',
      moduleUrl: '',
      moduleCategory: MODULE_CATEGORY.DISASTER_WARNING.value,
      moduleGroup: '',
      checkType: MODULE_CHECK_TYPE.WARN_HISTORY.value,
      checkParam: '',
      expectedTime: '08:00'
    })

    const rules = {
      moduleCategory: [{ required: true, message: '请选择模块分类', trigger: 'change' }],
      moduleName: [{ required: true, message: '请输入模块名称', trigger: 'blur' }],
      moduleUrl: [{ required: true, message: '请输入页面地址', trigger: 'blur' }],
      siteId: [{ required: true, message: '请选择关联网站', trigger: 'change' }],
      expectedTime: [{ required: true, message: '请选择预期更新时间', trigger: 'change' }],
      checkType: [{ required: true, message: '请选择检测方式', trigger: 'change' }]
    }

    const disasterCount = computed(() =>
      tableData.value.filter(row => row.moduleCategory === MODULE_CATEGORY.DISASTER_WARNING.value).length
    )
    const forecastCount = computed(() =>
      tableData.value.filter(row => row.moduleCategory === MODULE_CATEGORY.FORECAST_SERVICE.value).length
    )
    const errorCount = computed(() =>
      tableData.value.filter(row =>
        shouldShowModuleStatus(row) && row.status === MONITOR_STATUS.ERROR.value
      ).length
    )

    const currentCheckFields = computed(() => {
      const item = checkTypeOptions.find(option => option.value === form.checkType)
      return item ? item.fields : []
    })

    const formatDatasourceLabel = (ds) => {
      if (!ds.tableName) return ds.dsName
      return `${ds.dsName}（${ds.tableName}）`
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const formatStatus = (status) => {
      const item = Object.values(MONITOR_STATUS).find(option => option.value === status)
      return item ? item.desc : '未知'
    }

    const statusKey = (status) => {
      if (status === MONITOR_STATUS.ERROR.value) return 'error'
      if (status === MONITOR_STATUS.WARNING.value) return 'warning'
      return 'normal'
    }

    const formatCheckType = (checkType) => {
      const item = checkTypeOptions.find(option => option.value === checkType)
      return item ? item.desc : checkType || '-'
    }

    const categoryKey = (category) =>
      category === MODULE_CATEGORY.FORECAST_SERVICE.value ? 'forecast' : 'disaster'

    const categoryIcon = (category) =>
      category === MODULE_CATEGORY.FORECAST_SERVICE.value ? 'Sunny' : 'WarningFilled'

    const resetParamForm = (checkType) => {
      Object.keys(paramForm).forEach(key => delete paramForm[key])
      const item = checkTypeOptions.find(option => option.value === checkType)
      if (!item) return
      item.fields.forEach(field => {
        paramForm[field.key] = ''
      })
    }

    const parseCheckParam = (checkParam) => {
      resetParamForm(form.checkType)
      if (!checkParam) return
      try {
        const data = JSON.parse(checkParam)
        Object.keys(data).forEach(key => {
          paramForm[key] = data[key]
        })
      } catch {
        ElMessage.warning('检测参数解析失败')
      }
    }

    const buildCheckParam = () => {
      const data = {}
      currentCheckFields.value.forEach(field => {
        const value = paramForm[field.key]
        if (value) data[field.key] = value
      })
      return JSON.stringify(data)
    }

    const handleCheckTypeChange = (value) => {
      resetParamForm(value)
    }

    const resetForm = () => {
      form.id = null
      form.siteId = siteOptions.value[0]?.id || null
      form.moduleName = ''
      form.moduleUrl = ''
      form.moduleCategory = MODULE_CATEGORY.DISASTER_WARNING.value
      form.moduleGroup = ''
      form.checkType = MODULE_CHECK_TYPE.WARN_HISTORY.value
      form.expectedTime = '08:00'
      resetParamForm(form.checkType)
    }

    const loadSiteOptions = async () => {
      const res = await listSites()
      siteOptions.value = res.data || []
    }

    const loadDatasourceOptions = async () => {
      const res = await listDatasource()
      datasourceOptions.value = res.data || []
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await queryModule(queryForm)
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
    }

    const handleReset = () => {
      queryForm.moduleName = ''
      queryForm.moduleCategory = ''
      queryForm.moduleGroup = ''
      queryForm.pageNum = 1
      handleQuery()
    }

    const handleSizeChange = () => {
      queryForm.pageNum = 1
      handleQuery()
    }

    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增模块'
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      form.id = row.id
      form.siteId = row.siteId
      form.moduleName = row.moduleName
      form.moduleUrl = row.moduleUrl
      form.moduleCategory = row.moduleCategory
      form.moduleGroup = row.moduleGroup
      form.checkType = row.checkType
      form.expectedTime = row.expectedTime
      parseCheckParam(row.checkParam)
      dialogTitle.value = '编辑模块'
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      submitting.value = true
      try {
        const payload = { ...form, checkParam: buildCheckParam() }
        if (form.id) {
          await updateModule(payload)
          ElMessage.success('更新成功')
        } else {
          await addModule(payload)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        await handleQuery()
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确认删除模块「${row.moduleName}」吗？`, '删除确认', { type: 'warning' })
      await deleteModule(row.id)
      ElMessage.success('删除成功')
      await handleQuery()
    }

    onMounted(async () => {
      await loadSiteOptions()
      await loadDatasourceOptions()
      await handleQuery()
    })

    return {
      loading,
      tableData,
      total,
      queryForm,
      form,
      paramForm,
      rules,
      formRef,
      dialogVisible,
      dialogTitle,
      submitting,
      siteOptions,
      datasourceOptions,
      categoryOptions,
      checkTypeOptions,
      tableHeaderStyle,
      disasterCount,
      forecastCount,
      errorCount,
      currentCheckFields,
      formatDatasourceLabel,
      formatTime,
      formatStatus,
      statusKey,
      shouldShowModuleStatus,
      formatCheckType,
      categoryKey,
      categoryIcon,
      handleCheckTypeChange,
      handleQuery,
      handleReset,
      handleSizeChange,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.monitor-module-page {
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
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

.stat-card--total .stat-icon { background: linear-gradient(135deg, #e6f4ff, #bae0ff); color: #1890ff; }
.stat-card--disaster .stat-icon { background: linear-gradient(135deg, #fff1f0, #ffccc7); color: #ff4d4f; }
.stat-card--forecast .stat-icon { background: linear-gradient(135deg, #fffbe6, #ffe58f); color: #faad14; }
.stat-card--error .stat-icon { background: linear-gradient(135deg, #f9f0ff, #efdbff); color: #722ed1; }

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

.module-table {
  --el-table-border-color: #f0f0f0;
}

.module-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  font-size: 18px;
  flex-shrink: 0;
}

.module-icon--disaster {
  background: linear-gradient(135deg, #fff1f0, #fff7e6);
  color: #ff4d4f;
}

.module-icon--forecast {
  background: linear-gradient(135deg, #fffbe6, #e6fffb);
  color: #faad14;
}

.module-cell-name {
  display: block;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.module-cell-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.check-type {
  font-size: 13px;
  color: #595959;
}

.alarm-preview {
  font-size: 13px;
  color: #262626;
}

.cycle-text {
  font-size: 13px;
  color: #595959;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge--normal { background: #f6ffed; color: #52c41a; }
.status-badge--warning { background: #fffbe6; color: #faad14; }
.status-badge--error { background: #fff1f0; color: #ff4d4f; }

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.text-muted {
  color: #bfbfbf;
  font-size: 13px;
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

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .monitor-module-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
