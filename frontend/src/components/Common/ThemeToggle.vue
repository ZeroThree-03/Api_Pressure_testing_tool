<template>
  <el-switch
    v-model="isDark"
    inline-prompt
    :active-icon="Moon"
    :inactive-icon="Sunny"
    @change="handleThemeChange"
  />
</template>

<script setup>
import { ref, watch } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const isDark = ref(appStore.theme === 'dark')

watch(() => appStore.theme, (newTheme) => {
  isDark.value = newTheme === 'dark'
})

function handleThemeChange() {
  appStore.toggleTheme()
}
</script>
