<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { studentApi } from '@/api/modules'
import { formatDate } from '@/utils/format'
import type { Announcement } from '@/types'

const loading = ref(false)
const list = ref<Announcement[]>([])

async function loadData() {
  loading.value = true
  try {
    list.value = await studentApi.announcements()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page" v-loading="loading">
    <section class="notice-list">
      <RouterLink
        v-for="item in list"
        :key="item.id"
        :to="`/student/announcements/${item.id}`"
        class="notice-item"
      >
        <strong>{{ item.title }}</strong>
        <time>{{ formatDate(item.createTime) }}</time>
      </RouterLink>
      <div v-if="!list.length" class="panel empty-hint">暂无公告</div>
    </section>
  </div>
</template>

<style scoped>
.notice-list {
  display: grid;
  gap: 14px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 16px 18px;
  color: #2c3e50;
  text-decoration: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.notice-item:hover {
  border-color: var(--app-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.notice-item strong {
  font-size: 16px;
  font-weight: 600;
}

.notice-item time {
  color: var(--app-muted);
  font-size: 13px;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .notice-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
