<template>
  <!-- start 网站管理列表 -->
  <div class="page-container monitor-site-page">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="网站名称">
          <el-input v-model="queryForm.siteName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="网站类型">
          <el-select v-model="queryForm.siteType" placeholder="全部" clearable style="width: 140px">
            <el-option
              v-for="item in siteTypeOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleAdd">新增网站</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="siteName" label="网站名称" min-width="160" />
        <el-table-column prop="siteUrl" label="网站地址" min-width="260" show-overflow-tooltip />
        <el-table-column prop="siteType" label="类型" width="100">
          <template #default="{ row }">
            {{ formatSiteType(row.siteType) }}
          </template>
        </el-table-column>
        <el-table-column prop="timeoutMs" label="探测超时(ms)" width="120" />
        <el-table-column prop="responseThreshold" label="响应阈值(ms)" width="120" />
        <el-table-column prop="lastCheckTime" label="最近检测" width="170">
          <template #default="{ row }">
            {{ formatTime(row.lastCheckTime) }}
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="网站名称" prop="siteName">
          <el-input v-model="form.siteName" placeholder="请输入网站名称" />
        </el-form-item>
        <el-form-item label="网站地址" prop="siteUrl">
          <el-input v-model="form.siteUrl" placeholder="https://..." />
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
        <el-form-item label="探测超时(ms)" prop="timeoutMs">
          <el-input-number v-model="form.timeoutMs" :min="1000" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="响应阈值(ms)" prop="responseThreshold">
          <el-input-number v-model="form.responseThreshold" :min="100" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
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
import { MONITOR_STATUS } from '@/constants/monitor'

export default {
  name: 'MonitorSiteList',
  setup() {
    const tableData = ref([])
    const total = ref(0)
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增网站')
    const submitting = ref(false)
    const formRef = ref(null)

    const siteTypeOptions = Object.values(SITE_TYPE)

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

    /**
     * 格式化时间
     */
    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    /**
     * 格式化网站类型
     */
    const formatSiteType = (type) => {
      const item = siteTypeOptions.find(option => option.value === type)
      return item ? item.desc : type
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
     * 重置表单
     */
    const resetForm = () => {
      form.id = null
      form.siteName = ''
      form.siteUrl = ''
      form.siteType = SITE_TYPE.PORTAL.value
      form.timeoutMs = 10000
      form.responseThreshold = 3000
    }

    /**
     * 查询列表
     */
    const handleQuery = async () => {
      const res = await querySite(queryForm)
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }

    /**
     * 打开新增弹窗
     */
    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '新增网站'
      dialogVisible.value = true
    }

    /**
     * 打开编辑弹窗
     */
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

    /**
     * 提交保存
     */
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

    /**
     * 删除网站
     */
    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确认删除网站「${row.siteName}」吗？`, '提示', { type: 'warning' })
      await deleteSite(row.id)
      ElMessage.success('删除成功')
      await handleQuery()
    }

    onMounted(() => {
      handleQuery()
    })

    return {
      tableData,
      total,
      queryForm,
      form,
      rules,
      formRef,
      dialogVisible,
      dialogTitle,
      submitting,
      siteTypeOptions,
      formatTime,
      formatSiteType,
      formatStatus,
      statusTagType,
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
.monitor-site-page {
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
  .monitor-site-page {
    padding: 12px;
  }
}
</style>
