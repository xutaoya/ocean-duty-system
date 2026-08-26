<template>
  <!-- start 值班日志列表 -->
  <div class="page-container duty-log-page">
    <el-card>
      <div class="page-toolbar">
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="值班人员">
            <el-input v-model="queryForm.userName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          填写日志
        </el-button>
      </div>

      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="userName" label="值班人员" width="120" />
        <el-table-column prop="dutyTime" label="值班时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.dutyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="siteStatus" label="网站状态" show-overflow-tooltip />
        <el-table-column prop="moduleStatus" label="模块状态" show-overflow-tooltip />
        <el-table-column prop="problem" label="故障原因" show-overflow-tooltip />
        <el-table-column prop="solution" label="处理措施" show-overflow-tooltip />
        <el-table-column prop="recoverTime" label="恢复时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.recoverTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
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
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="值班时间" prop="dutyTime">
          <el-date-picker
            v-model="form.dutyTime"
            type="datetime"
            placeholder="选择值班时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="网站状态" prop="siteStatus">
          <el-input v-model="form.siteStatus" type="textarea" :rows="2" placeholder="网站运行状态摘要" />
        </el-form-item>
        <el-form-item label="模块状态" prop="moduleStatus">
          <el-input v-model="form.moduleStatus" type="textarea" :rows="2" placeholder="模块更新状态摘要" />
        </el-form-item>
        <el-form-item label="故障原因" prop="problem">
          <el-input v-model="form.problem" type="textarea" :rows="2" placeholder="如有异常请填写原因" />
        </el-form-item>
        <el-form-item label="处理措施" prop="solution">
          <el-input v-model="form.solution" type="textarea" :rows="2" placeholder="处理过程与结果" />
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
  <!-- end 值班日志列表 -->
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDutyLog, deleteDutyLog, queryDutyLog, updateDutyLog } from '@/api/duty'

export default {
  name: 'DutyLogList',
  setup() {
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const tableData = ref([])
    const total = ref(0)

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

    const rules = {
      dutyTime: [{ required: true, message: '请选择值班时间', trigger: 'change' }]
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await queryDutyLog(queryForm)
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
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
      await ElMessageBox.confirm('确定删除这条值班日志吗？', '提示', { type: 'warning' })
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
      formRef,
      tableData,
      total,
      queryForm,
      form,
      rules,
      dialogTitle,
      formatTime,
      handleQuery,
      resetForm,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleDelete
    }
  }
}
</script>

<style scoped>
.duty-log-page {
  padding: 20px;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.query-form {
  margin-bottom: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .duty-log-page {
    padding: 12px;
  }

  .page-toolbar {
    flex-direction: column;
  }
}
</style>
