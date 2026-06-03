<template>
  <div class="curl-input">
    <el-input
      v-model="curlText"
      type="textarea"
      :rows="6"
      placeholder="请粘贴curl命令..."
    />
    <div class="actions">
      <el-button type="primary" @click="handleParse" :loading="loading">
        解析
      </el-button>
      <el-button @click="handleClear">清空</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['parse'])

const curlText = ref('')
const loading = ref(false)

function handleParse() {
  if (!curlText.value.trim()) {
    return
  }
  emit('parse', curlText.value)
}

function handleClear() {
  curlText.value = ''
}

function setLoading(value) {
  loading.value = value
}

defineExpose({
  setLoading,
})
</script>

<style lang="scss" scoped>
.curl-input {
  .actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
