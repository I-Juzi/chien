import request from '@/utils/request'

// 分页查询操作日志
export function listOperLogApi(params) {
  return request({ url: '/operlog/list', method: 'get', params })
}

// 查询日志详情
export function getOperLogApi(id) {
  return request({ url: `/operlog/${id}`, method: 'get' })
}

// 删除日志
export function removeOperLogApi(id) {
  return request({ url: `/operlog/${id}`, method: 'delete' })
}

// 清空日志
export function cleanOperLogApi() {
  return request({ url: '/operlog/clean', method: 'delete' })
}
