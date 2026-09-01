<template>
  <!-- start 网站管理列表 -->
  <div class="monitor-site-page admin-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">网站管理</h1>
        <p class="page-desc">配置监控站点、探测参数与响应阈值</p>
      </div>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增网站
      </el-button>
    </div>

    <div class="toolbar-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent="handleQuery">
        <el-form-item label="网站名称">
          <el-input
            v-model="queryForm.siteName"
            placeholder="搜索网站名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="网站类型">
          <el-select v-model="queryForm.siteType" placeholder="全部类型" clearable style="width: 140px">
            <el-option
              v-for="item in siteTypeOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
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
        <span class="table-title">站点列表</span>
        <span class="table-count">共 {{ total }} 个站点</span>
      </div>

      <div class="admin-table-wrap">
        <el-table
          v-loading="loading"
          :data="tableData"
          class="site-table"
          :header-cell-style="tableHeaderStyle"
        >
        <el-table-column label="网站" min-width="220">
          <template #default="{ row }">
            <div class="site-cell">
              <SiteFaviconAvatar :site-url="row.siteUrl" :name="row.siteName" size="sm" />
              <div class="site-cell-info">
                <span class="site-cell-name">{{ row.siteName }}</span>
                <el-tag size="small" type="info" effect="plain" round>
                  {{ formatSiteType(row.siteType) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="网站地址" min-width="240">
          <template #default="{ row }">
            <a
              v-if="row.siteUrl"
              :href="row.siteUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="site-link"
            >
              {{ row.siteUrl }}
              <el-icon><TopRight /></el-icon>
            </a>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="HTTP" width="90" align="center">
          <template #default="{ row }">
            <span :class="['http-code', getHttpStatusClass(row.httpStatus)]">
              {{ row.httpStatus || '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="响应时间" width="100" align="center">
          <template #default="{ row }">
            <span :class="['response-time', responseTimeClass(row)]">
              {{ row.responseTime != null ? `${row.responseTime} ms` : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-badge', `status-badge--${statusKey(row.status)}`]">
              <span class="status-dot" />
              {{ formatStatus(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="最近检测" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatTime(row.lastCheckTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="探测配置" width="150">
          <template #default="{ row }">
            <div class="config-cell">
              <span>超时 {{ row.timeoutMs }}ms</span>
              <span>阈值 {{ row.responseThreshold }}ms</span>
            </div>
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
            <el-icon :size="48"><Monitor /></el-icon>
            <p>暂无网站数据</p>
            <el-button type="primary" link @click="handleAdd">立即新增</el-button>
          </div>
        </template>
      </el-table>
      </div>

      <div v-loading="loading" class="admin-mobile-list">
        <div v-for="row in tableData" :key="row.id" class="admin-mobile-card">
          <div class="admin-mobile-card__header">
            <div class="site-cell">
              <SiteFaviconAvatar :site-url="row.siteUrl" :name="row.siteName" size="sm" />
              <div class="site-cell-info">
                <span class="site-cell-name">{{ row.siteName }}</span>
                <el-tag size="small" type="info" effect="plain" round>{{ formatSiteType(row.siteType) }}</el-tag>
              </div>
            </div>
            <span :class="['status-badge', `status-badge--${statusKey(row.status)}`]">
              <span class="status-dot" />
              {{ formatStatus(row.status) }}
            </span>
          </div>
          <div class="admin-mobile-card__meta">
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">网站地址</span>
              <a v-if="row.siteUrl" :href="row.siteUrl" target="_blank" rel="noopener noreferrer" class="site-link admin-mobile-meta-value">{{ row.siteUrl }}</a>
              <span v-else class="text-muted">-</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">HTTP</span>
              <span :class="['http-code', getHttpStatusClass(row.httpStatus), 'admin-mobile-meta-value']">{{ row.httpStatus || '-' }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">响应时间</span>
              <span :class="['response-time', responseTimeClass(row), 'admin-mobile-meta-value']">{{ row.responseTime != null ? `${row.responseTime} ms` : '-' }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">最近检测</span>
              <span class="admin-mobile-meta-value text-muted">{{ formatTime(row.lastCheckTime) }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">探测配置</span>
              <span class="admin-mobile-meta-value">超时 {{ row.timeoutMs }}ms / 阈值 {{ row.responseThreshold }}ms</span>
            </div>
          </div>
          <div class="admin-mobile-card__actions">
            <el-button type="primary" plain size="small" @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" plain size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </div>
        </div>
        <div v-if="!loading && !tableData.length" class="empty-state">
          <el-icon :size="48"><Monitor /></el-icon>
          <p>暂无网站数据</p>
          <el-button type="primary" link @click="handleAdd">立即新增</el-button>
        </div>
      </div>

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
      :width="isMobile ? '100%' : '520px'"
      :top="isMobile ? '0' : '8vh'"
      :fullscreen="isMobile"
      destroy-on-close
      :class="['admin-dialog', 'site-dialog', { 'admin-dialog--mobile': isMobile }]"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-position="isMobile ? 'top' : 'right'"
        :label-width="isMobile ? 'auto' : '110px'"
        class="site-form"
      >
        <el-form-item label="网站名称" prop="siteName">
          <el-input v-model="form.siteName" placeholder="请输入网站名称" />
        </el-form-item>
        <el-form-item label="网站地址" prop="siteUrl">
          <el-input v-model="form.siteUrl" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="网站类型" prop="siteType">
          <el-select v-model="form.siteType" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="item in siteTypeOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="探测超时" prop="timeoutMs">
          <el-input-number v-model="form.timeoutMs" :min="1000" :step="1000" style="width: 100%" />
          <span class="form-tip">单位：毫秒，建议 10000</span>
        </el-form-item>
        <el-form-item label="响应阈值" prop="responseThreshold">
          <el-input-number v-model="form.responseThreshold" :min="100" :step="100" style="width: 100%" />
          <span class="form-tip">单位：毫秒，超过则标记警告</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div :class="['admin-dialog-footer', { 'admin-dialog-footer--mobile': isMobile }]">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
  <!-- end 网站管理列表 -->
</template>

<script>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { querySite, addSite, updateSite, deleteSite } from '@/api/monitor-site'
import { SITE_TYPE } from '@/constants/site'
import { MONITOR_STATUS, getHttpStatusClass } from '@/constants/monitor'
import SiteFaviconAvatar from '@/components/site-favicon-avatar.vue'
import { useMobileLayout } from '@/composables/use-mobile-layout'

export default {
  name: 'MonitorSiteList',
  components: { SiteFaviconAvatar },
  setup() {
    const { isMobile } = useMobileLayout()
    const tableData = ref([])
    const total = ref(0)
    const loading = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增网站')
    const submitting = ref(false)
    const formRef = ref(null)

    const siteTypeOptions = Object.values(SITE_TYPE)
    const tableHeaderStyle = { background: '#fafbfc', color: '#595959', fontWeight: '600' }

    const queryForm = reactive({
      siteName: '',
      siteType: '',
      pageNum: 1,
      pageSize: 10
    })

    const form = reactive({
      id: null,
      siteName: '',
      siteUrl: '',
      siteType: SITE_TYPE.PORTAL.value,
      timeoutMs: 10000,
      responseThreshold: 3000
    })

    const rules = {
      siteName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
      siteUrl: [{ required: true, message: '请输入网站地址', trigger: 'blur' }],
      siteType: [{ required: true, message: '请选择网站类型', trigger: 'change' }],
      timeoutMs: [{ required: true, message: '请输入探测超时', trigger: 'blur' }],
      responseThreshold: [{ required: true, message: '请输入响应阈值', trigger: 'blur' }]
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const formatSiteType = (type) => {
      const item = siteTypeOptions.find(option => option.value === type)
      return item ? item.desc : type
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

    const responseTimeClass = (row) => {
      if (row.responseTime == null) return ''
      if (row.responseThreshold && row.responseTime > row.responseThreshold) return 'is-slow'
      return 'is-fast'
    }

    const resetForm = () => {
      form.id = null
      form.siteName = ''
      form.siteUrl = ''
      form.siteType = SITE_TYPE.PORTAL.value
      form.timeoutMs = 10000
      form.responseThreshold = 3000
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await querySite(queryForm)
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
    }

    const handleReset = () => {
      queryForm.siteName = ''
      queryForm.siteType = ''
      queryForm.pageNum = 1
      handleQuery()
    }

    const handleSizeChange = () => {
      queryForm.pageNum = 1
      handleQuery()
    }

    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增网站'
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      form.id = row.id
      form.siteName = row.siteName
      form.siteUrl = row.siteUrl
      form.siteType = row.siteType
      form.timeoutMs = row.timeoutMs
      form.responseThreshold = row.responseThreshold
      dialogTitle.value = '编辑网站'
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      submitting.value = true
      try {
        if (form.id) {
          await updateSite(form)
          ElMessage.success('更新成功')
        } else {
          await addSite(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        await handleQuery()
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确认删除网站「${row.siteName}」吗？`, '删除确认', { type: 'warning' })
      await deleteSite(row.id)
      ElMessage.success('删除成功')
      await handleQuery()
    }

    onMounted(() => {
      handleQuery()
    })

    return {
      isMobile,
      tableData,
      total,
      loading,
      queryForm,
      form,
      rules,
      formRef,
      dialogVisible,
      dialogTitle,
      submitting,
      siteTypeOptions,
      tableHeaderStyle,
      formatTime,
      formatSiteType,
      formatStatus,
      statusKey,
      responseTimeClass,
      getHttpStatusClass,
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
.monitor-site-page {
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

.site-table {
  --el-table-border-color: #f0f0f0;
}

.site-table :deep(.el-table__row) {
  transition: background 0.2s;
}

.site-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.site-cell-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.site-cell-name {
  font-weight: 600;
  color: #1a1a2e;
}

.site-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #1890ff;
  text-decoration: none;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.site-link:hover {
  color: #096dd9;
}

.text-muted {
  color: #8c8c8c;
  font-size: 13px;
}

.http-code {
  font-weight: 600;
  font-size: 14px;
}

.http-code.http-success { color: #52c41a; }
.http-code.http-redirect { color: #1890ff; }
.http-code.http-client-error { color: #faad14; }
.http-code.http-server-error { color: #ff4d4f; }
.http-code.http-unknown { color: #bfbfbf; }

.response-time {
  font-size: 13px;
  font-weight: 500;
}

.response-time.is-fast { color: #52c41a; }
.response-time.is-slow { color: #faad14; }

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge--normal {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge--warning {
  background: #fffbe6;
  color: #faad14;
}

.status-badge--error {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.config-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #8c8c8c;
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

.form-tip {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #bfbfbf;
}
</style>
