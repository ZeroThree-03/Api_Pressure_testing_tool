import request from '@/utils/request'

export function getEnvVariables(environmentId) {
  return request({
    url: '/env-variables',
    method: 'get',
    params: { environmentId },
  })
}

export function createEnvVariable(data) {
  return request({
    url: '/env-variables',
    method: 'post',
    data,
  })
}

export function updateEnvVariable(id, data) {
  return request({
    url: `/env-variables/${id}`,
    method: 'put',
    data,
  })
}

export function deleteEnvVariable(id) {
  return request({
    url: `/env-variables/${id}`,
    method: 'delete',
  })
}

export function batchCreateEnvVariables(environmentId, variables) {
  return request({
    url: '/env-variables/batch',
    method: 'post',
    data: { environmentId, variables },
  })
}
