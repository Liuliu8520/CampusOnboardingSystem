<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Edit, Plus, RefreshLeft, Tickets, View } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { College, Major, Student, StudentFeeStatus } from '@/types'

const loading = ref(false)
const dialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const saving = ref(false)
const paymentSaving = ref(false)
const total = ref(0)
const list = ref<Student[]>([])
const colleges = ref<College[]>([])
const majors = ref<Major[]>([])
const paymentStudent = ref<Student>()
const detailStudent = ref<Student>()
const detailVisible = ref(false)
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  college: '',
  major: '',
  checkedIn: undefined as boolean | undefined,
  paid: undefined as boolean | undefined,
  verified: undefined as boolean | undefined,
  bedAssigned: undefined as boolean | undefined
})

const form = reactive<Student>({
  studentId: '',
  name: '',
  gender: '男',
  college: '',
  major: '',
  className: '',
  phone: '',
  idCard: '',
  address: '',
  paid: false,
  checkedIn: false
})
const paymentForm = reactive({
  paidFeeItemIds: [] as number[]
})

const queryMajorOptions = computed(() => query.college ? majorsByCollege(query.college) : majors.value)
const formMajorOptions = computed(() => form.college ? majorsByCollege(form.college) : [])

function resetForm() {
  Object.assign(form, {
    id: undefined,
    studentId: '',
    name: '',
    gender: '男',
    college: '',
    major: '',
    className: '',
    phone: '',
    idCard: '',
    address: '',
    paid: false,
    checkedIn: false
  })
}

function collegeIdByName(name: string) {
  return colleges.value.find((item) => item.name === name)?.id
}

function majorsByCollege(collegeName: string) {
  const collegeId = collegeIdByName(collegeName)
  return collegeId ? majors.value.filter((item) => item.collegeId === collegeId) : []
}

async function loadAcademics() {
  const [collegeData, majorData] = await Promise.all([
    adminApi.colleges({ enabled: true }),
    adminApi.majors({ enabled: true })
  ])
  colleges.value = collegeData
  majors.value = majorData
}

async function loadData() {
  loading.value = true
  try {
    const data = await adminApi.students(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Student) {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

function openPayments(row: Student) {
  paymentStudent.value = row
  paymentForm.paidFeeItemIds = row.paymentStatuses?.filter((item) => item.paid).map((item) => item.feeItemId) || []
  paymentDialogVisible.value = true
}

function openDetail(row: Student) {
  detailStudent.value = row
  detailVisible.value = true
}

async function save() {
  if (!form.studentId || !form.name || !form.gender || !form.college || !form.major || !form.className) {
    ElMessage.warning('请填写完整的学生基础信息')
    return
  }
  saving.value = true
  try {
    await adminApi.saveStudent(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function savePayments() {
  if (!paymentStudent.value?.id) {
    return
  }
  paymentSaving.value = true
  try {
    await adminApi.updateStudentPayments(paymentStudent.value.id, paymentForm.paidFeeItemIds)
    ElMessage.success('缴费状态已更新')
    paymentDialogVisible.value = false
    await loadData()
  } finally {
    paymentSaving.value = false
  }
}

async function remove(row: Student) {
  await ElMessageBox.confirm(`确认删除学生 ${row.name}？`, '删除确认', { type: 'warning' })
  await adminApi.deleteStudent(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

async function resetPassword(row: Student) {
  await ElMessageBox.confirm(`确认将 ${row.name} 的密码重置为 123456？`, '重置密码', { type: 'warning' })
  await adminApi.resetPassword(row.id!)
  ElMessage.success('密码已重置为 123456')
}

async function toggleCheckin(row: Student) {
  const turningOff = row.checkedIn
  if (turningOff) {
    await ElMessageBox.confirm(`确认将 ${row.name} 的报到状态改为「未报到」？`, '取消报到', { type: 'warning' })
  }
  await adminApi.toggleCheckin(row.id!)
  ElMessage.success(turningOff ? '已改为未报到' : '已确认报到')
  await loadData()
}

function search() {
  query.page = 1
  loadData()
}

function resetQuery() {
  query.keyword = ''
  query.college = ''
  query.major = ''
  query.checkedIn = undefined
  query.paid = undefined
  query.verified = undefined
  query.bedAssigned = undefined
  query.page = 1
  loadData()
}

function onQueryCollegeChange() {
  query.major = ''
}

function onFormCollegeChange() {
  form.major = ''
}

function paymentTagType(item: StudentFeeStatus) {
  if (item.paid) return 'success'
  return item.required ? 'danger' : 'info'
}

function paymentTooltip(item: StudentFeeStatus) {
  return `${item.name}：￥${formatAmount(item.amount)}，${item.required ? '必缴' : '选缴'}，${item.paid ? '已缴' : '未缴'}`
}

function requiredSummary(row: Student) {
  if (row.requiredFeeTotal === undefined || row.requiredFeePaidCount === undefined) {
    return row.paid ? '必缴已完成' : '必缴未完成'
  }
  return `必缴 ${row.requiredFeePaidCount}/${row.requiredFeeTotal}`
}

function formatAmount(amount?: number) {
  return Number(amount || 0).toFixed(2)
}

onMounted(async () => {
  await Promise.all([loadAcademics(), loadData()])
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">学生管理</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增学生</el-button>
    </div>

    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="学号/姓名/手机号" clearable style="width: 200px" @keyup.enter="search" />
        <el-select v-model="query.college" placeholder="学院" clearable filterable style="width: 150px" @change="onQueryCollegeChange">
          <el-option v-for="item in colleges" :key="item.id" :label="item.name" :value="item.name" />
        </el-select>
        <el-select v-model="query.major" placeholder="专业" clearable filterable style="width: 160px">
          <el-option v-for="item in queryMajorOptions" :key="item.id" :label="item.name" :value="item.name" />
        </el-select>
        <el-select v-model="query.paid" placeholder="缴费" clearable style="width: 110px">
          <el-option label="已缴费" :value="true" />
          <el-option label="未缴费" :value="false" />
        </el-select>
        <el-select v-model="query.verified" placeholder="核验" clearable style="width: 110px">
          <el-option label="已核验" :value="true" />
          <el-option label="未核验" :value="false" />
        </el-select>
        <el-select v-model="query.bedAssigned" placeholder="宿舍" clearable style="width: 110px">
          <el-option label="已分配" :value="true" />
          <el-option label="未分配" :value="false" />
        </el-select>
        <el-select v-model="query.checkedIn" placeholder="报到" clearable style="width: 110px">
          <el-option label="已报到" :value="true" />
          <el-option label="未报到" :value="false" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="college" label="学院" min-width="130" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="className" label="班级" min-width="110" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="必缴汇总" width="110">
          <template #default="{ row }">
            <el-tag :type="row.paid ? 'success' : 'danger'">{{ row.paid ? '已完成' : '未完成' }}</el-tag>
            <div class="required-count">{{ requiredSummary(row) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="缴费项目" min-width="300">
          <template #default="{ row }">
            <div v-if="row.paymentStatuses?.length" class="payment-status-list">
              <el-tooltip v-for="item in row.paymentStatuses" :key="item.feeItemId" :content="paymentTooltip(item)">
                <el-tag class="payment-tag" size="small" :type="paymentTagType(item)" effect="light">
                  {{ item.name }} {{ item.paid ? '已缴' : '未缴' }}
                </el-tag>
              </el-tooltip>
            </div>
            <el-tag v-else :type="row.paid ? 'success' : 'danger'">{{ row.paid ? '已缴' : '未缴' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报到" width="90">
          <template #default="{ row }">
            <el-tag :type="row.checkedIn ? 'success' : 'info'">{{ row.checkedIn ? '已报到' : '未报到' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340">
          <template #default="{ row }">
            <el-tooltip content="学生清单">
              <el-button :icon="View" circle @click="openDetail(row)" />
            </el-tooltip>
            <el-tooltip content="编辑">
              <el-button :icon="Edit" circle @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="缴费状态">
              <el-button :icon="Tickets" circle type="primary" @click="openPayments(row)" />
            </el-tooltip>
            <el-tooltip content="重置密码">
              <el-button :icon="RefreshLeft" circle @click="resetPassword(row)" />
            </el-tooltip>
            <el-tooltip :content="row.checkedIn ? '取消报到' : '确认报到'">
              <el-button
                :icon="Check"
                circle
                :type="row.checkedIn ? 'warning' : 'success'"
                @click="toggleCheckin(row)"
              />
            </el-tooltip>
            <el-tooltip content="删除">
              <el-button :icon="Delete" circle type="danger" @click="remove(row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        class="pager"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="loadData"
      />
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="760px">
      <el-form :model="form" label-position="top" class="form-grid">
        <el-form-item label="学号"><el-input v-model="form.studentId" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" class="wide-input">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="form.college" class="wide-input" filterable @change="onFormCollegeChange">
            <el-option v-for="item in colleges" :key="item.id" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="form.major" class="wide-input" filterable :disabled="!form.college">
            <el-option v-for="item in formMajorOptions" :key="item.id" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="家庭地址" class="wide"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentDialogVisible" :title="`${paymentStudent?.name || ''} 缴费状态`" width="560px">
      <el-checkbox-group v-if="paymentStudent?.paymentStatuses?.length" v-model="paymentForm.paidFeeItemIds" class="payment-check-list">
        <el-checkbox
          v-for="item in paymentStudent.paymentStatuses"
          :key="item.feeItemId"
          :label="item.feeItemId"
          border
          class="payment-check-item"
        >
          <span>{{ item.name }}</span>
          <small>￥{{ formatAmount(item.amount) }} · {{ item.required ? '必缴' : '选缴' }}</small>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-else description="暂无缴费项目" />
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paymentSaving" @click="savePayments">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="`${detailStudent?.name || ''} 学生清单`" width="680px">
      <el-descriptions v-if="detailStudent" :column="2" border>
        <el-descriptions-item label="学号">{{ detailStudent.studentId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailStudent.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailStudent.gender }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ detailStudent.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detailStudent.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detailStudent.className }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailStudent.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailStudent.idCard || '—' }}</el-descriptions-item>
        <el-descriptions-item label="家庭地址" :span="2">{{ detailStudent.address || '—' }}</el-descriptions-item>
        <el-descriptions-item label="必缴汇总">{{ requiredSummary(detailStudent) }}</el-descriptions-item>
        <el-descriptions-item label="宿舍">
          <el-tag :type="detailStudent.bedId ? 'success' : 'info'" size="small">{{ detailStudent.bedId ? '已分配' : '未分配' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报到状态" :span="2">
          <el-tag :type="detailStudent.checkedIn ? 'success' : 'info'">{{ detailStudent.checkedIn ? '已报到' : '未报到' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">缴费明细</div>
      <el-table :data="detailStudent?.paymentStatuses || []" border size="small">
        <el-table-column prop="name" label="项目" min-width="140" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">￥{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ row.required ? '必缴' : '选缴' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paid ? 'success' : (row.required ? 'danger' : 'info')" size="small">{{ row.paid ? '已缴' : '未缴' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.wide-input {
  width: 100%;
}

.required-count {
  margin-top: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.detail-section-title {
  margin: 16px 0 10px;
  font-size: 14px;
  font-weight: 800;
}

.payment-status-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.payment-tag {
  max-width: 150px;
}

.payment-check-list {
  display: grid;
  gap: 10px;
}

.payment-check-item {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 10px 12px;
}

:deep(.payment-check-item .el-checkbox__label) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  line-height: 1.35;
}

.payment-check-item small {
  color: var(--app-muted);
  font-size: 12px;
}
</style>
