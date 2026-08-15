<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/modules'

const loading = ref(false)
const stats = ref<Record<string, any>>({})
const collegeChartRef = ref<HTMLDivElement>()
const collegeCountChartRef = ref<HTMLDivElement>()
const checkinPieChartRef = ref<HTMLDivElement>()
const genderPieChartRef = ref<HTMLDivElement>()
let collegeChart: echarts.ECharts | null = null
let collegeCountChart: echarts.ECharts | null = null
let checkinPieChart: echarts.ECharts | null = null
let genderPieChart: echarts.ECharts | null = null

const metrics = [
  { key: 'totalStudents', label: '总人数' },
  { key: 'checkedInStudents', label: '已报到' },
  { key: 'paidStudents', label: '已缴费' },
  { key: 'assignedStudents', label: '已入住' }
]

async function loadData() {
  loading.value = true
  try {
    stats.value = await adminApi.dashboard()
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  if (collegeChartRef.value) {
    collegeChart ||= echarts.init(collegeChartRef.value)
    const rows = stats.value.collegeCheckin || []
    collegeChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 36, right: 16, top: 26, bottom: 36 },
      xAxis: { type: 'category', data: rows.map((row: any) => row.college) },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [{
        name: '报到率',
        type: 'bar',
        barWidth: 36,
        data: rows.map((row: any) => {
          const total = Number(row.total || 0)
          const checked = Number(row.checked_in || row.checkedIn || 0)
          return total === 0 ? 0 : Math.round((checked * 10000) / total) / 100
        }),
        itemStyle: { color: '#256f73', borderRadius: [4, 4, 0, 0] }
      }]
    })
  }

  if (collegeCountChartRef.value) {
    collegeCountChart ||= echarts.init(collegeCountChartRef.value)
    const rows = stats.value.collegeCheckin || []
    collegeCountChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 16, top: 26, bottom: 48 },
      xAxis: {
        type: 'category',
        data: rows.map((row: any) => row.college || ''),
        axisLabel: { interval: 0, rotate: rows.length > 4 ? 24 : 0 }
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '新生人数',
        type: 'bar',
        barWidth: 30,
        data: rows.map((row: any) => Number(row.total || 0)),
        itemStyle: { color: '#256f73', borderRadius: [4, 4, 0, 0] }
      }]
    })
  }

  if (checkinPieChartRef.value) {
    checkinPieChart ||= echarts.init(checkinPieChartRef.value)
    const total = Number(stats.value.totalStudents || 0)
    const checkedIn = Number(stats.value.checkedInStudents || 0)
    const notCheckedIn = Math.max(total - checkedIn, 0)
    checkinPieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        name: '报到情况',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        label: { formatter: '{b}\n{c}人' },
        data: [
          { value: checkedIn, name: '已报到', itemStyle: { color: '#256f73' } },
          { value: notCheckedIn, name: '未报到', itemStyle: { color: '#c0c4cc' } }
        ]
      }]
    })
  }

  if (genderPieChartRef.value) {
    genderPieChart ||= echarts.init(genderPieChartRef.value)
    const rows: any[] = stats.value.genderDistribution || []
    const colorMap: Record<string, string> = { '男': '#409eff', '女': '#f472b6' }
    const nameMap: Record<string, string> = { '男': '男生', '女': '女生' }
    genderPieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        name: '男女占比',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        label: { formatter: '{b}\n{c}人' },
        data: rows.map((row: any) => ({
          value: Number(row.count || 0),
          name: nameMap[row.gender] || row.gender,
          itemStyle: { color: colorMap[row.gender] || '#256f73' }
        }))
      }]
    })
  }
}

function resizeCharts() {
  collegeChart?.resize()
  collegeCountChart?.resize()
  checkinPieChart?.resize()
  genderPieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  collegeChart?.dispose()
  collegeCountChart?.dispose()
  checkinPieChart?.dispose()
  genderPieChart?.dispose()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据看板</h2>
      </div>
      <el-button type="primary" @click="loadData">刷新数据</el-button>
    </div>

    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.key" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ stats[item.key] ?? 0 }}</div>
      </div>
    </section>

    <section class="chart-grid">
      <div class="panel">
        <div class="panel-title">各学院报到率</div>
        <div ref="collegeChartRef" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-title">报到情况</div>
        <div ref="checkinPieChartRef" class="chart" />
      </div>
    </section>

    <section class="chart-grid">
      <div class="panel">
        <div class="panel-title">各院系新生人数</div>
        <div ref="collegeCountChartRef" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-title">男女占比</div>
        <div ref="genderPieChartRef" class="chart" />
      </div>
    </section>
  </div>
</template>

<style scoped>
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.panel-title {
  margin-bottom: 10px;
  font-weight: 800;
}

@media (max-width: 1000px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
