<template>
  <div class="qps-chart" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
  },
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  updateChart()
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

function updateChart() {
  if (!chart) return

  const option = {
    title: {
      text: 'QPS趋势',
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
      name: 'QPS',
    },
    series: [{
      data: props.data,
      type: 'line',
      smooth: true,
      areaStyle: {
        opacity: 0.3,
      },
    }],
  }

  chart.setOption(option)
}

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
})
</script>

<style lang="scss" scoped>
.qps-chart {
  width: 100%;
  height: 300px;
}
</style>
