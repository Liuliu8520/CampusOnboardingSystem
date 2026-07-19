<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Bell, DocumentChecked, House, SwitchButton, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const activePath = computed(() => route.path)

const menus = [
  { path: '/student/onboarding', label: '报到流程', icon: DocumentChecked },
  { path: '/student/profile', label: '个人中心', icon: User },
  { path: '/student/announcements', label: '公告通知', icon: Bell }
]

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="student-shell">
    <header class="student-header">
      <RouterLink to="/student/onboarding" class="student-brand">
        <el-icon><House /></el-icon>
        <span>高校迎新管理系统</span>
      </RouterLink>
      <el-menu :default-active="activePath" mode="horizontal" router class="student-menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="student-user">
        <span>{{ auth.displayName }}</span>
        <el-tooltip content="退出登录">
          <el-button :icon="SwitchButton" circle @click="logout" />
        </el-tooltip>
      </div>
    </header>
    <main class="student-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.student-shell {
  min-height: 100vh;
}

.student-header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 68px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 20px;
  padding: 0 22px;
  background: #fff;
  border-bottom: 1px solid var(--app-border);
}

.student-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  white-space: nowrap;
}

.student-brand .el-icon {
  color: var(--app-primary);
  font-size: 22px;
}

.student-menu {
  border-bottom: none;
  min-width: 0;
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
  max-width: 1180px;
  margin: 0 auto;
}

@media (max-width: 800px) {
  .student-header {
    grid-template-columns: 1fr auto;
    height: auto;
    padding: 12px;
  }

  .student-menu {
    grid-column: 1 / -1;
    order: 3;
  }
}
</style>
