import request from '@/utils/request'

// 获取在线用户列表
export function getOnlineList() {
  return request({ url: '/online/list', method: 'get' })
}

// 强退用户（按 token）
export function kickByToken(token) {
  return request({ url: `/online/kick/${encodeURIComponent(token)}`, method: 'delete' })
}

// 强退用户（按 userId，踢掉所有会话）
export function kickByUserId(userId) {
  return request({ url: `/online/kick/user/${userId}`, method: 'delete' })
}
