<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { FeeItem } from '@/types'

const loading = ref(false)
const dialogVisible = ref(false)
const total = ref(0)
const list = ref<FeeItem[]>([])
const query = reactive({ page: 1, size: 10 })
const form = reactive<FeeItem>({
  name: '',
  amount: 0,
  required: true,
  enabled: true,
  description: ''
})

function resetForm() {
  Object.assign(form, { id: undefined, name: '', amount: 0, required: true, enabled: true, description: '' })
}

async function loadData() {
  loading.value = true
  try {
    const data = await adminApi.fees(query)
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

function openEdit(row: FeeItem) {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.name || Number(form.amount) <= 0) {
    ElMessage.warning('请填写项目名称和有效金额')
    return
  }
  await adminApi.saveFee(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadData()
}

async function remove(row: FeeItem) {
  await ElMessageBox.confirm(`确认删除缴费项目 ${row.name}？`, '删除确认', { type: 'warning' })
  await adminApi.deleteFee(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">缴费项目管理</h2>
        <p class="page-subtitle">配置学费、住宿费及选缴项目</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增项目</el-button>
    </div>

    <section class="panel">
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="name" label="项目名称" min-width="160" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="属性" width="100">
          <template #default="{ row }">
            <el-tag :type="row.required ? 'danger' : 'info'">{{ row.required ? '必缴' : '选缴' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button :icon="Edit" circle @click="openEdit(row)" />
            <el-button :icon="Delete" circle type="danger" @click="remove(row)" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑缴费项目' : '新增缴费项目'" width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="项目名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" /></el-form-item>
        <el-form-item label="属性">
          <el-radio-group v-model="form.required">
            <el-radio-button :label="true">必缴</el-radio-button>
            <el-radio-button :label="false">选缴</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
