<template>
  <div class="monitor-page">
    <div class="page-header">
      <h2>实时监控</h2>
      <div class="actions">
        <el-button v-if="!isRunning" type="primary" @click="handleStart" :loading="starting">
          开始测试
        </el-button>
        <el-button v-else type="danger" @click="handleStop">
          停止测试
        </el-button>
      </div>
    </div>

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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Dashboard from '@/components/Monitor/Dashboard.vue'
import QpsChart from '@/components/Monitor/QpsChart.vue'
import ResponseTimeChart from '@/components/Monitor/ResponseTimeChart.vue'
import ResultTree from '@/components/Monitor/ResultTree.vue'
import { WebSocketClient } from '@/utils/websocket'
import { startTask, stopTask, getTaskStatus } from '@/api/task'

const route = useRoute()

const isRunning = ref(false)
const starting = ref(false)
const currentTaskId = ref(route.params.taskId !== 'new' ? route.params.taskId : null)
const monitorData = ref({})
const qpsHistory = ref([])
const responseTimeHistory = ref([])
const results = ref([])
let wsClient = null

onMounted(async () => {
  if (currentTaskId.value) {
    // 检查任务是否仍在运行
    try {
      const res = await getTaskStatus(currentTaskId.value)
      if (res.data) {
        isRunning.value = res.data.status === 'running'
      }
    } catch {
      // ignore
    }
    connectWebSocket(currentTaskId.value)
  }
})

onUnmounted(() => {
  if (wsClient) {
    wsClient.close()
  }
})

function connectWebSocket(taskId) {
  let wsUrl
  if (window.location.protocol === 'file:') {
    // Electron production mode
    wsUrl = `ws://localhost:8080/ws/monitor/${taskId}`
  } else {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    wsUrl = `${protocol}//${window.location.host}/ws/monitor/${taskId}`
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
    if (status === 'completed' || status === 'failed' || status === 'stopped') {
      isRunning.value = false
    }
  })

  wsClient.on('error', () => {
    ElMessage.error('WebSocket连接错误')
  })

  wsClient.on('close', () => {
    if (isRunning.value) {
      ElMessage.warning('WebSocket连接已断开')
    }
  })

  wsClient.connect()
}

async function handleStart() {
  starting.value = true
  try {
    const response = await startTask({
      scenarioId: route.query.scenarioId,
    })
    currentTaskId.value = response.data
    isRunning.value = true
    connectWebSocket(response.data)
    ElMessage.success('测试已开始')
  } catch (error) {
    ElMessage.error('启动测试失败')
  } finally {
    starting.value = false
  }
}

async function handleStop() {
  try {
    await stopTask(currentTaskId.value)
    isRunning.value = false
    ElMessage.success('测试已停止')
  } catch (error) {
    ElMessage.error('停止测试失败')
  }
}
</script>

<style lang="scss" scoped>
.monitor-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }

  .chart-row {
    margin-top: 20px;
  }

  .result-tree-section {
    margin-top: 20px;
  }
}
</style>
