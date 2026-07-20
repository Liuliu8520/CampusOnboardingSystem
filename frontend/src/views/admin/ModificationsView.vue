<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/modules'
import type { QualificationModification } from '@/types'

const loading = ref(false)
const reviewVisible = ref(false)
const total = ref(0)
const list = ref<QualificationModification[]>([])
const current = ref<QualificationModification>()
const query = reactive({
  page: 1,
  size: 10,
  status: 'PENDING'
})
const reviewForm = reactive({
  approved: true,
  comment: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await adminApi.modifications(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openReview(row: QualificationModification, approved: boolean) {
  current.value = row
  reviewForm.approved = approved
  reviewForm.comment = approved ? '审核通过，信息已同步。' : ''
  reviewVisible.value = true
}

async function submitReview() {
  if (!current.value?.id) {
    return
  }
  if (!reviewForm.approved && !reviewForm.comment) {
    ElMessage.warning('驳回时请填写原因')
    return
  }
  await adminApi.reviewModification(current.value.id, reviewForm)
  ElMessage.success(reviewForm.approved ? '已通过申请' : '已驳回申请')
  reviewVisible.value = false
  await loadData()
}

function statusText(status: string) {
  return status === 'APPROVED' ? '已通过' : status === 'REJECTED' ? '已驳回' : '待审核'
}

function statusType(status: string) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
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
        <h2 class="page-title">资格修改审核</h2>
      </div>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <section class="panel">
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="审核状态" clearable style="width: 150px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="fieldLabel" label="字段" width="120" />
        <el-table-column prop="oldValue" label="原值" min-width="150" />
        <el-table-column prop="newValue" label="新值" min-width="150" />
        <el-table-column prop="reason" label="申请原因" min-width="220" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核意见" min-width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="通过">
              <el-button :icon="Check" circle type="success" :disabled="row.status !== 'PENDING'" @click="openReview(row, true)" />
            </el-tooltip>
            <el-tooltip content="驳回">
              <el-button :icon="Close" circle type="danger" :disabled="row.status !== 'PENDING'" @click="openReview(row, false)" />
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

    <el-dialog v-model="reviewVisible" :title="reviewForm.approved ? '通过申请' : '驳回申请'" width="520px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="学号">{{ current.studentId }}</el-descriptions-item>
        <el-descriptions-item label="字段">{{ current.fieldLabel }}</el-descriptions-item>
        <el-descriptions-item label="原值">{{ current.oldValue || '-' }}</el-descriptions-item>
        <el-descriptions-item label="新值">{{ current.newValue }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="reviewForm" label-position="top" class="review-form">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button :type="reviewForm.approved ? 'success' : 'danger'" @click="submitReview">
          {{ reviewForm.approved ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.review-form {
  margin-top: 14px;
}
</style>
