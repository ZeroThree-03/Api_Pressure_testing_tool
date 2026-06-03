<template>
  <div :class="['app-container', themeClass]">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const themeClass = computed(() => appStore.theme === 'dark' ? 'dark-theme' : 'light-theme')
</script>

<style lang="scss">
.app-container {
  width: 100%;
  min-height: 100vh;
}
</style>
