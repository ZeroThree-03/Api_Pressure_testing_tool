<template>
  <div class="result-tree">
    <div class="result-header">
      <span>结果树</span>
      <el-radio-group v-model="filter" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="success">成功</el-radio-button>
        <el-radio-button value="failed">失败</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="filteredResults" max-height="400" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
            {{ row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="method" label="方法" width="80" />
      <el-table-column prop="url" label="URL" show-overflow-tooltip />
      <el-table-column prop="statusCode" label="状态码" width="80" />
      <el-table-column prop="responseTime" label="响应时间" width="100">
        <template #default="{ row }">
          {{ row.responseTime }}ms
        </template>
      </el-table-column>
      <el-table-column prop="timestamp" label="时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="请求详情" width="800px">
      <el-descriptions :column="1" border v-if="currentResult">
        <el-descriptions-item label="请求方法">
          {{ currentResult.method }}
        </el-descriptions-item>
        <el-descriptions-item label="请求URL">
          {{ currentResult.url }}
        </el-descriptions-item>
        <el-descriptions-item label="状态码">
          {{ currentResult.statusCode }}
        </el-descriptions-item>
        <el-descriptions-item label="响应时间">
          {{ currentResult.responseTime }}ms
        </el-descriptions-item>
        <el-descriptions-item label="请求体" v-if="currentResult.requestBody">
          <pre>{{ currentResult.requestBody }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应体" v-if="currentResult.responseBody">
          <pre>{{ currentResult.responseBody }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentResult.error">
          <el-tag type="danger">{{ currentResult.error }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  results: {
    type: Array,
    default: () => [],
  },
})

const filter = ref('all')
const detailVisible = ref(false)
const currentResult = ref(null)

const filteredResults = computed(() => {
  if (filter.value === 'all') return props.results
  return props.results.filter(r => r.status === filter.value)
})

function showDetail(row) {
  currentResult.value = row
  detailVisible.value = true
}
</script>

<style lang="scss" scoped>
.result-tree {
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    font-weight: 500;
  }

  pre {
    background-color: var(--bg-secondary);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    max-height: 200px;
  }
}
</style>
