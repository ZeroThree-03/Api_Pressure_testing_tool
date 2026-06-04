import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getScenarios, getScenario, createScenario, updateScenario, deleteScenario } from '@/api/scenario'

export const useScenarioStore = defineStore('scenario', () => {
  const scenarios = ref([])
  const currentScenario = ref(null)
  const currentSteps = ref([])
  const loading = ref(false)

  async function fetchScenarios() {
    loading.value = true
    try {
      const response = await getScenarios()
      scenarios.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchScenario(id) {
    loading.value = true
    try {
      const response = await getScenario(id)
      currentScenario.value = response.data.scenario
      currentSteps.value = response.data.steps || []
    } finally {
      loading.value = false
    }
  }

  async function create(data) {
    const response = await createScenario(data)
    await fetchScenarios()
    return response.data
  }

  async function update(id, data) {
    const response = await updateScenario(id, data)
    await fetchScenarios()
    return response.data
  }

  async function remove(id) {
    await deleteScenario(id)
    await fetchScenarios()
  }

  return {
    scenarios,
    currentScenario,
    currentSteps,
    loading,
    fetchScenarios,
    fetchScenario,
    create,
    update,
    remove,
  }
})
