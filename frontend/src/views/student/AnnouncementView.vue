<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { studentApi } from '@/api/modules'
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
      <article v-for="item in list" :key="item.id" class="notice-item">
        <div>
          <h3>{{ item.title }}</h3>
          <time>{{ item.createTime }}</time>
        </div>
        <p>{{ item.content }}</p>
      </article>
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
  background: #fff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 18px;
}

.notice-item h3 {
  margin: 0;
  font-size: 18px;
}

.notice-item time {
  display: inline-block;
  margin-top: 6px;
  color: var(--app-muted);
  font-size: 13px;
}

.notice-item p {
  margin: 14px 0 0;
  color: #3b4658;
  line-height: 1.8;
}
</style>
