<template>
  <div class="header">
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRoute.meta.title">
          {{ currentRoute.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <ThemeToggle />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ThemeToggle from '@/components/Common/ThemeToggle.vue'

const route = useRoute()
const currentRoute = computed(() => route)
</script>

<style lang="scss" scoped>
.header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
