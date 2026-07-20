<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Bell, CreditCard, HomeFilled, LocationFilled, SwitchButton, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const activePath = computed(() => route.path)
const pageTitle = computed(() => String(route.meta.title || '学生端'))

const menus = [
  { path: '/student/home', label: '首页', icon: HomeFilled },
  { path: '/student/qualification', label: '个人信息', icon: User },
  { path: '/student/payment', label: '缴费', icon: CreditCard },
  { path: '/student/report', label: '报到', icon: LocationFilled },
  { path: '/student/announcements', label: '通知', icon: Bell }
]

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="student-shell">
    <el-aside width="236px" class="student-aside">
      <div class="brand">
        <RouterLink to="/student/home" class="student-brand">
          <el-icon><HomeFilled /></el-icon>
          <div>
            <strong>新生服务端</strong>
            <span>Student Panel</span>
          </div>
        </RouterLink>
      </div>
      <el-menu :default-active="activePath" router class="side-menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="student-header">
        <div>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="student-user">
          <span>{{ auth.displayName }}</span>
          <el-tooltip content="退出登录">
            <el-button :icon="SwitchButton" circle @click="logout" />
          </el-tooltip>
        </div>
      </el-header>
      <el-main class="student-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.student-shell {
  min-height: 100vh;
  background: var(--app-bg);
}

.student-aside {
  background: #fff;
  border-right: 1px solid var(--app-border);
}

.brand {
  height: 76px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  border-bottom: 1px solid var(--app-border);
}

.student-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
  font-weight: 800;
}

.student-brand .el-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  background: var(--app-primary);
  flex: 0 0 auto;
}

.student-brand strong,
.student-brand span {
  display: block;
}

.student-brand span {
  margin-top: 2px;
  font-size: 12px;
  color: var(--app-muted);
}

.side-menu {
  border-bottom: none;
  border-right: none;
  padding: 8px;
}

.student-header {
  height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: #fff;
  border-bottom: 1px solid var(--app-border);
}

.student-header h1 {
  margin: 0;
  font-size: 20px;
}

.student-header span {
  font-size: 13px;
  color: var(--app-muted);
}

.student-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-user span {
  color: var(--app-muted);
}

.student-main {
  padding: 0;
}

@media (max-width: 900px) {
  .student-aside {
    width: 76px !important;
  }

  .student-brand div,
  .side-menu span {
    display: none;
  }
}
</style>
