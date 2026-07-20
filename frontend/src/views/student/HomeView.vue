<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { CreditCard, DocumentChecked, LocationFilled } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import type { Announcement, StudentProfile } from '@/types'

const loading = ref(false)
const profile = ref<StudentProfile>()
const announcements = ref<Announcement[]>([])

const quickLinks = [
  { path: '/student/qualification', label: '个人信息页', desc: '查看详细资料并提交核验修改', icon: DocumentChecked },
  { path: '/student/payment', label: '缴费页', desc: '完成模拟支付并刷新缴费状态', icon: CreditCard },
  { path: '/student/report', label: '报到页', desc: '宿舍分配与现场报到合并处理', icon: LocationFilled }
]

const latestAnnouncements = computed(() => announcements.value.slice(0, 3))

async function loadData() {
  loading.value = true
  try {
    const [profileData, announcementData] = await Promise.all([
      studentApi.profile(),
      studentApi.announcements()
    ])
    profile.value = profileData
    announcements.value = announcementData
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="panel">
      <div class="welcome-block">
        <h3>欢迎 {{ profile?.student.name || '同学' }}</h3>
        <p>{{ profile?.student.studentId || '-' }} | {{ profile?.student.major || '-' }} - {{ profile?.student.className || '-' }}</p>
      </div>
      <div class="status-row">
        <el-tag :type="profile?.student.paid ? 'success' : 'warning'">
          {{ profile?.student.paid ? '必缴已完成' : '必缴待完成' }}
        </el-tag>
        <el-tag :type="profile?.student.checkedIn ? 'success' : 'info'">
          {{ profile?.student.checkedIn ? '已报到' : '未报到' }}
        </el-tag>
        <el-tag v-if="profile?.dorm" type="success">{{ profile.dorm.display }}</el-tag>
      </div>
    </section>

    <section class="panel">
      <div class="section-title">快速跳转</div>
      <div class="quick-grid">
        <RouterLink v-for="item in quickLinks" :key="item.path" :to="item.path" class="quick-card">
          <el-icon><component :is="item.icon" /></el-icon>
          <strong>{{ item.label }}</strong>
          <span>{{ item.desc }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="panel">
      <div class="notice-header">
        <div class="section-title">通知</div>
        <RouterLink to="/student/announcements" class="more-link">查看全部</RouterLink>
      </div>
      <div class="notice-list">
        <article v-for="item in latestAnnouncements" :key="item.id" class="notice-item">
          <div class="notice-meta">
            <strong>{{ item.title }}</strong>
            <time>{{ item.createTime }}</time>
          </div>
          <p>{{ item.content }}</p>
        </article>
        <div v-if="!latestAnnouncements.length" class="empty-hint">暂无通知</div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.section-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.welcome-block h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.3;
}

.welcome-block p {
  margin: 8px 0 0;
  color: var(--app-muted);
  font-size: 15px;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.quick-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 118px;
  padding: 16px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: linear-gradient(180deg, #fff, #f8fbfc);
}

.quick-card .el-icon {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  font-size: 18px;
}

.quick-card strong {
  font-size: 16px;
}

.quick-card span {
  color: var(--app-muted);
  line-height: 1.6;
  font-size: 13px;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.more-link {
  color: var(--app-primary);
  font-size: 13px;
}

.notice-list {
  display: grid;
  gap: 12px;
}

.notice-item {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 14px 16px;
  background: #fff;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.notice-meta strong {
  font-size: 15px;
}

.notice-meta time {
  color: var(--app-muted);
  font-size: 12px;
}

.notice-item p {
  margin: 10px 0 0;
  color: #3c4758;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }

  .notice-meta {
    flex-direction: column;
  }
}
</style>
