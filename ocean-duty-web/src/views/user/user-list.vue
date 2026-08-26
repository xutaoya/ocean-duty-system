<template>
  <!-- start 用户管理列表 -->
  <div class="user-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">管理系统账号、角色与启用状态</p>
      </div>
      <el-button type="primary" class="add-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <div class="toolbar-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent="handleQuery">
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="搜索用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部角色" clearable style="width: 140px">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" class="user-table">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" effect="plain" round>
              {{ formatRole(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" round>
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="danger"
              link
              :disabled="row.id === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="isEdit ? '留空表示不修改' : '登录密码'"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.desc"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.desc }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
  <!-- end 用户管理列表 -->
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addUser, deleteUser, queryUser, updateUser } from '@/api/user'
import { USER_ROLE, USER_ROLE_OPTIONS, USER_STATUS, USER_STATUS_OPTIONS } from '@/constants/user'

export default {
  name: 'UserList',
  setup() {
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const tableData = ref([])
    const total = ref(0)

    const queryForm = reactive({
      username: '',
      role: '',
      pageNum: 1,
      pageSize: 10
    })

    const form = reactive({
      id: null,
      username: '',
      password: '',
      realName: '',
      role: USER_ROLE.DUTY.value,
      status: USER_STATUS.ENABLED.value
    })

    const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'))

    const validatePassword = (rule, value, callback) => {
      if (!isEdit.value && !value) {
        callback(new Error('请输入密码'))
        return
      }
      callback()
    }

    const rules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ validator: validatePassword, trigger: 'blur' }],
      realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }]
    }

    const formatRole = (role) => {
      const item = USER_ROLE_OPTIONS.find(option => option.value === role)
      return item ? item.desc : role
    }

    const formatStatus = (status) => {
      const item = USER_STATUS_OPTIONS.find(option => option.value === status)
      return item ? item.desc : '-'
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    const handleQuery = async () => {
      loading.value = true
      try {
        const res = await queryUser(queryForm)
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } finally {
        loading.value = false
      }
    }

    const handleReset = () => {
      queryForm.username = ''
      queryForm.role = ''
      queryForm.pageNum = 1
      handleQuery()
    }

    const resetForm = () => {
      form.id = null
      form.username = ''
      form.password = ''
      form.realName = ''
      form.role = USER_ROLE.DUTY.value
      form.status = USER_STATUS.ENABLED.value
    }

    const handleAdd = () => {
      isEdit.value = false
      resetForm()
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      isEdit.value = true
      form.id = row.id
      form.username = row.username
      form.password = ''
      form.realName = row.realName
      form.role = row.role
      form.status = row.status
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      submitting.value = true
      try {
        if (isEdit.value) {
          await updateUser({ ...form })
          ElMessage.success('更新成功')
        } else {
          await addUser({ ...form })
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
      } finally {
        submitting.value = false
      }
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
      await deleteUser(row.id)
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
      isEdit,
      formRef,
      tableData,
      total,
      queryForm,
      form,
      rules,
      dialogTitle,
      roleOptions: USER_ROLE_OPTIONS,
      statusOptions: USER_STATUS_OPTIONS,
      formatRole,
      formatStatus,
      formatTime,
      handleQuery,
      handleReset,
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
.user-page {
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
  color: #1a1a2e;
  margin-bottom: 6px;
}

.page-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.toolbar-card,
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f4;
  padding: 20px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .user-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
