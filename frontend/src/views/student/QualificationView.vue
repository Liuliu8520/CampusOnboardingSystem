<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import type { StudentProfile } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const confirmLoading = ref(false)
const profile = ref<StudentProfile>()
const applyVisible = ref(false)
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

const qualificationStatus = computed(() => {
  const hasPending = profile.value?.modifications.some((item) => item.status === 'PENDING')
  if (hasPending) return '待审核'
  if (profile.value?.student.verified) return '资料已核验'
  return '资料未核验'
})

async function loadData() {
  loading.value = true
  try {
    profile.value = await studentApi.profile()
  } finally {
    loading.value = false
  }
}

async function submitApply() {
  if (!applyForm.newValue || !applyForm.reason) {
    ElMessage.warning('请填写修改内容和原因')
    return
  }
  submitLoading.value = true
  try {
    await studentApi.applyModification(applyForm)
    ElMessage.success('修改申请已提交')
    applyVisible.value = false
    applyForm.newValue = ''
    applyForm.reason = ''
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm('确认以上信息无误？确认后资格状态将变为"资料已核验"。', '请确认', {
      confirmButtonText: '确认无误',
      cancelButtonText: '再看看',
      type: 'warning'
    })
  } catch {
    return
  }
  confirmLoading.value = true
  try {
    await studentApi.confirmQualification()
    ElMessage.success('已确认，资料核验通过')
    await loadData()
  } finally {
    confirmLoading.value = false
  }
}

function statusText(status: string) {
  return status === 'APPROVED' ? '已通过' : status === 'REJECTED' ? '已驳回' : '待审核'
}

function statusType(status: string) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

function qualificationColor() {
  if (qualificationStatus.value === '资料已核验') return 'success'
  if (qualificationStatus.value === '待审核') return 'warning'
  return 'info'
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-actions">
      <el-button
        type="success"
        :loading="confirmLoading"
        :disabled="qualificationStatus !== '资料未核验'"
        @click="handleConfirm"
      >确认资料无误</el-button>
      <el-button type="primary" :icon="EditPen" @click="applyVisible = true">发起修改申请</el-button>
    </div>

    <section class="panel">
      <div class="section-title">核验信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ profile?.student.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ profile?.student.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile?.student.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ profile?.student.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ profile?.student.major || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ profile?.student.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile?.student.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile?.student.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭地址" :span="2">{{ profile?.student.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资格状态">
          <el-tag :type="qualificationColor()">{{ qualificationStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="宿舍">{{ profile?.dorm?.display || '未分配' }}</el-descriptions-item>
      </el-descriptions>
    </section>

    <section class="panel history-panel">
      <div class="section-title">修改申请记录</div>
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

    <el-dialog v-model="applyVisible" title="资格修改申请" width="520px">
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="修改字段">
          <el-select v-model="applyForm.fieldName" class="wide-input">
            <el-option v-for="item in fieldOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="新值">
          <el-input v-model="applyForm.newValue" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="applyForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
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

.history-panel {
  margin-top: 16px;
}

.wide-input {
  width: 100%;
}
</style>
