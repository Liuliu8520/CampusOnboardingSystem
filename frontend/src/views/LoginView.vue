<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/modules'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const captchaLoading = ref(false)
const captchaId = ref('')
const captchaQuestion = ref('')
const form = reactive({
  role: 'STUDENT' as 'STUDENT' | 'ADMIN',
  username: '20260001',
  password: '123456',
  captcha: ''
})

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const data = await authApi.captcha()
    captchaId.value = data.captchaId
    captchaQuestion.value = data.question
    form.captcha = ''
  } finally {
    captchaLoading.value = false
  }
}

function switchRole(role: 'STUDENT' | 'ADMIN') {
  form.role = role
  form.username = role === 'ADMIN' ? 'admin' : '20260001'
}

async function submit() {
  loading.value = true
  try {
    const result = await auth.login({ ...form, captchaId: captchaId.value })
    ElMessage.success('登录成功')
    router.push(result.role === 'ADMIN' ? '/admin/dashboard' : '/student/home')
  } catch {
    // 错误提示已由请求拦截器统一处理；旧验证码已被服务端消费，需刷新
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <main class="login-page">
    <div class="bubbles" aria-hidden="true">
      <span style="left:6%;width:54px;height:54px;animation-duration:19s;animation-delay:0s"></span>
      <span style="left:18%;width:26px;height:26px;animation-duration:14s;animation-delay:3s"></span>
      <span style="left:32%;width:72px;height:72px;animation-duration:24s;animation-delay:1s"></span>
      <span style="left:46%;width:34px;height:34px;animation-duration:16s;animation-delay:6s"></span>
      <span style="left:60%;width:48px;height:48px;animation-duration:21s;animation-delay:2s"></span>
      <span style="left:72%;width:30px;height:30px;animation-duration:15s;animation-delay:5s"></span>
      <span style="left:84%;width:60px;height:60px;animation-duration:23s;animation-delay:0s"></span>
      <span style="left:94%;width:24px;height:24px;animation-duration:13s;animation-delay:4s"></span>
    </div>

    <section class="login-intro">
      <div class="system-mark">Campus Onboarding</div>
      <h1>高校迎新管理系统</h1>
      <p>学生完成核验、缴费、宿舍分配和到校报到；管理员集中处理迎新数据、宿舍、费用、审核与公告。</p>
      <div class="login-stats">
        <span>四步报到</span>
        <span>同专业分配</span>
        <span>男女宿舍分离</span>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-tabs">
        <el-button :type="form.role === 'STUDENT' ? 'primary' : 'default'" @click="switchRole('STUDENT')">学生登录</el-button>
        <el-button :type="form.role === 'ADMIN' ? 'primary' : 'default'" @click="switchRole('ADMIN')">管理员登录</el-button>
      </div>
      <el-form label-position="top" @keyup.enter="submit">
        <el-form-item :label="form.role === 'ADMIN' ? '管理员账号' : '学号'">
          <el-input v-model="form.username" :prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password size="large" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captcha" size="large" placeholder="输入计算结果" />
            <div
              class="captcha-box"
              :title="captchaLoading ? '加载中...' : '点击刷新验证码'"
              v-loading="captchaLoading"
              @click="loadCaptcha"
            >
              {{ captchaQuestion || '加载中...' }}
            </div>
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="login-button" :loading="loading" @click="submit">
          登录系统
        </el-button>
      </el-form>
      <div class="demo-account">
        <span>演示账号：</span>
        <span>学生 20260001 / 123456</span>
        <span>管理员 admin / 123456</span>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 48px;
  align-items: center;
  padding: 48px max(36px, 8vw);
  background:
    linear-gradient(120deg, rgba(37, 111, 115, 0.12), transparent 42%),
    linear-gradient(300deg, rgba(183, 121, 31, 0.16), transparent 38%),
    var(--app-bg);
}

/* 气泡背景：从底部缓慢上浮，不阻挡交互 */
.bubbles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bubbles span {
  position: absolute;
  bottom: -120px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(37, 111, 115, 0.22), rgba(37, 111, 115, 0.06));
  box-shadow: inset 0 0 8px rgba(255, 255, 255, 0.4);
  animation-name: bubble-float;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

@keyframes bubble-float {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 0.85;
  }
  100% {
    transform: translateY(-112vh) scale(1.25);
    opacity: 0;
  }
}

.login-intro {
  position: relative;
  z-index: 1;
}

.login-intro h1 {
  margin: 16px 0;
  font-size: 42px;
  letter-spacing: 0;
}

.login-intro p {
  max-width: 620px;
  margin: 0;
  color: var(--app-muted);
  line-height: 1.8;
}

.system-mark {
  display: inline-block;
  color: var(--app-primary);
  font-weight: 800;
}

.login-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.login-stats span {
  padding: 9px 12px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--app-text);
  font-weight: 600;
}

.login-panel {
  position: relative;
  z-index: 1;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 26px;
  box-shadow: 0 18px 44px rgba(23, 32, 51, 0.08);
}

.login-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.login-tabs .el-button {
  width: 100%;
  margin-left: 0;
}

.captcha-row {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 132px;
  gap: 10px;
}

.captcha-box {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  font-weight: 800;
  white-space: nowrap;
  cursor: pointer;
  user-select: none;
  transition: opacity 0.2s;
}

.captcha-box:hover {
  opacity: 0.8;
}

.login-button {
  width: 100%;
}

.demo-account {
  display: grid;
  gap: 5px;
  margin-top: 16px;
  color: var(--app-muted);
  font-size: 13px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 28px 16px;
  }

  .login-intro h1 {
    font-size: 32px;
  }
}
</style>
