<template>
  <div class="sidebar">
    <div class="logo">
      <h2>API压测工具</h2>
    </div>
    <el-menu
      :default-active="currentRoute"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/curl-parser">
        <el-icon><Document /></el-icon>
        <span>Curl解析</span>
      </el-menu-item>
      <el-menu-item index="/templates">
        <el-icon><Files /></el-icon>
        <span>请求模板</span>
      </el-menu-item>
      <el-menu-item index="/scenarios">
        <el-icon><List /></el-icon>
        <span>压测场景</span>
      </el-menu-item>
      <el-menu-item index="/monitor-list">
        <el-icon><Monitor /></el-icon>
        <span>实时监控</span>
      </el-menu-item>
      <el-menu-item index="/tasks">
        <el-icon><DataLine /></el-icon>
        <span>任务列表</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Document, Files, List, Setting, DataLine, Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style lang="scss" scoped>
.sidebar {
  width: 240px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  background-color: var(--bg-primary);
  border-right: 1px solid var(--border-color);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-color);

  h2 {
    font-size: 18px;
    color: var(--text-primary);
  }
}

.sidebar-menu {
  border-right: none;
}
</style>
