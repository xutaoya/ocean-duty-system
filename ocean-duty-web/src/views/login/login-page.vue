<template>
  <!-- start 登录页 -->
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="login-title">海洋预报值班监控系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
  <!-- end 登录页 -->
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/login'
import { useUserStore } from '@/store/user'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const formRef = ref(null)
    const loading = ref(false)

    const form = reactive({
      username: '',
      password: ''
    })

    const rules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    /**
     * 提交登录
     */
    const handleLogin = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      loading.value = true
      try {
        const res = await login(form)
        userStore.setLoginInfo(res.data)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } finally {
        loading.value = false
      }
    }

    return { formRef, form, rules, loading, handleLogin }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1a3a5c 0%, #0d2137 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-btn {
  width: 100%;
}

@media (max-width: 768px) {
  .login-card {
    width: 90%;
    margin: 0 16px;
  }
}
</style>
