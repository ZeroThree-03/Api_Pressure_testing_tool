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
          <span class="url-text">{{ displayUrl }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="请求头" v-if="result.headers && Object.keys(result.headers).length > 0">
          <div v-for="(value, key) in result.headers" :key="key">
            <strong>{{ key }}:</strong> {{ value }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="请求体" v-if="result.body">
          <pre>{{ formatJson(result.body) }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <!-- URL 查询参数（可编辑） -->
      <div class="params-section" v-if="queryParams.length > 0">
        <div class="params-header">
          <h4>URL 查询参数</h4>
          <el-button size="small" type="primary" plain @click="addParam">
            <el-icon><Plus /></el-icon> 添加参数
          </el-button>
        </div>
        <el-table :data="queryParams" border size="small" class="params-table">
          <el-table-column label="参数名" min-width="160">
            <template #default="{ row }">
              <el-input v-model="row.name" size="small" placeholder="参数名" @change="onParamChange" />
            </template>
          </el-table-column>
          <el-table-column label="参数值" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.value" size="small" placeholder="参数值" @change="onParamChange" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="{ row, $index }">
              <el-button type="success" size="small" link @click="saveSingleParam(row)" :disabled="!row.name">
                <el-icon><FolderAdd /></el-icon> 存为变量
              </el-button>
              <el-button type="danger" size="small" link @click="removeParam($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- ${...} 占位符参数 -->
      <div class="params-section" v-if="result.params && result.params.length > 0">
        <h4>占位符参数</h4>
        <div class="placeholder-params">
          <div v-for="param in result.params" :key="param" class="placeholder-item">
            <el-tag type="warning" class="param-tag">
              {{ formatVarName(param) }}
            </el-tag>
            <el-input
              v-model="placeholderValues[param]"
              size="small"
              placeholder="设置默认值（可选）"
              class="placeholder-input"
              @change="onPlaceholderChange"
            />
            <el-button type="success" size="small" link @click="saveSinglePlaceholder(param)">
              <el-icon><FolderAdd /></el-icon> 存为变量
            </el-button>
          </div>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" @click="handleSave">保存为模板</el-button>
        <el-button type="success" @click="handleSaveAllAsEnvVars">
          <el-icon><FolderAdd /></el-icon> 全部保存为环境变量
        </el-button>
        <el-button @click="handleCopy">复制</el-button>
      </div>
    </el-card>

    <!-- 创建环境弹窗 -->
    <el-dialog v-model="envDialogVisible" title="创建环境" width="480px">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        当前没有激活的环境，请先创建一个环境来保存变量。
      </el-alert>
      <el-form :model="envForm" label-width="100px">
        <el-form-item label="环境名称" required>
          <el-input v-model="envForm.name" placeholder="如：开发环境、测试环境" />
        </el-form-item>
        <el-form-item label="基础URL">
          <el-input v-model="envForm.baseUrl" placeholder="如：http://api.example.com" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="envForm.description" type="textarea" :rows="2" placeholder="环境说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="envDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateEnvAndSave">创建并保存变量</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, FolderAdd } from '@element-plus/icons-vue'
import { useEnvironmentStore } from '@/stores/environment'

const props = defineProps({
  result: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['save'])

const envStore = useEnvironmentStore()
const queryParams = ref([])
const placeholderValues = ref({})

// 环境创建弹窗
const envDialogVisible = ref(false)
const envForm = ref({ name: '', baseUrl: '', description: '' })
const pendingSaveAction = ref(null) // 保存待执行的保存操作

const baseUrl = computed(() => {
  if (!props.result?.url) return ''
  const idx = props.result.url.indexOf('?')
  return idx >= 0 ? props.result.url.substring(0, idx) : props.result.url
})

const displayUrl = computed(() => {
  if (queryParams.value.length === 0) return props.result?.url || ''
  const params = queryParams.value
    .filter(p => p.name)
    .map(p => `${p.name}=${p.value}`)
    .join('&')
  return params ? `${baseUrl.value}?${params}` : baseUrl.value
})

// 从 URL 提取 origin（protocol + host）
const urlOrigin = computed(() => {
  if (!props.result?.url) return ''
  try {
    const url = new URL(props.result.url)
    return url.origin
  } catch {
    return ''
  }
})

watch(() => props.result, (newResult) => {
  if (newResult?.queryParams) {
    queryParams.value = newResult.queryParams.map(p => ({ ...p }))
  } else {
    queryParams.value = []
  }
  if (newResult?.params) {
    const vals = {}
    newResult.params.forEach(p => { vals[p] = '' })
    placeholderValues.value = vals
  }
}, { immediate: true })

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

function formatVarName(name) {
  return '${' + name + '}'
}

function addParam() {
  queryParams.value.push({ name: '', value: '' })
}

function removeParam(index) {
  queryParams.value.splice(index, 1)
}

function onParamChange() {}
function onPlaceholderChange() {}

function handleSave() {
  const finalResult = {
    ...props.result,
    url: displayUrl.value,
    queryParams: queryParams.value.filter(p => p.name),
    placeholderValues: { ...placeholderValues.value },
  }
  emit('save', finalResult)
}

async function handleCopy() {
  try {
    const text = JSON.stringify({
      ...props.result,
      url: displayUrl.value,
      queryParams: queryParams.value,
    }, null, 2)
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 显示创建环境弹窗
function showCreateEnvDialog(onSuccess) {
  envForm.value = {
    name: '',
    baseUrl: urlOrigin.value || '',
    description: '',
  }
  pendingSaveAction.value = onSuccess
  envDialogVisible.value = true
}

// 创建环境并执行待保存操作
async function handleCreateEnvAndSave() {
  if (!envForm.value.name) {
    ElMessage.warning('请输入环境名称')
    return
  }
  try {
    const newEnv = await envStore.create(envForm.value)
    await envStore.activate(newEnv.id)
    envDialogVisible.value = false
    ElMessage.success(`环境"${envForm.value.name}"已创建并激活`)

    // 执行待保存的操作
    if (pendingSaveAction.value) {
      await pendingSaveAction.value()
      pendingSaveAction.value = null
    }
  } catch {
    ElMessage.error('创建环境失败')
  }
}

// 确保有激活环境，没有则弹窗创建
async function ensureActiveEnv() {
  if (envStore.activeEnvironmentId) return true
  return new Promise((resolve) => {
    showCreateEnvDialog(() => resolve(true))
    // 弹窗关闭时如果没有创建成功则 reject
    const unwatch = watch(envDialogVisible, (visible) => {
      if (!visible && !envStore.activeEnvironmentId) {
        unwatch()
        resolve(false)
      }
    })
  })
}

// 保存单个查询参数为环境变量
async function saveSingleParam(param) {
  const hasEnv = await ensureActiveEnv()
  if (!hasEnv) return
  try {
    await envStore.createVariable({
      environmentId: envStore.activeEnvironmentId,
      name: param.name,
      value: param.value,
      description: `来自 curl 解析: ${param.name}`,
    })
    ElMessage.success(`已保存变量 ${param.name}`)
  } catch {
    ElMessage.error('保存失败')
  }
}

// 保存单个占位符为环境变量
async function saveSinglePlaceholder(paramName) {
  const hasEnv = await ensureActiveEnv()
  if (!hasEnv) return
  const value = placeholderValues.value[paramName] || ''
  try {
    await envStore.createVariable({
      environmentId: envStore.activeEnvironmentId,
      name: paramName,
      value,
      description: `来自 curl 占位符: ${paramName}`,
    })
    ElMessage.success(`已保存变量 ${paramName}`)
  } catch {
    ElMessage.error('保存失败')
  }
}

// 全部保存为环境变量
async function handleSaveAllAsEnvVars() {
  const hasEnv = await ensureActiveEnv()
  if (!hasEnv) return

  const variables = []

  for (const p of queryParams.value) {
    if (p.name) {
      variables.push({
        name: p.name,
        value: p.value || '',
        description: `来自 curl 查询参数: ${p.name}`,
      })
    }
  }

  for (const [name, value] of Object.entries(placeholderValues.value)) {
    variables.push({
      name,
      value: value || '',
      description: `来自 curl 占位符: ${name}`,
    })
  }

  if (variables.length === 0) {
    ElMessage.warning('没有可保存的参数')
    return
  }

  try {
    await envStore.batchCreateVariables(variables)
    ElMessage.success(`已保存 ${variables.length} 个环境变量`)
  } catch {
    ElMessage.error('保存失败')
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

  .url-text {
    word-break: break-all;
    font-family: monospace;
    font-size: 13px;
  }
}

.params-section {
  margin-top: 16px;

  h4 {
    margin: 0 0 12px 0;
    color: var(--text-primary);
    font-size: 14px;
  }
}

.params-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h4 {
    margin: 0;
  }
}

.params-table {
  width: 100%;
}

.placeholder-params {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.placeholder-item {
  display: flex;
  align-items: center;
  gap: 12px;

  .param-tag {
    flex-shrink: 0;
    font-family: monospace;
  }

  .placeholder-input {
    flex: 1;
    max-width: 300px;
  }
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
