<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { Announcement } from '@/types'

const loading = ref(false)
const dialogVisible = ref(false)
const total = ref(0)
const list = ref<Announcement[]>([])
const query = reactive({ page: 1, size: 10 })
const form = reactive<Announcement>({
  title: '',
  content: '',
  published: true
})

function resetForm() {
  Object.assign(form, { id: undefined, title: '', content: '', published: true })
}

async function loadData() {
  loading.value = true
  try {
    const data = await adminApi.announcements(query)
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

function openEdit(row: Announcement) {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  await adminApi.saveAnnouncement(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadData()
}

async function remove(row: Announcement) {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '删除公告', { type: 'warning' })
  await adminApi.deleteAnnouncement(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">公告管理</h2>
        <p class="page-subtitle">发布和维护迎新通知</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
    </div>

    <section class="panel">
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.published ? 'success' : 'info'">{{ row.published ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="640px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.published" active-text="发布" inactive-text="草稿" />
        </el-form-item>
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
