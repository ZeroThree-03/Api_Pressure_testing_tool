import request from '@/utils/request'

export function getTemplates() {
  return request({
    url: '/templates',
    method: 'get',
  })
}

export function getTemplate(id) {
  return request({
    url: `/templates/${id}`,
    method: 'get',
  })
}

export function createTemplate(data) {
  return request({
    url: '/templates',
    method: 'post',
    data,
  })
}

export function updateTemplate(id, data) {
  return request({
    url: `/templates/${id}`,
    method: 'put',
    data,
  })
}

export function deleteTemplate(id) {
  return request({
    url: `/templates/${id}`,
    method: 'delete',
  })
}
