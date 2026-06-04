<template>
  <div class="response-time-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})

const chartRef = ref(null)
let chart = null
let resizeObserver = null

onMounted(async () => {
  await nextTick()
  chart = echarts.init(chartRef.value)
  updateChart()
  resizeObserver = new ResizeObserver(() => {
    chart?.resize()
  })
  if (chartRef.value) {
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

function updateChart() {
  if (!chart) return

  const option = {
    title: {
      text: '响应时间趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: props.data.map((_, i) => i),
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)',
    },
    series: [{
      data: props.data,
      type: 'line',
      smooth: true,
    }],
  }

  chart.setOption(option)
}

onUnmounted(() => {
  resizeObserver?.disconnect()
  if (chart) {
    chart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.response-time-chart {
  width: 100%;
  height: 300px;
}
</style>
