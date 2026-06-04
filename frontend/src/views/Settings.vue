<template>
  <div class="settings-page">
    <h2>设置</h2>

    <!-- 主题设置 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">外观设置</span>
      </template>
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
      </el-form>
    </el-card>

    <!-- 环境管理 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span class="section-title">环境管理</span>
          <el-button type="primary" size="small" @click="showEnvDialog()">
            <el-icon><Plus /></el-icon> 新增环境
          </el-button>
        </div>
      </template>

      <el-table :data="environments" border size="small">
        <el-table-column prop="name" label="环境名称" min-width="120" />
        <el-table-column prop="baseUrl" label="基础URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">
              {{ row.isActive === 1 ? '激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleActivate(row)"
              :disabled="row.isActive === 1">激活</el-button>
            <el-button type="primary" link size="small" @click="showEnvDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteEnv(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 环境变量管理 -->
    <el-card class="section-card" shadow="never" v-if="activeEnvironment">
      <template #header>
        <div class="section-header">
          <span class="section-title">环境变量 - {{ activeEnvironment.name }}</span>
          <el-button type="primary" size="small" @click="showVarDialog()">
            <el-icon><Plus /></el-icon> 新增变量
          </el-button>
        </div>
      </template>

      <el-table :data="envVariables" border size="small" v-if="envVariables.length > 0">
        <el-table-column prop="name" label="变量名" min-width="150">
          <template #default="{ row }">
            <el-tag type="warning" size="small" class="var-name">{{ formatVarName(row.name) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="变量值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showVarDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteVar(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无环境变量" :image-size="60" />
    </el-card>
    <el-card class="section-card" shadow="never" v-else>
      <el-empty description="请先激活一个环境" :image-size="60" />
    </el-card>

    <!-- 环境编辑弹窗 -->
    <el-dialog v-model="envDialogVisible" :title="editingEnv ? '编辑环境' : '新增环境'" width="480px">
      <el-form :model="envForm" label-width="100px">
        <el-form-item label="环境名称" required>
          <el-input v-model="envForm.name" placeholder="如：开发环境、生产环境" />
        </el-form-item>
        <el-form-item label="基础URL">
          <el-input v-model="envForm.baseUrl" placeholder="如：http://api.example.com" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="envForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="envDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEnv">保存</el-button>
      </template>
    </el-dialog>

    <!-- 变量编辑弹窗 -->
    <el-dialog v-model="varDialogVisible" :title="editingVar ? '编辑变量' : '新增变量'" width="480px">
      <el-form :model="varForm" label-width="100px">
        <el-form-item label="变量名" required>
          <el-input v-model="varForm.name" placeholder="如：baseUrl、apiToken" />
        </el-form-item>
        <el-form-item label="变量值" required>
          <el-input v-model="varForm.value" placeholder="变量值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="varForm.description" placeholder="变量说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="varDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveVar">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useEnvironmentStore } from '@/stores/environment'

const appStore = useAppStore()
const envStore = useEnvironmentStore()
const { environments, envVariables, activeEnvironment } = storeToRefs(envStore)

const isDark = ref(appStore.theme === 'dark')

// 环境弹窗
const envDialogVisible = ref(false)
const editingEnv = ref(null)
const envForm = ref({ name: '', baseUrl: '', description: '' })

// 变量弹窗
const varDialogVisible = ref(false)
const editingVar = ref(null)
const varForm = ref({ name: '', value: '', description: '' })

onMounted(async () => {
  await envStore.fetchEnvironments()
  if (envStore.activeEnvironmentId) {
    await envStore.fetchEnvVariables(envStore.activeEnvironmentId)
  }
})

watch(() => envStore.activeEnvironmentId, async (id) => {
  if (id) {
    await envStore.fetchEnvVariables(id)
  }
})

function handleThemeChange(value) {
  appStore.setTheme(value ? 'dark' : 'light')
}

function formatVarName(name) {
  return '${' + name + '}'
}

function showEnvDialog(env = null) {
  editingEnv.value = env
  envForm.value = env
    ? { name: env.name, baseUrl: env.baseUrl || '', description: env.description || '' }
    : { name: '', baseUrl: '', description: '' }
  envDialogVisible.value = true
}

async function handleSaveEnv() {
  if (!envForm.value.name) {
    ElMessage.warning('请输入环境名称')
    return
  }
  try {
    if (editingEnv.value) {
      await envStore.update(editingEnv.value.id, envForm.value)
      ElMessage.success('更新成功')
    } else {
      await envStore.create(envForm.value)
      ElMessage.success('创建成功')
    }
    envDialogVisible.value = false
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDeleteEnv(env) {
  try {
    await ElMessageBox.confirm(`确定删除环境"${env.name}"吗？关联的变量也会被删除。`, '确认删除', { type: 'warning' })
    await envStore.remove(env.id)
    ElMessage.success('删除成功')
  } catch {
    // 取消
  }
}

async function handleActivate(env) {
  await envStore.activate(env.id)
  ElMessage.success(`已切换到"${env.name}"`)
}

function showVarDialog(v = null) {
  editingVar.value = v
  varForm.value = v
    ? { name: v.name, value: v.value || '', description: v.description || '' }
    : { name: '', value: '', description: '' }
  varDialogVisible.value = true
}

async function handleSaveVar() {
  if (!varForm.value.name || !varForm.value.value) {
    ElMessage.warning('请输入变量名和变量值')
    return
  }
  try {
    if (editingVar.value) {
      await envStore.updateVariable(editingVar.value.id, varForm.value)
      ElMessage.success('更新成功')
    } else {
      await envStore.createVariable({
        ...varForm.value,
        environmentId: envStore.activeEnvironmentId,
      })
      ElMessage.success('创建成功')
    }
    varDialogVisible.value = false
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDeleteVar(v) {
  try {
    await ElMessageBox.confirm(`确定删除变量"${v.name}"吗？`, '确认删除', { type: 'warning' })
    await envStore.removeVariable(v.id)
    ElMessage.success('删除成功')
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.settings-page {
  max-width: 960px;
  margin: 0 auto;

  h2 {
    margin-bottom: 24px;
    color: var(--text-primary);
  }
}

.section-card {
  margin-bottom: 20px;
  border-color: var(--border-color);

  :deep(.el-card__header) {
    padding: 12px 20px;
    background-color: var(--bg-secondary);
    border-bottom-color: var(--border-color);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: 600;
  color: var(--text-primary);
}

.var-name {
  font-family: monospace;
  color: var(--el-color-warning);
  font-size: 13px;
}
</style>
