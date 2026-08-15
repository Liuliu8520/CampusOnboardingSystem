<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { studentApi } from '@/api/modules'
import type { StudentProfile } from '@/types'

const loading = ref(false)
const profile = ref<StudentProfile>()

async function loadData() {
  loading.value = true
  try {
    profile.value = await studentApi.profile()
  } finally {
    loading.value = false
  }
}

function statusText(status: string) {
  return status === 'APPROVED' ? '已通过' : status === 'REJECTED' ? '已驳回' : '待审核'
}

function statusType(status: string) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人中心</h2>
      </div>
    </div>

    <section class="panel" v-if="profile">
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="学号">{{ profile.student.studentId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.student.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile.student.gender }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ profile.student.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile.student.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ profile.student.className }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile.student.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile.student.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭地址" :span="2">{{ profile.student.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="缴费状态">
          <el-tag :type="profile.student.paid ? 'success' : 'danger'">{{ profile.student.paid ? '已完成必缴' : '未完成' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报到状态">
          <el-tag :type="profile.student.checkedIn ? 'success' : 'info'">{{ profile.student.checkedIn ? '已报到' : '未报到' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="宿舍" :span="2">{{ profile.dorm?.display || '未分配' }}</el-descriptions-item>
      </el-descriptions>
    </section>

    <section class="panel history-panel">
      <div class="section-title">资格修改记录</div>
      <el-table :data="profile?.modifications || []" border>
        <el-table-column prop="fieldLabel" label="字段" width="120" />
        <el-table-column prop="oldValue" label="原值" />
        <el-table-column prop="newValue" label="新值" />
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column prop="reviewComment" label="审核意见" min-width="160" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped>
.history-panel {
  margin-top: 16px;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 800;
}
</style>
