import request from '@/utils/request'

// 公告列表（管理端）
export function listNoticeApi() {
  return request({ url: '/notice/list', method: 'get' })
}

// 已发布公告（展示用）
export function publishedNoticeApi() {
  return request({ url: '/notice/published', method: 'get' })
}

// 公告详情
export function getNoticeApi(id) {
  return request({ url: '/notice/' + id, method: 'get' })
}

// 新增公告
export function addNoticeApi(data) {
  return request({ url: '/notice', method: 'post', data })
}

// 修改公告
export function editNoticeApi(data) {
  return request({ url: '/notice', method: 'put', data })
}

// 删除公告
export function removeNoticeApi(id) {
  return request({ url: '/notice/' + id, method: 'delete' })
}
