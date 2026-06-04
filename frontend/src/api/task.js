import request from '@/utils/request'

export function startTask(data) {
  return request({
    url: '/tasks/start',
    method: 'post',
    data,
  })
}

export function stopTask(taskId) {
  return request({
    url: `/tasks/${taskId}/stop`,
    method: 'post',
  })
}

export function getTaskStatus(taskId) {
  return request({
    url: `/tasks/${taskId}/status`,
    method: 'get',
  })
}

export function getTasks() {
  return request({
    url: '/tasks',
    method: 'get',
  })
}

export function getTaskResult(taskId) {
  return request({
    url: `/tasks/${taskId}/result`,
    method: 'get',
  })
}

export function deleteTask(taskId) {
  return request({
    url: `/tasks/${taskId}`,
    method: 'delete',
  })
}
