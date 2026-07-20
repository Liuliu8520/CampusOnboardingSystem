<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  role: 'STUDENT' as 'STUDENT' | 'ADMIN',
  username: '20260001',
  password: '123456',
  captcha: '6666'
})

function switchRole(role: 'STUDENT' | 'ADMIN') {
  form.role = role
  form.username = role === 'ADMIN' ? 'admin' : '20260001'
}

async function submit() {
  loading.value = true
  try {
    const result = await auth.login(form)
    ElMessage.success('登录成功')
    router.push(result.role === 'ADMIN' ? '/admin/dashboard' : '/student/home')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
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
            <el-input v-model="form.captcha" size="large" />
            <div class="captcha-box">6666</div>
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
  grid-template-columns: 1fr 104px;
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
