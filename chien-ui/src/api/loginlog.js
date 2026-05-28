import request from '@/utils/request'

export function listLoginLogApi(params) {
  return request({ url: '/loginlog/list', method: 'get', params })
}

export function removeLoginLogApi(id) {
  return request({ url: `/loginlog/${id}`, method: 'delete' })
}

export function cleanLoginLogApi() {
  return request({ url: '/loginlog/clean', method: 'delete' })
}
