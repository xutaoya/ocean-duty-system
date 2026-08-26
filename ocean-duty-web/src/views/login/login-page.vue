<template>
  <!-- start 登录页 -->
  <div class="login-page">
    <!-- 左侧品牌展示区 -->
    <section class="login-brand">
      <div class="brand-bg">
        <div class="brand-orb brand-orb--1" />
        <div class="brand-orb brand-orb--2" />
        <div class="brand-orb brand-orb--3" />
        <svg class="brand-wave" viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path
            d="M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
          />
        </svg>
      </div>

      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="36"><Monitor /></el-icon>
        </div>
        <h1 class="brand-title">海洋预报值班监控系统</h1>
        <p class="brand-subtitle">Ocean Forecast Duty Monitoring Platform</p>

        <ul class="brand-features">
          <li>
            <span class="feature-icon"><el-icon><Monitor /></el-icon></span>
            <div>
              <strong>实时监控</strong>
              <span>网站可用性与响应状态全天候追踪</span>
            </div>
          </li>
          <li>
            <span class="feature-icon"><el-icon><WarningFilled /></el-icon></span>
            <div>
              <strong>异常告警</strong>
              <span>故障站点即时提醒，保障业务连续</span>
            </div>
          </li>
          <li>
            <span class="feature-icon"><el-icon><Document /></el-icon></span>
            <div>
              <strong>值班日志</strong>
              <span>规范化记录，可追溯可审计</span>
            </div>
          </li>
        </ul>
      </div>
    </section>

    <!-- 右侧登录表单区 -->
    <section class="login-form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息以访问系统</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <label class="field-label">用户名</label>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <label class="field-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="form-footer">© {{ currentYear }} 海洋预报值班监控系统</p>
      </div>
    </section>
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
    const currentYear = new Date().getFullYear()

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

    return { formRef, form, rules, loading, currentYear, handleLogin }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: #f4f7fb;
}

/* ---- 左侧品牌区 ---- */
.login-brand {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(145deg, #041525 0%, #0a2d4a 45%, #0d4d6e 100%);
}

.brand-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.brand-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.35;
  animation: float 8s ease-in-out infinite;
}

.brand-orb--1 {
  width: 320px;
  height: 320px;
  top: -80px;
  right: -60px;
  background: #1890ff;
  animation-delay: 0s;
}

.brand-orb--2 {
  width: 240px;
  height: 240px;
  bottom: 20%;
  left: -40px;
  background: #13c2c2;
  animation-delay: -3s;
}

.brand-orb--3 {
  width: 180px;
  height: 180px;
  top: 40%;
  right: 20%;
  background: #2f54eb;
  animation-delay: -5s;
}

.brand-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 180px;
}

.brand-wave path {
  fill: rgba(255, 255, 255, 0.06);
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 480px;
  padding: 48px;
  color: #fff;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin-bottom: 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.brand-title {
  font-size: 32px;
  font-weight: 600;
  line-height: 1.3;
  letter-spacing: 1px;
  margin-bottom: 10px;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 0.5px;
  margin-bottom: 48px;
}

.brand-features {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand-features li {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 18px;
}

.brand-features strong {
  display: block;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.brand-features span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.5;
}

/* ---- 右侧表单区 ---- */
.login-form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 520px;
  flex-shrink: 0;
  background: #fff;
  box-shadow: -8px 0 32px rgba(0, 21, 41, 0.06);
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
  padding: 48px 40px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: #8c8c8c;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #595959;
  margin-bottom: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: box-shadow 0.2s;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1890ff inset;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  border-radius: 8px;
  font-size: 15px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  transition: opacity 0.2s, transform 0.15s;
}

.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.form-footer {
  margin-top: 48px;
  text-align: center;
  font-size: 12px;
  color: #bfbfbf;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* ---- 响应式 ---- */
@media (max-width: 960px) {
  .login-page {
    flex-direction: column;
  }

  .login-brand {
    flex: none;
    min-height: 280px;
    padding: 32px 0;
  }

  .brand-content {
    padding: 24px 32px;
  }

  .brand-title {
    font-size: 24px;
  }

  .brand-subtitle {
    margin-bottom: 0;
  }

  .brand-features {
    display: none;
  }

  .login-form-panel {
    width: 100%;
    flex: 1;
    box-shadow: none;
  }

  .form-wrapper {
    padding: 32px 24px;
  }
}
</style>
