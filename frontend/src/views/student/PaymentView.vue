<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { CreditCard } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import type { FeeItem, StudentProfile } from '@/types'

const loading = ref(false)
const actionLoading = ref(false)
const profile = ref<StudentProfile>()
const fees = ref<FeeItem[]>([])
const selectedFeeIds = ref<number[]>([])

const requiredUnpaidCount = computed(() => fees.value.filter((item) => item.required && !item.paid).length)

function formatAmount(amount?: number) {
  return Number(amount || 0).toFixed(2)
}

async function loadData() {
  loading.value = true
  try {
    const [profileData, feeData] = await Promise.all([
      studentApi.profile(),
      studentApi.paymentItems()
    ])
    profile.value = profileData
    fees.value = feeData
    selectedFeeIds.value = feeData.filter((item) => item.required && !item.paid && item.id).map((item) => item.id!)
  } finally {
    loading.value = false
  }
}

async function pay() {
  if (!selectedFeeIds.value.length) {
    ElMessage.warning('请先选择缴费项目')
    return
  }
  actionLoading.value = true
  try {
    await studentApi.pay(selectedFeeIds.value)
    ElMessage.success('模拟支付成功')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-actions">
      <el-button type="primary" :icon="CreditCard" :loading="actionLoading" @click="pay">模拟支付</el-button>
    </div>

    <section class="panel">
      <div class="section-title">缴费概览</div>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="姓名">{{ profile?.student.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ profile?.student.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile?.student.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ profile?.student.className || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="status-row">
        <el-tag :type="profile?.student.paid ? 'success' : 'warning'">
          {{ profile?.student.paid ? '必缴已完成' : `还有 ${requiredUnpaidCount} 项必缴未完成` }}
        </el-tag>
      </div>
    </section>

    <section class="panel fee-panel">
      <div class="section-title">缴费项目</div>
      <el-checkbox-group v-model="selectedFeeIds">
        <div v-for="item in fees" :key="item.id" class="fee-row">
          <div class="fee-info">
            <el-checkbox :label="item.id" :disabled="item.paid">
              {{ item.name }}
            </el-checkbox>
            <span v-if="item.description">{{ item.description }}</span>
          </div>
          <strong class="fee-amount">￥{{ formatAmount(item.amount) }}</strong>
          <el-tag size="small" :type="item.required ? 'danger' : 'info'">{{ item.required ? '必缴' : '选缴' }}</el-tag>
          <el-tag v-if="item.paid" size="small" type="success">已缴</el-tag>
        </div>
      </el-checkbox-group>
    </section>
  </div>
</template>

<style scoped>
.section-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.page-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.status-row {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.fee-panel {
  margin-top: 16px;
}

.fee-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 52px 52px;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  margin-bottom: 10px;
}

.fee-info {
  min-width: 0;
}

.fee-info span {
  display: block;
  margin-left: 24px;
  color: var(--app-muted);
  font-size: 12px;
}

.fee-amount {
  color: var(--app-text);
  font-size: 16px;
  text-align: right;
}

@media (max-width: 900px) {
  .fee-row {
    grid-template-columns: 1fr;
  }
}
</style>
