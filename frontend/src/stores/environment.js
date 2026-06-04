import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getEnvironments,
  createEnvironment as apiCreate,
  updateEnvironment as apiUpdate,
  deleteEnvironment as apiDelete,
  activateEnvironment as apiActivate,
} from '@/api/environment'
import {
  getEnvVariables,
  createEnvVariable as apiCreateVar,
  updateEnvVariable as apiUpdateVar,
  deleteEnvVariable as apiDeleteVar,
  batchCreateEnvVariables as apiBatchCreateVars,
} from '@/api/envVariable'

export const useEnvironmentStore = defineStore('environment', () => {
  const environments = ref([])
  const envVariables = ref([])
  const loading = ref(false)
  const activeEnvironmentId = ref(Number(localStorage.getItem('activeEnvironmentId')) || null)

  const activeEnvironment = computed(() =>
    environments.value.find(e => e.id === activeEnvironmentId.value) || null
  )

  async function fetchEnvironments() {
    loading.value = true
    try {
      const response = await getEnvironments()
      environments.value = response.data
      // 如果有激活的环境但本地存储的 ID 不存在，重置
      if (activeEnvironmentId.value) {
        const exists = environments.value.find(e => e.id === activeEnvironmentId.value)
        if (!exists) {
          activeEnvironmentId.value = null
          localStorage.removeItem('activeEnvironmentId')
        }
      }
    } finally {
      loading.value = false
    }
  }

  async function create(data) {
    const response = await apiCreate(data)
    await fetchEnvironments()
    return response.data
  }

  async function update(id, data) {
    const response = await apiUpdate(id, data)
    await fetchEnvironments()
    return response.data
  }

  async function remove(id) {
    await apiDelete(id)
    if (activeEnvironmentId.value === id) {
      activeEnvironmentId.value = null
      localStorage.removeItem('activeEnvironmentId')
    }
    await fetchEnvironments()
  }

  async function activate(id) {
    await apiActivate(id)
    activeEnvironmentId.value = id
    localStorage.setItem('activeEnvironmentId', String(id))
    await fetchEnvironments()
    await fetchEnvVariables(id)
  }

  async function fetchEnvVariables(environmentId) {
    if (!environmentId) {
      envVariables.value = []
      return
    }
    const response = await getEnvVariables(environmentId)
    envVariables.value = response.data || []
  }

  async function createVariable(data) {
    const response = await apiCreateVar(data)
    if (activeEnvironmentId.value) {
      await fetchEnvVariables(activeEnvironmentId.value)
    }
    return response.data
  }

  async function updateVariable(id, data) {
    const response = await apiUpdateVar(id, data)
    if (activeEnvironmentId.value) {
      await fetchEnvVariables(activeEnvironmentId.value)
    }
    return response.data
  }

  async function removeVariable(id) {
    await apiDeleteVar(id)
    if (activeEnvironmentId.value) {
      await fetchEnvVariables(activeEnvironmentId.value)
    }
  }

  async function batchCreateVariables(variables) {
    if (!activeEnvironmentId.value) return
    const response = await apiBatchCreateVars(activeEnvironmentId.value, variables)
    envVariables.value = response.data || []
    return response.data
  }

  return {
    environments,
    envVariables,
    loading,
    activeEnvironmentId,
    activeEnvironment,
    fetchEnvironments,
    create,
    update,
    remove,
    activate,
    fetchEnvVariables,
    createVariable,
    updateVariable,
    removeVariable,
    batchCreateVariables,
  }
})
