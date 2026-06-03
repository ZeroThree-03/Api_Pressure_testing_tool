<template>
  <div class="report-page">
    <h2>压测报告</h2>
    <el-card v-if="report">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务ID">{{ report.taskId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ report.status }}</el-descriptions-item>
        <el-descriptions-item label="QPS">{{ report.qps }}</el-descriptions-item>
        <el-descriptions-item label="平均响应时间">{{ report.avgResponseTime }}ms</el-descriptions-item>
        <el-descriptions-item label="错误率">{{ report.errorRate }}%</el-descriptions-item>
        <el-descriptions-item label="总请求数">{{ report.totalRequests }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-empty v-else description="暂无报告数据" />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const report = ref(null)
</script>
