<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Edit, Plus, RefreshLeft } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { Student } from '@/types'

const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const total = ref(0)
const list = ref<Student[]>([])
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  college: '',
  major: '',
  checkedIn: undefined as boolean | undefined
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

async function adminCheckin(row: Student) {
  await adminApi.adminCheckin(row.id!)
  ElMessage.success('已确认报到')
  await loadData()
}

function search() {
  query.page = 1
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">学生管理</h2>
        <p class="page-subtitle">筛选学生、维护信息、重置密码和确认报到</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增学生</el-button>
    </div>

    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="学号/姓名/手机号" clearable style="width: 220px" @keyup.enter="search" />
        <el-input v-model="query.college" placeholder="学院" clearable style="width: 180px" @keyup.enter="search" />
        <el-input v-model="query.major" placeholder="专业" clearable style="width: 180px" @keyup.enter="search" />
        <el-select v-model="query.checkedIn" placeholder="报到状态" clearable style="width: 150px">
          <el-option label="已报到" :value="true" />
          <el-option label="未报到" :value="false" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="college" label="学院" min-width="130" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="className" label="班级" min-width="110" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="缴费" width="90">
          <template #default="{ row }">
            <el-tag :type="row.paid ? 'success' : 'warning'">{{ row.paid ? '已缴' : '未缴' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报到" width="90">
          <template #default="{ row }">
            <el-tag :type="row.checkedIn ? 'success' : 'info'">{{ row.checkedIn ? '已报到' : '未报到' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑">
              <el-button :icon="Edit" circle @click="openEdit(row)" />
            </el-tooltip>
            <el-tooltip content="重置密码">
              <el-button :icon="RefreshLeft" circle @click="resetPassword(row)" />
            </el-tooltip>
            <el-tooltip content="确认报到">
              <el-button :icon="Check" circle type="success" :disabled="row.checkedIn" @click="adminCheckin(row)" />
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
        <el-form-item label="学院"><el-input v-model="form.college" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="家庭地址" class="wide"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="流程状态">
          <el-checkbox v-model="form.paid">必缴已完成</el-checkbox>
          <el-checkbox v-model="form.checkedIn">已现场报到</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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
</style>
