<template>
  <div class="monitor-list-page">
    <div class="page-header">
      <h2>实时监控</h2>
      <el-button @click="fetchTasks" :loading="loading">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <!-- 运行中的任务 -->
    <el-card class="section-card" shadow="never" v-if="runningTasks.length > 0">
      <template #header>
        <span class="section-title">运行中 ({{ runningTasks.length }})</span>
      </template>
      <div class="task-cards">
        <div
          v-for="task in runningTasks"
          :key="task.id"
          class="task-card running"
          @click="goMonitor(task.id)"
        >
          <div class="task-card-header">
            <el-tag type="primary" size="small">运行中</el-tag>
            <span class="task-id">任务 #{{ task.id }}</span>
          </div>
          <div class="task-card-body">
            <div class="task-info">
              <span>开始时间: {{ formatTime(task.startedAt) }}</span>
            </div>
            <div class="task-actions">
              <el-button type="primary" size="small" @click.stop="goMonitor(task.id)">监控</el-button>
              <el-button type="danger" size="small" @click.stop="handleStop(task.id)">停止</el-button>
              <el-button type="danger" size="small" plain @click.stop="handleDelete(task.id)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 已完成/失败的任务 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">历史任务</span>
      </template>
      <el-table :data="finishedTasks" border size="small" v-if="finishedTasks.length > 0">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="180" />
        <el-table-column prop="completedAt" label="完成时间" width="180" />
        <el-table-column prop="resultSummary" label="结果摘要" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goMonitor(row.id)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无历史任务" :image-size="60" />
    </el-card>

    <!-- 实时监控面板（选中的任务） -->
    <el-dialog
      v-model="showMonitor"
      :title="'任务 #' + selectedTaskId + ' 监控'"
      width="90%"
      fullscreen
    >
      <MonitorView :task-id="selectedTaskId" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getTasks, stopTask, deleteTask } from '@/api/task'
import MonitorView from '@/components/Monitor/MonitorView.vue'

const tasks = ref([])
const loading = ref(false)
const showMonitor = ref(false)
const selectedTaskId = ref(null)
let refreshTimer = null

const runningTasks = computed(() => tasks.value.filter(t => t.status === 'running'))
const finishedTasks = computed(() =>
  tasks.value.filter(t => t.status !== 'running').sort((a, b) => b.id - a.id)
)

onMounted(() => {
  fetchTasks()
  // 每 3 秒自动刷新运行中的任务
  refreshTimer = setInterval(() => {
    if (runningTasks.value.length > 0) {
      fetchTasks()
    }
  }, 3000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

async function fetchTasks() {
  loading.value = true
  try {
    const response = await getTasks()
    tasks.value = response.data || []
  } catch {
    // silent
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  const types = { completed: 'success', failed: 'danger', stopped: 'warning' }
  return types[status] || 'info'
}

function getStatusLabel(status) {
  const labels = { completed: '已完成', failed: '失败', stopped: '已停止' }
  return labels[status] || status
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 19)
}

function goMonitor(taskId) {
  selectedTaskId.value = taskId
  showMonitor.value = true
}

async function handleStop(taskId) {
  try {
    await stopTask(taskId)
    ElMessage.success('已停止')
    fetchTasks()
  } catch {
    ElMessage.error('停止失败')
  }
}

async function handleDelete(taskId) {
  try {
    await deleteTask(taskId)
    ElMessage.success('已删除')
    fetchTasks()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style lang="scss" scoped>
.monitor-list-page {
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
}

.section-card {
  margin-bottom: 20px;
  border-color: var(--border-color);

  :deep(.el-card__header) {
    padding: 12px 20px;
    background-color: var(--bg-secondary);
    border-bottom-color: var(--border-color);
  }
}

.section-title {
  font-weight: 600;
  color: var(--text-primary);
}

.task-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &.running {
    border-left: 3px solid var(--el-color-primary);
  }

  .task-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .task-id {
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .task-card-body {
    .task-info {
      font-size: 13px;
      color: var(--text-secondary);
      margin-bottom: 12px;
    }

    .task-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
