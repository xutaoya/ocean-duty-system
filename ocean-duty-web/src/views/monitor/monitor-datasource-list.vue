<template>
  <div class="datasource-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">数据源管理</h1>
        <p class="page-desc">配置模块检测使用的 MySQL 连接与数据表</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增数据源
      </el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="dsName" label="名称" min-width="160" />
        <el-table-column prop="host" label="主机" min-width="140" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="databaseName" label="数据库" width="120" />
        <el-table-column prop="tableName" label="数据表" width="160" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round>
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleTest(row)">测试</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="pagination"
        @current-change="handleQuery"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="dsName">
          <el-input v-model="form.dsName" placeholder="如 中国海洋预报网CMS" />
        </el-form-item>
        <el-form-item label="类型" prop="dsType">
          <el-select v-model="form.dsType" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空表示不修改' : '数据库密码'" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
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

export default {
  name: 'MonitorDatasourceList',
  setup() {
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const tableData = ref([])
    const total = ref(0)

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
      await ElMessageBox.confirm(`确定删除数据源「${row.dsName}」吗？`, '提示', { type: 'warning' })
      await deleteDatasource(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    }

    const handleTest = async (row) => {
      await testDatasource(row.id)
      ElMessage.success('连接成功')
    }

    onMounted(handleQuery)

    return {
      loading,
      submitting,
      dialogVisible,
      isEdit,
      formRef,
      tableData,
      total,
      queryForm,
      form,
      rules,
      dialogTitle,
      handleQuery,
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
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  padding: 20px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
