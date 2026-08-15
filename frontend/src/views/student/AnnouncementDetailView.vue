<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { studentApi } from '@/api/modules'
import { formatDate } from '@/utils/format'
import type { Announcement } from '@/types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<Announcement>()

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    detail.value = await studentApi.announcement(id)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, loadDetail)
onMounted(loadDetail)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="panel">
      <el-button :icon="ArrowLeft" text @click="router.push('/student/announcements')">返回通知列表</el-button>
      <article v-if="detail" class="detail">
        <h2>{{ detail.title }}</h2>
        <time>{{ formatDate(detail.createTime) }}</time>
        <div class="content">{{ detail.content }}</div>
      </article>
      <div v-else-if="!loading" class="empty-hint">通知不存在</div>
    </section>
  </div>
</template>

<style scoped>
.detail {
  margin-top: 16px;
}

.detail h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.4;
}

.detail time {
  display: inline-block;
  margin-top: 8px;
  color: var(--app-muted);
  font-size: 13px;
}

.content {
  margin-top: 18px;
  color: #3b4658;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
