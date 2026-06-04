<template>
  <div class="monitor-view">
    <el-alert
      v-if="taskStatus !== 'running'"
      :type="taskStatus === 'completed' ? 'success' : 'warning'"
      :title="taskStatus === 'completed' ? '测试已完成' : '测试已停止'"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <Dashboard :data="monitorData" />

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <QpsChart :data="qpsHistory" />
      </el-col>
      <el-col :span="12">
        <ResponseTimeChart :data="responseTimeHistory" />
      </el-col>
    </el-row>

    <ResultTree :results="results" class="result-tree-section" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import Dashboard from '@/components/Monitor/Dashboard.vue'
import QpsChart from '@/components/Monitor/QpsChart.vue'
import ResponseTimeChart from '@/components/Monitor/ResponseTimeChart.vue'
import ResultTree from '@/components/Monitor/ResultTree.vue'
import { WebSocketClient } from '@/utils/websocket'

const props = defineProps({
  taskId: {
    type: [String, Number],
    required: true,
  },
})

const monitorData = ref({})
const qpsHistory = ref([])
const responseTimeHistory = ref([])
const results = ref([])
const taskStatus = ref('running')
let wsClient = null

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

watch(() => props.taskId, () => {
  disconnectWebSocket()
  qpsHistory.value = []
  responseTimeHistory.value = []
  results.value = []
  taskStatus.value = 'running'
  connectWebSocket()
})

function connectWebSocket() {
  if (!props.taskId) return

  let wsUrl
  if (window.location.protocol === 'file:') {
    wsUrl = `ws://localhost:8080/ws/monitor/${props.taskId}`
  } else {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    wsUrl = `${protocol}//${window.location.host}/ws/monitor/${props.taskId}`
  }

  wsClient = new WebSocketClient(wsUrl)

  wsClient.on('monitor', (data) => {
    monitorData.value = data
    qpsHistory.value = [...qpsHistory.value, data.qps].slice(-100)
    responseTimeHistory.value = [...responseTimeHistory.value, data.avgResponseTime].slice(-100)
  })

  wsClient.on('result', (data) => {
    const newResults = Array.isArray(data) ? data : [data]
    results.value = [...newResults.reverse(), ...results.value].slice(0, 1000)
  })

  wsClient.on('status', (status) => {
    taskStatus.value = status
  })

  wsClient.on('error', () => {
    ElMessage.error('WebSocket连接错误')
  })

  wsClient.connect()
}

function disconnectWebSocket() {
  if (wsClient) {
    wsClient.close()
    wsClient = null
  }
}
</script>

<style lang="scss" scoped>
.monitor-view {
  .chart-row {
    margin-top: 20px;
  }

  .result-tree-section {
    margin-top: 20px;
  }
}
</style>
