import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getTemplates, getTemplate, createTemplate, updateTemplate, deleteTemplate } from '@/api/template'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref([])
  const currentTemplate = ref(null)
  const loading = ref(false)

  async function fetchTemplates() {
    loading.value = true
    try {
      const response = await getTemplates()
      templates.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchTemplate(id) {
    loading.value = true
    try {
      const response = await getTemplate(id)
      currentTemplate.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function create(data) {
    const response = await createTemplate(data)
    await fetchTemplates()
    return response.data
  }

  async function update(id, data) {
    const response = await updateTemplate(id, data)
    await fetchTemplates()
    return response.data
  }

  async function remove(id) {
    await deleteTemplate(id)
    await fetchTemplates()
  }

  return {
    templates,
    currentTemplate,
    loading,
    fetchTemplates,
    fetchTemplate,
    create,
    update,
    remove,
  }
})
