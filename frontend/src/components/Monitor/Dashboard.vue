<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.qps || 0 }}</div>
          <div class="stat-label">QPS</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.avgResponseTime || 0 }}ms</div>
          <div class="stat-label">平均响应时间</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.errorRate || 0 }}%</div>
          <div class="stat-label">错误率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ data.activeThreads || 0 }}</div>
          <div class="stat-label">活跃线程</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    text-align: center;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: var(--el-color-primary);
    }

    .stat-label {
      margin-top: 8px;
      color: var(--text-secondary);
    }
  }
}
</style>
