<template>
  <!-- start 监控模块管理 -->
  <div class="page-container monitor-module-page">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="模块名称">
          <el-input v-model="queryForm.moduleName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="模块分类">
          <el-select v-model="queryForm.moduleCategory" placeholder="全部" clearable style="width: 140px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleAdd">新增模块</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="moduleCategoryName" label="分类" width="100" />
        <el-table-column prop="moduleGroup" label="分组" width="120" />
        <el-table-column prop="moduleName" label="模块名称" min-width="150" />
        <el-table-column prop="moduleUrl" label="页面地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="expectedTime" label="更新周期" width="100">
          <template #default="{ row }">
            每天 {{ row.expectedTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后更新" width="170">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
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
          <el-select v-model="form.siteId" placeholder="请选择" style="width: 100%">
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
          <el-input v-model="paramForm[field.key]" :placeholder="field.placeholder" />
        </el-form-item>
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
import { MODULE_CATEGORY, MONITOR_STATUS } from '@/constants/monitor'
import { MODULE_CHECK_TYPE, MODULE_CATEGORY_OPTIONS } from '@/constants/module'

export default {
  name: 'MonitorModuleList',
  setup() {
    const tableData = ref([])
    const total = ref(0)
    const siteOptions = ref([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增模块')
    const submitting = ref(false)
    const formRef = ref(null)
    const paramForm = reactive({})

    const categoryOptions = MODULE_CATEGORY_OPTIONS
    const checkTypeOptions = Object.values(MODULE_CHECK_TYPE)

    const queryForm = reactive({
      moduleName: '',
      moduleCategory: '',
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

    const currentCheckFields = computed(() => {
      const item = checkTypeOptions.find(option => option.value === form.checkType)
      return item ? item.fields : []
    })

    /**
     * 格式化时间
     */
    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    /**
     * 格式化状态
     */
    const formatStatus = (status) => {
      const item = Object.values(MONITOR_STATUS).find(option => option.value === status)
      return item ? item.desc : '未知'
    }

    /**
     * 状态标签类型
     */
    const statusTagType = (status) => {
      if (status === MONITOR_STATUS.ERROR.value) return 'danger'
      if (status === MONITOR_STATUS.WARNING.value) return 'warning'
      return 'success'
    }

    /**
     * 重置检测参数表单
     */
    const resetParamForm = (checkType) => {
      Object.keys(paramForm).forEach(key => delete paramForm[key])
      const item = checkTypeOptions.find(option => option.value === checkType)
      if (!item) return
      item.fields.forEach(field => {
        paramForm[field.key] = ''
      })
    }

    /**
     * 解析检测参数
     */
    const parseCheckParam = (checkParam) => {
      resetParamForm(form.checkType)
      if (!checkParam) return
      try {
        const data = JSON.parse(checkParam)
        Object.keys(data).forEach(key => {
          paramForm[key] = data[key]
        })
      } catch (error) {
        ElMessage.warning('检测参数解析失败')
      }
    }

    /**
     * 构建检测参数JSON
     */
    const buildCheckParam = () => {
      const data = {}
      currentCheckFields.value.forEach(field => {
        const value = paramForm[field.key]
        if (value) {
          data[field.key] = value
        }
      })
      return JSON.stringify(data)
    }

    /**
     * 检测方式变更
     */
    const handleCheckTypeChange = (value) => {
      resetParamForm(value)
    }

    /**
     * 重置表单
     */
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

    /**
     * 加载网站选项
     */
    const loadSiteOptions = async () => {
      const res = await listSites()
      siteOptions.value = res.data || []
    }

    /**
     * 查询列表
     */
    const handleQuery = async () => {
      const res = await queryModule(queryForm)
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }

    /**
     * 新增模块
     */
    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增模块'
      dialogVisible.value = true
    }

    /**
     * 编辑模块
     */
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

    /**
     * 提交保存
     */
    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      submitting.value = true
      try {
        const payload = {
          ...form,
          checkParam: buildCheckParam()
        }
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

    /**
     * 删除模块
     */
    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确认删除模块「${row.moduleName}」吗？`, '提示', { type: 'warning' })
      await deleteModule(row.id)
      ElMessage.success('删除成功')
      await handleQuery()
    }

    onMounted(async () => {
      await loadSiteOptions()
      await handleQuery()
    })

    return {
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
      categoryOptions,
      checkTypeOptions,
      currentCheckFields,
      formatTime,
      formatStatus,
      statusTagType,
      handleCheckTypeChange,
      handleQuery,
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
  padding: 20px;
}

.query-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .monitor-module-page {
    padding: 12px;
  }
}
</style>
