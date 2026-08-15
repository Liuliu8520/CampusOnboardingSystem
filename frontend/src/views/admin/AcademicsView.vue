<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { College, Major, SchoolClass } from '@/types'

const loading = ref(false)
const activeTab = ref<'college' | 'major' | 'class'>('college')

const colleges = ref<College[]>([])
const majors = ref<Major[]>([])
const classes = ref<SchoolClass[]>([])

// 学院弹窗
const collegeDialogVisible = ref(false)
const collegeSaving = ref(false)
const collegeForm = reactive<College>({ name: '', enabled: true })

// 专业弹窗
const majorDialogVisible = ref(false)
const majorSaving = ref(false)
const majorForm = reactive<Major>({ collegeId: 0, name: '', enabled: true })

// 班级弹窗
const classDialogVisible = ref(false)
const classSaving = ref(false)
const classForm = reactive<SchoolClass & { collegeId?: number }>({ collegeId: 0, majorId: 0, name: '', enabled: true })

const classMajorOptions = computed(() =>
  classForm.collegeId ? majors.value.filter((item) => item.collegeId === classForm.collegeId) : majors.value
)

function collegeNameById(id?: number) {
  if (!id) return '-'
  return colleges.value.find((item) => item.id === id)?.name || '-'
}

function majorNameById(id?: number) {
  if (!id) return '-'
  return majors.value.find((item) => item.id === id)?.name || '-'
}

function collegeIdByMajor(majorId?: number) {
  if (!majorId) return undefined
  return majors.value.find((item) => item.id === majorId)?.collegeId
}

async function loadData() {
  loading.value = true
  try {
    const [collegeData, majorData, classData] = await Promise.all([
      adminApi.colleges(),
      adminApi.majors(),
      adminApi.classes()
    ])
    colleges.value = collegeData
    majors.value = majorData
    classes.value = classData
  } finally {
    loading.value = false
  }
}

// ===== 学院 =====
function openCreateCollege() {
  Object.assign(collegeForm, { id: undefined, name: '', enabled: true })
  collegeDialogVisible.value = true
}

function openEditCollege(row: College) {
  Object.assign(collegeForm, row)
  collegeDialogVisible.value = true
}

async function saveCollege() {
  if (!collegeForm.name.trim()) {
    ElMessage.warning('请填写学院名称')
    return
  }
  collegeSaving.value = true
  try {
    await adminApi.saveCollege({ ...collegeForm })
    ElMessage.success('保存成功')
    collegeDialogVisible.value = false
    await loadData()
  } finally {
    collegeSaving.value = false
  }
}

async function deleteCollege(row: College) {
  await ElMessageBox.confirm(`确认删除学院「${row.name}」？`, '删除确认', { type: 'warning' })
  await adminApi.deleteCollege(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

// ===== 专业 =====
function openCreateMajor() {
  Object.assign(majorForm, { id: undefined, collegeId: colleges.value[0]?.id || 0, name: '', enabled: true })
  majorDialogVisible.value = true
}

function openEditMajor(row: Major) {
  Object.assign(majorForm, row)
  majorDialogVisible.value = true
}

async function saveMajor() {
  if (!majorForm.collegeId) {
    ElMessage.warning('请选择所属学院')
    return
  }
  if (!majorForm.name.trim()) {
    ElMessage.warning('请填写专业名称')
    return
  }
  majorSaving.value = true
  try {
    await adminApi.saveMajor({ ...majorForm })
    ElMessage.success('保存成功')
    majorDialogVisible.value = false
    await loadData()
  } finally {
    majorSaving.value = false
  }
}

async function deleteMajor(row: Major) {
  await ElMessageBox.confirm(`确认删除专业「${row.name}」？`, '删除确认', { type: 'warning' })
  await adminApi.deleteMajor(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

// ===== 班级 =====
function openCreateClass() {
  const firstMajor = majors.value[0]
  Object.assign(classForm, {
    id: undefined,
    collegeId: firstMajor?.collegeId || 0,
    majorId: firstMajor?.id || 0,
    name: '',
    enabled: true
  })
  classDialogVisible.value = true
}

function openEditClass(row: SchoolClass) {
  Object.assign(classForm, { ...row, collegeId: collegeIdByMajor(row.majorId) || 0 })
  classDialogVisible.value = true
}

function onClassCollegeChange() {
  classForm.majorId = 0
}

async function saveClass() {
  if (!classForm.majorId) {
    ElMessage.warning('请选择所属专业')
    return
  }
  if (!classForm.name.trim()) {
    ElMessage.warning('请填写班级名称')
    return
  }
  classSaving.value = true
  try {
    await adminApi.saveClass({ id: classForm.id, majorId: classForm.majorId, name: classForm.name, sortNo: classForm.sortNo, enabled: classForm.enabled })
    ElMessage.success('保存成功')
    classDialogVisible.value = false
    await loadData()
  } finally {
    classSaving.value = false
  }
}

async function deleteClass(row: SchoolClass) {
  await ElMessageBox.confirm(`确认删除班级「${row.name}」？`, '删除确认', { type: 'warning' })
  await adminApi.deleteClass(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="panel">
      <el-menu :default-active="activeTab" mode="horizontal" @select="(key: any) => (activeTab = key)" class="sub-nav">
        <el-menu-item index="college">学院管理</el-menu-item>
        <el-menu-item index="major">专业</el-menu-item>
        <el-menu-item index="class">班级</el-menu-item>
      </el-menu>

      <div v-show="activeTab === 'college'">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" @click="openCreateCollege">新增学院</el-button>
          </div>
          <el-table :data="colleges" border>
            <el-table-column prop="name" label="学院名称" min-width="180" />
            <el-table-column prop="sortNo" label="排序" width="100" />
            <el-table-column label="启用" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button :icon="Edit" circle @click="openEditCollege(row)" />
                <el-button :icon="Delete" circle type="danger" @click="deleteCollege(row)" />
              </template>
            </el-table-column>
          </el-table>
      </div>

      <div v-show="activeTab === 'major'">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" :disabled="!colleges.length" @click="openCreateMajor">新增专业</el-button>
          </div>
          <el-table :data="majors" border>
            <el-table-column label="所属学院" min-width="160">
              <template #default="{ row }">{{ collegeNameById(row.collegeId) }}</template>
            </el-table-column>
            <el-table-column prop="name" label="专业名称" min-width="180" />
            <el-table-column prop="sortNo" label="排序" width="100" />
            <el-table-column label="启用" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button :icon="Edit" circle @click="openEditMajor(row)" />
                <el-button :icon="Delete" circle type="danger" @click="deleteMajor(row)" />
              </template>
            </el-table-column>
          </el-table>
      </div>

      <div v-show="activeTab === 'class'">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" :disabled="!majors.length" @click="openCreateClass">新增班级</el-button>
          </div>
          <el-table :data="classes" border>
            <el-table-column label="所属学院" min-width="150">
              <template #default="{ row }">{{ collegeNameById(collegeIdByMajor(row.majorId)) }}</template>
            </el-table-column>
            <el-table-column label="所属专业" min-width="160">
              <template #default="{ row }">{{ majorNameById(row.majorId) }}</template>
            </el-table-column>
            <el-table-column prop="name" label="班级名称" min-width="160" />
            <el-table-column prop="sortNo" label="排序" width="100" />
            <el-table-column label="启用" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button :icon="Edit" circle @click="openEditClass(row)" />
                <el-button :icon="Delete" circle type="danger" @click="deleteClass(row)" />
              </template>
            </el-table-column>
          </el-table>
      </div>
    </section>

    <!-- 学院弹窗 -->
    <el-dialog v-model="collegeDialogVisible" :title="collegeForm.id ? '编辑学院' : '新增学院'" width="440px">
      <el-form :model="collegeForm" label-position="top">
        <el-form-item label="学院名称"><el-input v-model="collegeForm.name" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="collegeForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collegeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="collegeSaving" @click="saveCollege">保存</el-button>
      </template>
    </el-dialog>

    <!-- 专业弹窗 -->
    <el-dialog v-model="majorDialogVisible" :title="majorForm.id ? '编辑专业' : '新增专业'" width="440px">
      <el-form :model="majorForm" label-position="top">
        <el-form-item label="所属学院">
          <el-select v-model="majorForm.collegeId" class="wide-input">
            <el-option v-for="item in colleges" :key="item.id" :label="item.name" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称"><el-input v-model="majorForm.name" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="majorForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="majorSaving" @click="saveMajor">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班级弹窗 -->
    <el-dialog v-model="classDialogVisible" :title="classForm.id ? '编辑班级' : '新增班级'" width="460px">
      <el-form :model="classForm" label-position="top">
        <el-form-item label="所属学院">
          <el-select v-model="classForm.collegeId" class="wide-input" @change="onClassCollegeChange">
            <el-option v-for="item in colleges" :key="item.id" :label="item.name" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select v-model="classForm.majorId" class="wide-input">
            <el-option v-for="item in classMajorOptions" :key="item.id" :label="item.name" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称"><el-input v-model="classForm.name" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="classForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="classSaving" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.sub-nav {
  border-bottom: 1px solid var(--app-border);
  margin-bottom: 16px;
}

.tab-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.wide-input {
  width: 100%;
}
</style>
