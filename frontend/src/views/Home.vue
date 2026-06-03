<template>
  <div class="home-page">
    <h1>欢迎使用API压测工具</h1>
    <p>通过粘贴curl命令，快速生成压测脚本</p>

    <el-row :gutter="20" class="quick-actions">
      <el-col :span="8">
        <el-card class="action-card" @click="$router.push('/curl-parser')">
          <el-icon size="48"><Document /></el-icon>
          <h3>Curl解析</h3>
          <p>粘贴curl命令，自动生成压测请求</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="action-card" @click="$router.push('/templates')">
          <el-icon size="48"><Files /></el-icon>
          <h3>请求模板</h3>
          <p>管理和复用请求模板</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="action-card" @click="$router.push('/scenarios')">
          <el-icon size="48"><List /></el-icon>
          <h3>压测场景</h3>
          <p>创建和管理压测场景</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { Document, Files, List } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1000px;
  margin: 0 auto;
  text-align: center;
  padding-top: 40px;

  h1 {
    margin-bottom: 12px;
    color: var(--text-primary);
  }

  p {
    color: var(--text-secondary);
    margin-bottom: 40px;
  }
}

.quick-actions {
  .action-card {
    cursor: pointer;
    text-align: center;
    padding: 20px;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .el-icon {
      color: var(--el-color-primary);
      margin-bottom: 16px;
    }

    h3 {
      margin-bottom: 8px;
      color: var(--text-primary);
    }

    p {
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}
</style>
