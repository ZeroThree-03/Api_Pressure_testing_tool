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
    templates.value.unshift(response.data)
    return response.data
  }

  async function update(id, data) {
    const response = await updateTemplate(id, data)
    const index = templates.value.findIndex(t => t.id === id)
    if (index !== -1) {
      templates.value[index] = response.data
    }
    return response.data
  }

  async function remove(id) {
    await deleteTemplate(id)
    templates.value = templates.value.filter(t => t.id !== id)
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
