<template>
  <!-- start 值班日志列表 -->
  <div class="page-container duty-log-page">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="值班人员">
          <el-input v-model="queryForm.userName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="userName" label="值班人员" width="120" />
        <el-table-column prop="dutyTime" label="值班时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.dutyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="siteStatus" label="网站状态" show-overflow-tooltip />
        <el-table-column prop="problem" label="故障原因" show-overflow-tooltip />
        <el-table-column prop="solution" label="处理措施" show-overflow-tooltip />
        <el-table-column prop="recoverTime" label="恢复时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.recoverTime) }}
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
  </div>
  <!-- end 值班日志列表 -->
</template>

<script>
import { onMounted, reactive, ref } from 'vue'
import { queryDutyLog } from '@/api/duty'

export default {
  name: 'DutyLogList',
  setup() {
    const tableData = ref([])
    const total = ref(0)

    const queryForm = reactive({
      userName: '',
      pageNum: 1,
      pageSize: 10
    })

    /**
     * 格式化时间
     */
    const formatTime = (time) => {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    }

    /**
     * 查询日志列表
     */
    const handleQuery = async () => {
      const res = await queryDutyLog(queryForm)
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }

    onMounted(() => {
      handleQuery()
    })

    return { tableData, total, queryForm, formatTime, handleQuery }
  }
}
</script>

<style scoped>
.duty-log-page {
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
  .duty-log-page {
    padding: 12px;
  }
}
</style>
