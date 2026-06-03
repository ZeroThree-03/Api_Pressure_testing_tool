<template>
  <div class="curl-parser-page">
    <h2>Curl解析</h2>
    <p class="description">粘贴curl命令，自动解析生成压测请求</p>

    <CurlInput ref="curlInputRef" @parse="handleParse" />
    <ParsedResult :result="parsedResult" @save="handleSave" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CurlInput from '@/components/CurlParser/CurlInput.vue'
import ParsedResult from '@/components/CurlParser/ParsedResult.vue'
import { parseCurl } from '@/api/curl'

const router = useRouter()
const curlInputRef = ref(null)
const parsedResult = ref(null)

async function handleParse(curl) {
  curlInputRef.value?.setLoading(true)
  try {
    const response = await parseCurl(curl)
    parsedResult.value = response.data
  } catch (error) {
    console.error('Parse error:', error)
  } finally {
    curlInputRef.value?.setLoading(false)
  }
}

function handleSave(result) {
  router.push({
    path: '/templates/new',
    query: {
      method: result.method,
      url: result.url,
      headers: JSON.stringify(result.headers),
      body: result.body || '',
    },
  })
  ElMessage.success('请在模板编辑页面完善信息后保存')
}
</script>

<style lang="scss" scoped>
.curl-parser-page {
  max-width: 800px;
  margin: 0 auto;

  h2 {
    margin-bottom: 8px;
    color: var(--text-primary);
  }

  .description {
    margin-bottom: 24px;
    color: var(--text-secondary);
  }
}
</style>
