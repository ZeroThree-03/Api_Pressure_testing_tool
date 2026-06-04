<template>
  <div class="task-list-page">
    <div class="page-header">
      <h2>任务列表</h2>
      <el-button @click="fetchTasks" :loading="loading">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-table :data="tasks" v-loading="loading" border>
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
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'running'"
            type="primary" link size="small"
            @click="goMonitor(row.id)"
          >监控</el-button>
          <el-button
            v-if="row.status === 'running'"
            type="danger" link size="small"
            @click="handleStop(row.id)"
          >停止</el-button>
          <el-button
            type="danger" link size="small"
            @click="handleDelete(row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { stopTask, deleteTask, getTasks } from '@/api/task'

const router = useRouter()
const tasks = ref([])
const loading = ref(false)

onMounted(() => {
  fetchTasks()
})

async function fetchTasks() {
  loading.value = true
  try {
    const response = await getTasks()
    tasks.value = (response.data || []).sort((a, b) => b.id - a.id)
  } catch {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  const types = { running: 'primary', completed: 'success', failed: 'danger', stopped: 'warning' }
  return types[status] || 'info'
}

function getStatusLabel(status) {
  const labels = { running: '运行中', completed: '已完成', failed: '失败', stopped: '已停止' }
  return labels[status] || status
}

function goMonitor(taskId) {
  router.push(`/monitor/${taskId}`)
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
.task-list-page {
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
</style>
