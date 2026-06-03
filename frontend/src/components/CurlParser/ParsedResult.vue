<template>
  <div class="parsed-result" v-if="result">
    <el-card>
      <template #header>
        <span>解析结果</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getMethodType(result.method)">
            {{ result.method }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL">
          {{ result.url }}
        </el-descriptions-item>
        <el-descriptions-item label="请求头" v-if="result.headers && Object.keys(result.headers).length > 0">
          <div v-for="(value, key) in result.headers" :key="key">
            <strong>{{ key }}:</strong> {{ value }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="请求体" v-if="result.body">
          <pre>{{ formatJson(result.body) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="可变参数" v-if="result.params && result.params.length > 0">
          <el-tag v-for="param in result.params" :key="param" class="param-tag">
            ${{ '{' + param + '}' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="handleSave">保存为模板</el-button>
        <el-button @click="handleCopy">复制</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  result: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['save'])

function getMethodType(method) {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return types[method] || 'info'
}

function formatJson(str) {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

function handleSave() {
  emit('save', props.result)
}

async function handleCopy() {
  try {
    const text = JSON.stringify(props.result, null, 2)
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style lang="scss" scoped>
.parsed-result {
  margin-top: 20px;

  pre {
    background-color: var(--bg-secondary);
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
  }

  .param-tag {
    margin-right: 8px;
    margin-bottom: 8px;
  }

  .actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
