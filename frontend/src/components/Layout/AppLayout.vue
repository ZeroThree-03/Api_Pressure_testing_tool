<template>
  <div class="app-layout">
    <Sidebar />
    <div class="main-content">
      <Header />
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 240px;
}

.page-content {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-secondary);
}
</style>
