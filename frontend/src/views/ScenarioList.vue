<template>
  <div class="scenario-list-page">
    <div class="page-header">
      <h2>压测场景</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建场景
      </el-button>
    </div>

    <el-table :data="scenarios" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="场景名称" />
      <el-table-column prop="type" label="场景类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'combination' ? 'primary' : 'success'">
            {{ row.type === 'combination' ? '接口组合' : '参数化' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row.id)">编辑</el-button>
          <el-button type="success" link @click="handleRun(row.id)">运行</el-button>
          <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useScenarioStore } from '@/stores/scenario'

const router = useRouter()
const scenarioStore = useScenarioStore()
const { scenarios, loading } = storeToRefs(scenarioStore)

onMounted(async () => {
  try {
    await scenarioStore.fetchScenarios()
  } catch {
    ElMessage.error('加载场景列表失败')
  }
})

function handleCreate() {
  router.push('/scenarios/new')
}

function handleEdit(id) {
  router.push(`/scenarios/${id}`)
}

function handleRun(id) {
  router.push(`/monitor/new?scenarioId=${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个场景吗？', '确认删除', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await scenarioStore.remove(id)
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style lang="scss" scoped>
.scenario-list-page {
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
