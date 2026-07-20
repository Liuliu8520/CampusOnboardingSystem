<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/modules'

const loading = ref(false)
const stats = ref<Record<string, any>>({})
const collegeChartRef = ref<HTMLDivElement>()
const paymentChartRef = ref<HTMLDivElement>()
let collegeChart: echarts.ECharts | null = null
let paymentChart: echarts.ECharts | null = null

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

  if (paymentChartRef.value) {
    paymentChart ||= echarts.init(paymentChartRef.value)
    const rows = stats.value.paymentTrend || []
    paymentChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 36, right: 16, top: 26, bottom: 36 },
      xAxis: { type: 'category', data: rows.map((row: any) => String(row.day || '').slice(0, 10)) },
      yAxis: { type: 'value' },
      series: [{
        name: '缴费笔数',
        type: 'line',
        smooth: true,
        symbolSize: 7,
        data: rows.map((row: any) => Number(row.count || 0)),
        lineStyle: { color: '#b7791f', width: 3 },
        itemStyle: { color: '#b7791f' },
        areaStyle: { color: 'rgba(183, 121, 31, 0.14)' }
      }]
    })
  }
}

function resizeCharts() {
  collegeChart?.resize()
  paymentChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  collegeChart?.dispose()
  paymentChart?.dispose()
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
        <div class="panel-title">近 7 日缴费趋势</div>
        <div ref="paymentChartRef" class="chart" />
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
