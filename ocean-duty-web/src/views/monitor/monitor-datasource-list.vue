<template>
  <!-- start 数据源管理 -->
  <div class="datasource-page admin-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">数据源管理</h1>
        <p class="page-desc">配置模块检测使用的 MySQL 连接与数据表</p>
      </div>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增数据源
      </el-button>
    </div>

    <div class="stats-row">
      <div class="stat-card stat-card--total">
        <div class="stat-icon"><el-icon><Coin /></el-icon></div>
        <div>
          <span class="stat-value">{{ total }}</span>
          <span class="stat-label">数据源总数</span>
        </div>
      </div>
      <div class="stat-card stat-card--active">
        <div class="stat-icon"><el-icon><CircleCheckFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ activeCount }}</span>
          <span class="stat-label">已启用</span>
        </div>
      </div>
      <div class="stat-card stat-card--disabled">
        <div class="stat-icon"><el-icon><CircleCloseFilled /></el-icon></div>
        <div>
          <span class="stat-value">{{ disabledCount }}</span>
          <span class="stat-label">已禁用</span>
        </div>
      </div>
    </div>

    <div class="toolbar-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent="handleQuery">
        <el-form-item label="数据源名称">
          <el-input
            v-model="queryForm.dsName"
            placeholder="搜索数据源名称"
            clearable
            style="width: 200px"
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
        <span class="table-title">数据源列表</span>
        <span class="table-count">共 {{ total }} 个数据源</span>
      </div>

      <div class="admin-table-wrap">
        <el-table
          v-loading="loading"
          :data="tableData"
          class="ds-table"
          :header-cell-style="tableHeaderStyle"
        >
        <el-table-column label="数据源" min-width="200">
          <template #default="{ row }">
            <div class="ds-cell">
              <div class="ds-icon">
                <el-icon><Coin /></el-icon>
              </div>
              <div>
                <span class="ds-name">{{ row.dsName }}</span>
                <el-tag size="small" type="info" effect="plain" round>{{ row.dsType || 'mysql' }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="连接地址" min-width="200">
          <template #default="{ row }">
            <div class="conn-info">
              <span class="conn-host">{{ row.host }}:{{ row.port }}</span>
              <span class="conn-db">{{ row.databaseName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="数据表" width="180">
          <template #default="{ row }">
            <code v-if="row.tableName" class="table-name">{{ row.tableName }}</code>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" width="120" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--disabled']">
              <span class="status-dot" />
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="success"
              link
              :loading="testingId === row.id"
              @click="handleTest(row)"
            >
              <el-icon><Connection /></el-icon>
              测试
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
            <el-icon :size="48"><Coin /></el-icon>
            <p>暂无数据源</p>
            <el-button type="primary" link @click="handleAdd">立即新增</el-button>
          </div>
        </template>
      </el-table>
      </div>

      <div v-loading="loading" class="admin-mobile-list">
        <div v-for="row in tableData" :key="row.id" class="admin-mobile-card">
          <div class="admin-mobile-card__header">
            <div class="ds-cell">
              <div class="ds-icon"><el-icon><Coin /></el-icon></div>
              <div>
                <span class="ds-name">{{ row.dsName }}</span>
                <el-tag size="small" type="info" effect="plain" round>{{ row.dsType || 'mysql' }}</el-tag>
              </div>
            </div>
            <span :class="['status-badge', row.status === 1 ? 'status-badge--active' : 'status-badge--disabled']">
              <span class="status-dot" />
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </div>
          <div class="admin-mobile-card__meta">
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">连接地址</span>
              <span class="admin-mobile-meta-value">{{ row.host }}:{{ row.port }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">数据库</span>
              <span class="admin-mobile-meta-value">{{ row.databaseName }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">数据表</span>
              <span class="admin-mobile-meta-value">{{ row.tableName || '-' }}</span>
            </div>
            <div class="admin-mobile-meta-row">
              <span class="admin-mobile-meta-label">用户名</span>
              <span class="admin-mobile-meta-value">{{ row.username }}</span>
            </div>
          </div>
          <div class="admin-mobile-card__actions">
            <el-button type="success" plain size="small" :loading="testingId === row.id" @click="handleTest(row)"><el-icon><Connection /></el-icon>测试</el-button>
            <el-button type="primary" plain size="small" @click="handleEdit(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button type="danger" plain size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </div>
        </div>
        <div v-if="!loading && !tableData.length" class="empty-state">
          <el-icon :size="48"><Coin /></el-icon>
          <p>暂无数据源</p>
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
      :width="isMobile ? '100%' : '560px'"
      :top="isMobile ? '0' : '8vh'"
      :fullscreen="isMobile"
      destroy-on-close
      :class="['admin-dialog', 'ds-dialog', { 'admin-dialog--mobile': isMobile }]"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-position="isMobile ? 'top' : 'right'"
        :label-width="isMobile ? 'auto' : '90px'"
        class="ds-form"
      >
        <div class="form-section">
          <div class="form-section-title">连接信息</div>
          <el-form-item label="名称" prop="dsName">
            <el-input v-model="form.dsName" placeholder="如 中国海洋预报网CMS" />
          </el-form-item>
          <el-form-item label="类型" prop="dsType">
            <el-select v-model="form.dsType" style="width: 100%">
              <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            </el-select>
          </el-form-item>
          <el-form-item label="主机" prop="host">
            <el-input v-model="form.host" placeholder="IP 或域名" />
          </el-form-item>
          <el-form-item label="端口" prop="port">
            <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%" />
          </el-form-item>
          <el-form-item label="数据库" prop="databaseName">
            <el-input v-model="form.databaseName" placeholder="数据库名" />
          </el-form-item>
          <el-form-item label="数据表" prop="tableName">
            <el-input v-model="form.tableName" placeholder="如 cms_forecast_alarm" />
          </el-form-item>
        </div>

        <div class="form-section">
          <div class="form-section-title">认证与状态</div>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="isEdit ? '留空表示不修改' : '数据库密码'"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div :class="['admin-dialog-footer', { 'admin-dialog-footer--mobile': isMobile }]">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
  <!-- end 数据源管理 -->
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDatasource,
  deleteDatasource,
  queryDatasource,
  testDatasource,
  updateDatasource
} from '@/api/monitor-datasource'
import { useMobileLayout } from '@/composables/use-mobile-layout'

export default {
  name: 'MonitorDatasourceList',
  setup() {
    const { isMobile } = useMobileLayout()
    const loading = ref(false)
    const submitting = ref(false)
    const testingId = ref(null)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const tableData = ref([])
    const total = ref(0)

    const tableHeaderStyle = { background: '#fafbfc', color: '#595959', fontWeight: '600' }

    const queryForm = reactive({ dsName: '', pageNum: 1, pageSize: 10 })
    const form = reactive({
      id: null,
      dsName: '',
      dsType: 'mysql',
      host: '',
      port: 3306,
      databaseName: '',
      tableName: '',
      username: '',
      password: '',
      status: 1
    })

    const dialogTitle = computed(() => (isEdit.value ? '编辑数据源' : '新增数据源'))
    const activeCount = computed(() => tableData.value.filter(row => row.status === 1).length)
    const disabledCount = computed(() => tableData.value.filter(row => row.status !== 1).length)

    const validatePassword = (rule, value, callback) => {
      if (!isEdit.value && !value) {
        callback(new Error('请输入密码'))
        return
      }
      callback()
    }

    const rules = {
      dsName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
      dsType: [{ required: true, message: '请选择类型', trigger: 'change' }],
      host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
      port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
      databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
      tableName: [{ required: true, message: '请输入数据表名', trigger: 'blur' }],
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ validator: validatePassword, trigger: 'blur' }]
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await queryDatasource(queryForm)
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
    }

    const handleReset = () => {
      queryForm.dsName = ''
      queryForm.pageNum = 1
      handleQuery()
    }

    const handleSizeChange = () => {
      queryForm.pageNum = 1
      handleQuery()
    }

    const resetForm = () => {
      Object.assign(form, {
        id: null,
        dsName: '',
        dsType: 'mysql',
        host: '',
        port: 3306,
        databaseName: '',
        tableName: '',
        username: '',
        password: '',
        status: 1
      })
    }

    const handleAdd = () => {
      isEdit.value = false
      resetForm()
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      isEdit.value = true
      Object.assign(form, {
        id: row.id,
        dsName: row.dsName,
        dsType: row.dsType,
        host: row.host,
        port: row.port,
        databaseName: row.databaseName,
        tableName: row.tableName,
        username: row.username,
        password: '',
        status: row.status
      })
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateDatasource({ ...form })
          ElMessage.success('更新成功')
        } else {
          await addDatasource({ ...form })
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确定删除数据源「${row.dsName}」吗？`, '删除确认', { type: 'warning' })
      await deleteDatasource(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    }

    const handleTest = async (row) => {
      testingId.value = row.id
      try {
        await testDatasource(row.id)
        ElMessage.success('连接成功')
      } finally {
        testingId.value = null
      }
    }

    onMounted(handleQuery)

    return {
      isMobile,
      loading,
      submitting,
      testingId,
      dialogVisible,
      isEdit,
      formRef,
      tableData,
      total,
      queryForm,
      form,
      rules,
      dialogTitle,
      tableHeaderStyle,
      activeCount,
      disabledCount,
      handleQuery,
      handleReset,
      handleSizeChange,
      resetForm,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleDelete,
      handleTest
    }
  }
}
</script>

<style scoped>
.datasource-page {
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
.stat-card--active .stat-icon { background: linear-gradient(135deg, #f6ffed, #d9f7be); color: #52c41a; }
.stat-card--disabled .stat-icon { background: linear-gradient(135deg, #f5f5f5, #e8e8e8); color: #8c8c8c; }

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

.ds-table {
  --el-table-border-color: #f0f0f0;
}

.ds-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ds-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e6f4ff, #f0f5ff);
  color: #1890ff;
  font-size: 18px;
  flex-shrink: 0;
}

.ds-name {
  display: block;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.conn-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conn-host {
  font-size: 13px;
  color: #262626;
  font-family: 'SF Mono', Menlo, monospace;
}

.conn-db {
  font-size: 12px;
  color: #8c8c8c;
}

.table-name {
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
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

.status-badge--active { background: #f6ffed; color: #52c41a; }
.status-badge--disabled { background: #f5f5f5; color: #8c8c8c; }

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
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
</style>
