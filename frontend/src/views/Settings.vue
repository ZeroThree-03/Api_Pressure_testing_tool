<template>
  <div class="settings-page">
    <h2>设置</h2>
    <el-card>
      <el-form label-width="120px">
        <el-form-item label="主题模式">
          <el-switch
            v-model="isDark"
            inline-prompt
            active-text="暗"
            inactive-text="亮"
            @change="handleThemeChange"
          />
        </el-form-item>
        <el-form-item label="后端地址">
          <el-input v-model="backendUrl" placeholder="http://localhost:8080" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isDark = ref(appStore.theme === 'dark')
const backendUrl = ref('http://localhost:8080')

function handleThemeChange(value) {
  appStore.setTheme(value ? 'dark' : 'light')
}
</script>
