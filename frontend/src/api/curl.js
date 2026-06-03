import request from '@/utils/request'

export function parseCurl(curl, variables) {
  return request({
    url: '/curl/parse',
    method: 'post',
    data: { curl, variables },
  })
}
