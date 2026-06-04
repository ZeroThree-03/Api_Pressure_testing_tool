<template>
  <div class="scenario-edit-page">
    <h2>{{ isEdit ? '编辑场景' : '新建场景' }}</h2>

    <!-- 基本信息 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">基本信息</span>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleSave"
      >
        <el-form-item label="场景名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="combination">接口组合</el-radio>
            <el-radio value="parameterized">参数化</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="场景描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入场景描述" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 压测配置 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">压测配置</span>
      </template>
      <el-form label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="并发数">
              <el-input-number v-model="config.concurrency" :min="1" :max="10000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="持续时间(秒)">
              <el-input-number v-model="config.duration" :min="1" :max="3600" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预热时间(秒)">
              <el-input-number v-model="config.rampUp" :min="0" :max="600" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="请求间隔(ms)">
              <el-input-number v-model="config.requestDelay" :min="0" :max="60000" :step="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请求次数">
              <div class="loop-config">
                <el-input-number
                  v-model="config.loopCount"
                  :min="1"
                  :max="1000000"
                  :disabled="config.loopForever"
                />
                <el-checkbox v-model="config.loopForever" class="forever-checkbox">永远</el-checkbox>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 接口组合 - 步骤列表 -->
    <el-card class="section-card" shadow="never" v-if="form.type === 'combination'">
      <template #header>
        <div class="section-header">
          <span class="section-title">接口步骤</span>
          <el-button type="primary" size="small" @click="addStep">
            <el-icon><Plus /></el-icon> 添加步骤
          </el-button>
        </div>
      </template>

      <div v-if="steps.length === 0" class="empty-steps">
        <el-empty description="暂无步骤，请点击上方按钮添加" :image-size="80" />
      </div>

      <div v-for="(step, index) in steps" :key="index" class="step-item">
        <div class="step-header">
          <span class="step-order">步骤 {{ index + 1 }}</span>
          <div class="step-actions">
            <el-button size="small" :icon="Top" :disabled="index === 0" @click="moveStep(index, -1)" />
            <el-button size="small" :icon="Bottom" :disabled="index === steps.length - 1" @click="moveStep(index, 1)" />
            <el-button size="small" type="danger" :icon="Delete" @click="removeStep(index)" />
          </div>
        </div>
        <el-form label-width="100px" class="step-form">
          <el-form-item label="选择模板" required>
            <el-select
              v-model="step.templateId"
              placeholder="请选择请求模板"
              filterable
              style="width: 100%"
              @change="(val) => onTemplateChange(step, val)"
            >
              <el-option
                v-for="tpl in templates"
                :key="tpl.id"
                :label="`${tpl.name} (${tpl.method})`"
                :value="tpl.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="步骤名称">
            <el-input v-model="step.name" placeholder="留空则使用模板名称" />
          </el-form-item>
          <el-form-item label="步骤配置">
            <el-input
              v-model="step.config"
              type="textarea"
              :rows="2"
              placeholder='可选JSON配置，如 {"weight": 1}'
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 参数化 - 步骤列表 -->
    <el-card class="section-card" shadow="never" v-if="form.type === 'parameterized'">
      <template #header>
        <div class="section-header">
          <span class="section-title">参数化配置</span>
          <el-button type="primary" size="small" @click="addStep">
            <el-icon><Plus /></el-icon> 添加参数化接口
          </el-button>
        </div>
      </template>

      <div v-if="steps.length === 0" class="empty-steps">
        <el-empty description="暂无参数化接口，请点击上方按钮添加" :image-size="80" />
      </div>

      <div v-for="(step, index) in steps" :key="index" class="step-item">
        <div class="step-header">
          <span class="step-order">接口 {{ index + 1 }}</span>
          <div class="step-actions">
            <el-button size="small" type="danger" :icon="Delete" @click="removeStep(index)" />
          </div>
        </div>
        <el-form label-width="100px" class="step-form">
          <el-form-item label="选择模板" required>
            <el-select
              v-model="step.templateId"
              placeholder="请选择请求模板"
              filterable
              style="width: 100%"
              @change="(val) => onTemplateChange(step, val)"
            >
              <el-option
                v-for="tpl in templates"
                :key="tpl.id"
                :label="`${tpl.name} (${tpl.method})`"
                :value="tpl.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="步骤名称">
            <el-input v-model="step.name" placeholder="留空则使用模板名称" />
          </el-form-item>
          <el-form-item label="参数化规则">
            <el-input
              v-model="step.config"
              type="textarea"
              :rows="3"
              placeholder='JSON格式，如 {"paramName": "userId", "values": [1,2,3], "strategy": "sequential"}'
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button type="primary" @click="handleSave" :loading="submitting" size="large">
        保存场景
      </el-button>
      <el-button @click="$router.push('/scenarios')" size="large">取消</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Top, Bottom } from '@element-plus/icons-vue'
import { useScenarioStore } from '@/stores/scenario'
import { getTemplates } from '@/api/template'

const route = useRoute()
const router = useRouter()
const scenarioStore = useScenarioStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const templates = ref([])

const form = ref({
  name: '',
  type: 'combination',
  description: '',
})

const config = ref({
  concurrency: 10,
  duration: 30,
  rampUp: 0,
  requestDelay: 0,
  loopCount: 1,
  loopForever: false,
})

const steps = ref([])

const rules = {
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场景类型', trigger: 'change' }],
}

onMounted(async () => {
  // 加载模板列表
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } catch {
    ElMessage.error('加载模板列表失败')
  }

  // 编辑模式：加载已有数据
  if (isEdit.value) {
    await scenarioStore.fetchScenario(route.params.id)
    const scenario = scenarioStore.currentScenario
    if (scenario) {
      form.value.name = scenario.name || ''
      form.value.type = scenario.type || 'combination'
      form.value.description = scenario.description || ''
      if (scenario.config) {
        try {
          const parsed = JSON.parse(scenario.config)
          Object.assign(config.value, parsed)
          // 恢复永远循环状态
          if (parsed.loopCount === -1) {
            config.value.loopForever = true
          }
        } catch {
          // ignore
        }
      }
    }
    if (scenarioStore.currentSteps && scenarioStore.currentSteps.length > 0) {
      steps.value = scenarioStore.currentSteps.map(s => ({
        templateId: s.templateId,
        name: s.name || '',
        config: s.config || '',
      }))
    }
  }
})

function addStep() {
  steps.value.push({
    templateId: null,
    name: '',
    config: '',
  })
}

function removeStep(index) {
  steps.value.splice(index, 1)
}

function moveStep(index, direction) {
  const target = index + direction
  const temp = steps.value[index]
  steps.value[index] = steps.value[target]
  steps.value[target] = temp
  // 触发响应式更新
  steps.value = [...steps.value]
}

function onTemplateChange(step, templateId) {
  const tpl = templates.value.find(t => t.id === templateId)
  if (tpl && !step.name) {
    step.name = tpl.name
  }
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 验证步骤
  if (steps.value.length === 0) {
    ElMessage.warning('请至少添加一个步骤')
    return
  }
  const emptyStep = steps.value.find(s => !s.templateId)
  if (emptyStep) {
    ElMessage.warning('请为所有步骤选择模板')
    return
  }

  submitting.value = true
  try {
    // 处理永远循环：loopForever 时 loopCount 设为 -1
    const configData = { ...config.value }
    if (configData.loopForever) {
      configData.loopCount = -1
    }
    delete configData.loopForever

    const data = {
      ...form.value,
      config: JSON.stringify(configData),
      steps: steps.value.map((s, i) => ({
        templateId: s.templateId,
        name: s.name || '',
        stepOrder: i + 1,
        config: s.config || '',
      })),
    }

    if (isEdit.value) {
      await scenarioStore.update(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await scenarioStore.create(data)
      ElMessage.success('创建成功')
    }

    router.push('/scenarios')
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.scenario-edit-page {
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

.section-title {
  font-weight: 600;
  color: var(--text-primary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-item {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: var(--bg-primary);
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.step-order {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.step-actions {
  display: flex;
  gap: 4px;
}

.step-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.empty-steps {
  padding: 20px 0;
}

.loop-config {
  display: flex;
  align-items: center;
  gap: 12px;
}

.forever-checkbox {
  white-space: nowrap;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px 0;
}
</style>
