<template>
  <div class="scenario-edit-page">
    <h2>{{ isEdit ? '编辑场景' : '新建场景' }}</h2>
    <el-form :model="form" label-width="120px">
      <el-form-item label="场景名称">
        <el-input v-model="form.name" placeholder="请输入场景名称" />
      </el-form-item>
      <el-form-item label="场景类型">
        <el-radio-group v-model="form.type">
          <el-radio value="combination">接口组合</el-radio>
          <el-radio value="parameterized">参数化</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="场景描述">
        <el-input v-model="form.description" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="$router.push('/scenarios')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isEdit = computed(() => route.params.id && route.params.id !== 'new')

const form = ref({
  name: '',
  type: 'combination',
  description: '',
})

function handleSave() {
  // TODO: 实现保存逻辑
}
</script>
