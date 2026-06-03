import request from '@/utils/request'

export function getScenarios() {
  return request({
    url: '/scenarios',
    method: 'get',
  })
}

export function getScenario(id) {
  return request({
    url: `/scenarios/${id}`,
    method: 'get',
  })
}

export function createScenario(data) {
  return request({
    url: '/scenarios',
    method: 'post',
    data,
  })
}

export function updateScenario(id, data) {
  return request({
    url: `/scenarios/${id}`,
    method: 'put',
    data,
  })
}

export function deleteScenario(id) {
  return request({
    url: `/scenarios/${id}`,
    method: 'delete',
  })
}
