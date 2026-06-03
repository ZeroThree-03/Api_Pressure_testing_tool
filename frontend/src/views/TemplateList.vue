<template>
  <div class="template-list-page">
    <div class="page-header">
      <h2>请求模板</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建模板
      </el-button>
    </div>

    <el-table :data="templates" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="模板名称" />
      <el-table-column prop="method" label="请求方法" width="100">
        <template #default="{ row }">
          <el-tag :type="getMethodType(row.method)">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="请求URL" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row.id)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useTemplateStore } from '@/stores/template'

const router = useRouter()
const templateStore = useTemplateStore()
const { templates, loading } = templateStore

onMounted(() => {
  templateStore.fetchTemplates()
})

function getMethodType(method) {
  const types = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
  }
  return types[method] || 'info'
}

function handleCreate() {
  router.push('/templates/new')
}

function handleEdit(id) {
  router.push(`/templates/${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '确认删除', {
      type: 'warning',
    })
    await templateStore.remove(id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.template-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: var(--text-primary);
    }
  }
}
</style>
