<template>
  <div class="template-edit-page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑模板' : '新建模板' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>

      <el-form-item label="请求方法" prop="method">
        <el-select v-model="form.method" placeholder="请选择请求方法">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>

      <el-form-item label="请求URL" prop="url">
        <el-input v-model="form.url" placeholder="请输入请求URL" />
      </el-form-item>

      <el-form-item label="请求头">
        <div v-for="(value, key) in form.headers" :key="key" class="header-item">
          <el-input v-model="form.headers[key]" :placeholder="key">
            <template #prepend>{{ key }}</template>
          </el-input>
          <el-button type="danger" @click="removeHeader(key)">删除</el-button>
        </div>
        <el-button @click="addHeader">添加请求头</el-button>
      </el-form-item>

      <el-form-item label="请求体">
        <el-input
          v-model="form.body"
          type="textarea"
          :rows="6"
          placeholder="请输入请求体(JSON格式)"
        />
      </el-form-item>

      <!-- URL 查询参数 -->
      <el-form-item label="查询参数">
        <el-table :data="queryParams" border size="small" class="params-table">
          <el-table-column label="参数名" min-width="160">
            <template #default="{ row }">
              <el-input v-model="row.name" size="small" placeholder="参数名" />
            </template>
          </el-table-column>
          <el-table-column label="参数值" min-width="200">
            <template #default="{ row }">
              <el-input v-model="row.value" size="small" placeholder="参数值" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }">
              <el-button type="danger" size="small" link @click="queryParams.splice($index, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="small" @click="queryParams.push({ name: '', value: '' })" style="margin-top: 8px">
          添加参数
        </el-button>
      </el-form-item>

      <!-- 占位符参数 -->
      <el-form-item label="占位符参数" v-if="Object.keys(placeholderValues).length > 0">
        <div v-for="(val, key) in placeholderValues" :key="key" class="placeholder-item">
          <el-tag type="warning" size="small" class="placeholder-name">{{ '${' + key + '}' }}</el-tag>
          <el-input v-model="placeholderValues[key]" size="small" placeholder="默认值" class="placeholder-input" />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useTemplateStore } from '@/stores/template'

const route = useRoute()
const router = useRouter()
const templateStore = useTemplateStore()

const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const form = ref({
  name: '',
  method: 'GET',
  url: '',
  headers: {},
  body: '',
})

const queryParams = ref([])
const placeholderValues = ref({})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求URL', trigger: 'blur' }],
}

onMounted(async () => {
  if (route.query.method) {
    form.value.method = route.query.method
  }
  if (route.query.url) {
    form.value.url = route.query.url
  }
  if (route.query.headers) {
    try {
      form.value.headers = JSON.parse(route.query.headers)
    } catch {
      form.value.headers = {}
    }
  }
  if (route.query.body) {
    form.value.body = route.query.body
  }
  if (route.query.queryParams) {
    try {
      queryParams.value = JSON.parse(route.query.queryParams)
    } catch {
      queryParams.value = []
    }
  }
  if (route.query.placeholderValues) {
    try {
      placeholderValues.value = JSON.parse(route.query.placeholderValues)
    } catch {
      placeholderValues.value = {}
    }
  }

  if (isEdit.value) {
    await templateStore.fetchTemplate(route.params.id)
    if (templateStore.currentTemplate) {
      Object.assign(form.value, templateStore.currentTemplate)
      if (typeof form.value.headers === 'string') {
        try {
          form.value.headers = JSON.parse(form.value.headers)
        } catch {
          form.value.headers = {}
        }
      }
      // 从 URL 中解析查询参数
      if (form.value.url && form.value.url.includes('?')) {
        const urlObj = new URL(form.value.url)
        queryParams.value = Array.from(urlObj.searchParams.entries())
          .map(([name, value]) => ({ name, value }))
        form.value.url = form.value.url.substring(0, form.value.url.indexOf('?'))
      }
    }
  }
})

function addHeader() {
  const key = prompt('请输入请求头名称:')
  if (key) {
    form.value.headers[key] = ''
  }
}

function removeHeader(key) {
  delete form.value.headers[key]
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true

    // 合并 baseUrl + 查询参数
    const params = queryParams.value.filter(p => p.name)
    let fullUrl = form.value.url
    if (params.length > 0) {
      const qs = params.map(p => `${encodeURIComponent(p.name)}=${encodeURIComponent(p.value)}`).join('&')
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + qs
    }

    const data = {
      ...form.value,
      url: fullUrl,
      headers: JSON.stringify(form.value.headers),
      authConfig: Object.keys(placeholderValues.value).length > 0
        ? JSON.stringify({ placeholderValues: placeholderValues.value, queryParams: queryParams.value })
        : '',
    }

    if (isEdit.value) {
      await templateStore.update(route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await templateStore.create(data)
      ElMessage.success('创建成功')
    }

    router.push('/templates')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/templates')
}
</script>

<style lang="scss" scoped>
.template-edit-page {
  max-width: 800px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 24px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }

  .header-item {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
  }

  .params-table {
    width: 100%;
  }

  .placeholder-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .placeholder-name {
      flex-shrink: 0;
      font-family: monospace;
      font-size: 13px;
      color: var(--el-color-warning);
      min-width: 120px;
    }

    .placeholder-input {
      flex: 1;
      max-width: 300px;
    }
  }
}
</style>
