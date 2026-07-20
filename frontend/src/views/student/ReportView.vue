<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationFilled } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import type { StudentProfile } from '@/types'

const loading = ref(false)
const actionLoading = ref(false)
const profile = ref<StudentProfile>()

const canAssignDorm = computed(() => Boolean(profile.value?.student.paid) && !profile.value?.student.bedId)

async function loadData() {
  loading.value = true
  try {
    profile.value = await studentApi.profile()
  } finally {
    loading.value = false
  }
}

async function assignDorm() {
  actionLoading.value = true
  try {
    await studentApi.assignDorm()
    ElMessage.success('宿舍分配成功')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

async function checkin() {
  actionLoading.value = true
  try {
    await studentApi.checkin()
    ElMessage.success('现场报到已完成')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="panel">
      <div class="section-title">报到状态</div>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="姓名">{{ profile?.student.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ profile?.student.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile?.student.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ profile?.student.className || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="status-row">
        <el-tag :type="profile?.student.paid ? 'success' : 'warning'">
          {{ profile?.student.paid ? '已完成缴费' : '请先完成缴费' }}
        </el-tag>
        <el-tag :type="profile?.student.bedId ? 'success' : 'info'">
          {{ profile?.student.bedId ? '已分配宿舍' : '未分配宿舍' }}
        </el-tag>
        <el-tag :type="profile?.student.checkedIn ? 'success' : 'info'">
          {{ profile?.student.checkedIn ? '已现场报到' : '未现场报到' }}
        </el-tag>
      </div>
    </section>

    <section class="panel workflow-panel">
      <el-steps :active="profile?.student.checkedIn ? 2 : profile?.student.bedId ? 1 : 0" finish-status="success" align-center>
        <el-step title="宿舍分配" />
        <el-step title="现场报到" />
      </el-steps>
      <div class="workflow-grid">
        <div class="workflow-card">
          <div class="section-title">宿舍分配</div>
          <div v-if="profile?.dorm" class="dorm-box">
            <strong>{{ profile.dorm.display }}</strong>
            <span>{{ profile.dorm.room.major }} / {{ profile.dorm.building.gender }}生宿舍</span>
          </div>
          <div v-else class="empty-hint">未分配宿舍</div>
          <div class="action-row">
            <span class="hint">系统会先找同专业空房，没有再在第一个可用楼栋建房</span>
            <el-button type="primary" :icon="LocationFilled" :loading="actionLoading" :disabled="!canAssignDorm || !!profile?.student.bedId" @click="assignDorm">
              自动分配
            </el-button>
          </div>
        </div>

        <div class="workflow-card">
          <div class="section-title">确认报到</div>
          <div class="checkin-box">
            <el-result
              :icon="profile?.student.checkedIn ? 'success' : 'info'"
              :title="profile?.student.checkedIn ? '已完成现场报到' : '等待点击确认报到'"
              :sub-title="profile?.student.checkedIn ? '迎新流程已经闭环完成' : '宿舍分配完成后即可办理'"
            />
          </div>
          <div class="action-row">
            <span class="hint">完成后学生流程正式结束</span>
            <el-button type="success" :loading="actionLoading" :disabled="!profile?.student.bedId || profile?.student.checkedIn" @click="checkin">
              确认到校报到
            </el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.section-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.workflow-panel {
  margin-top: 16px;
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.workflow-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 260px;
  padding: 16px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: #fff;
}

.dorm-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 126px;
  padding: 18px;
  border-radius: 8px;
  background: var(--app-primary-soft);
}

.dorm-box strong {
  font-size: 24px;
  color: var(--app-primary);
}

.dorm-box span {
  color: var(--app-muted);
}

.checkin-box :deep(.el-result) {
  padding: 0;
}

.action-row {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.hint {
  color: var(--app-muted);
  font-size: 13px;
}

@media (max-width: 900px) {
  .workflow-grid {
    grid-template-columns: 1fr;
  }

  .action-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
