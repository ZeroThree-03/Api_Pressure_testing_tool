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
      <!-- 环境选择 -->
      <div class="env-area" v-if="environments.length > 0">
        <el-dropdown @command="handleEnvSwitch" trigger="click">
          <span class="env-switcher">
            <el-icon><Connection /></el-icon>
            <span class="env-name">{{ activeEnvironment ? activeEnvironment.name : '选择环境' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="env in environments"
                :key="env.id"
                :command="env.id"
                :class="{ 'is-active': env.id === activeEnvironmentId }"
              >
                <span>{{ env.name }}</span>
                <el-icon v-if="env.id === activeEnvironmentId" class="check-icon"><Check /></el-icon>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-icon class="env-manage-icon" @click="$router.push('/settings')"><Setting /></el-icon>
      </div>
      <ThemeToggle />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Connection, ArrowDown, Check, Setting } from '@element-plus/icons-vue'
import ThemeToggle from '@/components/Common/ThemeToggle.vue'
import { useEnvironmentStore } from '@/stores/environment'

const route = useRoute()
const router = useRouter()
const currentRoute = computed(() => route)

const envStore = useEnvironmentStore()
const { environments, activeEnvironment, activeEnvironmentId } = storeToRefs(envStore)

onMounted(async () => {
  await envStore.fetchEnvironments()
  if (envStore.activeEnvironmentId) {
    await envStore.fetchEnvVariables(envStore.activeEnvironmentId)
  }
})

async function handleEnvSwitch(envId) {
  await envStore.activate(envId)
  const env = environments.value.find(e => e.id === envId)
  ElMessage.success(`已切换到"${env.name}"`)
}
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

.env-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-primary);
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
  }

  .env-name {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.check-icon {
  margin-left: 8px;
  color: var(--el-color-primary);
}

.env-area {
  display: flex;
  align-items: center;
  gap: 4px;
}

.env-manage-icon {
  cursor: pointer;
  font-size: 15px;
  color: var(--text-secondary);
  transition: color 0.2s;

  &:hover {
    color: var(--el-color-primary);
  }
}
</style>
