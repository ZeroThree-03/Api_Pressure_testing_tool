import request from '@/utils/request'

export function getEnvironments() {
  return request({
    url: '/environments',
    method: 'get',
  })
}

export function createEnvironment(data) {
  return request({
    url: '/environments',
    method: 'post',
    data,
  })
}

export function updateEnvironment(id, data) {
  return request({
    url: `/environments/${id}`,
    method: 'put',
    data,
  })
}

export function deleteEnvironment(id) {
  return request({
    url: `/environments/${id}`,
    method: 'delete',
  })
}

export function activateEnvironment(id) {
  return request({
    url: `/environments/${id}/activate`,
    method: 'put',
  })
}
