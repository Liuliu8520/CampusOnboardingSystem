<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { CreditCard, House, LocationFilled, Refresh, User } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import type { FeeItem, StudentProfile } from '@/types'

const loading = ref(false)
const actionLoading = ref(false)
const profile = ref<StudentProfile>()
const fees = ref<FeeItem[]>([])
const selectedFeeIds = ref<number[]>([])
const applyVisible = ref(false)
const applyFormRef = ref<FormInstance>()
const applyForm = reactive({
  fieldName: 'phone',
  newValue: '',
  reason: ''
})

const fieldOptions = [
  { label: '姓名', value: 'name' },
  { label: '学院', value: 'college' },
  { label: '专业', value: 'major' },
  { label: '班级', value: 'className' },
  { label: '手机号', value: 'phone' },
  { label: '身份证号', value: 'idCard' },
  { label: '家庭地址', value: 'address' }
]

const stepActive = computed(() => {
  const step = profile.value?.currentStep || 1
  return Math.min(step - 1, 3)
})

const hasPendingModification = computed(() => profile.value?.modifications.some((item) => item.status === 'PENDING') || false)
const requiredUnpaid = computed(() => fees.value.filter((item) => item.required && !item.paid))
const selectableFees = computed(() => fees.value.filter((item) => !item.paid))

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

async function submitApply() {
  if (!applyForm.newValue || !applyForm.reason) {
    ElMessage.warning('请填写新值和申请原因')
    return
  }
  actionLoading.value = true
  try {
    await studentApi.applyModification(applyForm)
    ElMessage.success('修改申请已提交')
    applyVisible.value = false
    applyForm.newValue = ''
    applyForm.reason = ''
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

async function pay() {
  if (selectedFeeIds.value.length === 0) {
    ElMessage.warning('请选择需要缴纳的项目')
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
        <h2 class="page-title">报到流程</h2>
        <p class="page-subtitle">当前阶段：{{ profile?.currentStepName || '-' }}</p>
      </div>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <section class="panel flow-panel">
      <el-steps :active="stepActive" finish-status="success" align-center>
        <el-step title="资格核验" />
        <el-step title="缴费" />
        <el-step title="宿舍分配" />
        <el-step title="现场报到" />
      </el-steps>
    </section>

    <section class="flow-grid">
      <div class="panel task-panel">
        <div class="task-title">
          <el-icon><User /></el-icon>
          <span>1. 资格核验</span>
        </div>
        <el-descriptions v-if="profile" :column="2" border>
          <el-descriptions-item label="学号">{{ profile.student.studentId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ profile.student.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.student.gender }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ profile.student.college }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ profile.student.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ profile.student.className }}</el-descriptions-item>
        </el-descriptions>
        <div class="task-actions">
          <el-tag v-if="hasPendingModification" type="warning">存在待审核修改申请</el-tag>
          <el-button type="primary" plain @click="applyVisible = true">申请修改信息</el-button>
        </div>
      </div>

      <div class="panel task-panel">
        <div class="task-title">
          <el-icon><CreditCard /></el-icon>
          <span>2. 在线缴费</span>
        </div>
        <div class="fee-list">
          <el-checkbox-group v-model="selectedFeeIds">
            <div v-for="item in fees" :key="item.id" class="fee-row">
              <el-checkbox :label="item.id" :disabled="item.paid">
                {{ item.name }}
              </el-checkbox>
              <span>￥{{ item.amount }}</span>
              <el-tag size="small" :type="item.required ? 'danger' : 'info'">{{ item.required ? '必缴' : '选缴' }}</el-tag>
              <el-tag v-if="item.paid" size="small" type="success">已缴</el-tag>
            </div>
          </el-checkbox-group>
        </div>
        <div class="task-actions">
          <span v-if="requiredUnpaid.length" class="hint">仍有 {{ requiredUnpaid.length }} 个必缴项目未缴</span>
          <span v-else class="status-dot is-ok">必缴项目已完成</span>
          <el-button type="primary" :loading="actionLoading" :disabled="selectableFees.length === 0 || hasPendingModification" @click="pay">
            模拟支付
          </el-button>
        </div>
      </div>

      <div class="panel task-panel">
        <div class="task-title">
          <el-icon><House /></el-icon>
          <span>3. 宿舍分配</span>
        </div>
        <div v-if="profile?.dorm" class="dorm-result">
          <strong>{{ profile.dorm.display }}</strong>
          <span>{{ profile.dorm.room.major }} / {{ profile.dorm.building.gender }}生宿舍</span>
        </div>
        <div v-else class="empty-hint">缴费完成后可自动分配宿舍</div>
        <div class="task-actions">
          <span class="hint">同专业优先填满现有房间，男女宿舍分离</span>
          <el-button type="primary" :loading="actionLoading" :disabled="!profile?.student.paid || hasPendingModification || !!profile?.student.bedId" @click="assignDorm">
            自动分配
          </el-button>
        </div>
      </div>

      <div class="panel task-panel">
        <div class="task-title">
          <el-icon><LocationFilled /></el-icon>
          <span>4. 现场报到</span>
        </div>
        <div class="checkin-state">
          <el-result
            :icon="profile?.student.checkedIn ? 'success' : 'info'"
            :title="profile?.student.checkedIn ? '已完成报到' : '等待到校确认'"
            :sub-title="profile?.student.checkedIn ? '迎新流程已经闭环完成' : '到达校园后点击确认报到'"
          />
        </div>
        <div class="task-actions">
          <el-button type="success" :loading="actionLoading" :disabled="!profile?.student.bedId || profile?.student.checkedIn" @click="checkin">
            确认到校报到
          </el-button>
        </div>
      </div>
    </section>

    <section class="panel" v-if="profile?.modifications.length">
      <div class="section-title">我的修改申请</div>
      <el-table :data="profile.modifications" border>
        <el-table-column prop="fieldLabel" label="字段" width="120" />
        <el-table-column prop="oldValue" label="原值" />
        <el-table-column prop="newValue" label="新值" />
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="applyVisible" title="申请修改资格信息" width="520px">
      <el-form ref="applyFormRef" :model="applyForm" label-position="top">
        <el-form-item label="修改字段">
          <el-select v-model="applyForm.fieldName" class="wide-input">
            <el-option v-for="item in fieldOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="新值">
          <el-input v-model="applyForm.newValue" />
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="applyForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.flow-panel {
  margin-bottom: 16px;
}

.flow-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.task-panel {
  min-height: 250px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 800;
}

.fee-list {
  display: grid;
  gap: 10px;
}

.fee-row {
  display: grid;
  grid-template-columns: 1fr 90px 52px 52px;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
}

.task-actions {
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

.dorm-result {
  min-height: 128px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 18px;
  border-radius: 8px;
  background: var(--app-primary-soft);
}

.dorm-result strong {
  font-size: 24px;
  color: var(--app-primary);
}

.dorm-result span {
  color: var(--app-muted);
}

.checkin-state :deep(.el-result) {
  padding: 6px 0;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.wide-input {
  width: 100%;
}

@media (max-width: 900px) {
  .flow-grid {
    grid-template-columns: 1fr;
  }

  .fee-row {
    grid-template-columns: 1fr;
  }
}
</style>
