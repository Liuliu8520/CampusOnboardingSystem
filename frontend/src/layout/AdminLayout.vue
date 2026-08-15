<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Bell,
  CreditCard,
  HomeFilled,
  House,
  Finished,
  Lock,
  PieChart,
  School,
  SwitchButton,
  User
} from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/ChangePasswordDialog.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activePath = computed(() => route.path)
const pageTitle = computed(() => String(route.meta.title || '管理后台'))
const passwordVisible = ref(false)

const menus = [
  { path: '/admin/dashboard', label: '数据看板', icon: PieChart },
  { path: '/admin/academics', label: '学院管理', icon: School },
  { path: '/admin/students', label: '学生管理', icon: User },
  { path: '/admin/dorms', label: '宿舍管理', icon: House },
  { path: '/admin/fees', label: '缴费项目', icon: CreditCard },
  { path: '/admin/modifications', label: '资格修改审核', icon: Finished },
  { path: '/admin/announcements', label: '公告管理', icon: Bell }
]

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="admin-aside">
      <div class="brand">
        <el-icon><HomeFilled /></el-icon>
        <div>
          <strong>新生管理系统</strong>
          <span>Admin Console</span>
        </div>
      </div>
      <el-menu :default-active="activePath" router class="side-menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="header-actions">
          <span>{{ auth.displayName }}</span>
          <el-tooltip content="修改密码">
            <el-button :icon="Lock" circle @click="passwordVisible = true" />
          </el-tooltip>
          <el-tooltip content="退出登录">
            <el-button :icon="SwitchButton" circle @click="logout" />
          </el-tooltip>
        </div>
      </el-header>
      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
    <ChangePasswordDialog v-model="passwordVisible" />
  </el-container>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
}

.admin-aside {
  background: #fff;
  border-right: 1px solid var(--app-border);
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid var(--app-border);
}

.brand .el-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: #fff;
  background: var(--app-primary);
}

.brand strong,
.brand span {
  display: block;
}

.brand span {
  margin-top: 2px;
  font-size: 12px;
  color: var(--app-muted);
}

.side-menu {
  border-right: none;
  padding: 8px;
}

.admin-header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid var(--app-border);
}

.admin-header h1 {
  margin: 0;
  font-size: 20px;
}

.admin-header span {
  font-size: 13px;
  color: var(--app-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-main {
  padding: 0;
}

@media (max-width: 900px) {
  .admin-aside {
    width: 72px !important;
  }

  .brand div,
  .side-menu span {
    display: none;
  }

  .brand {
    justify-content: center;
    padding: 0;
  }
}
</style>
