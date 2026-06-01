import request from '@/utils/request'

// 获取个人信息
export function getProfileApi() {
  return request({ url: '/auth/profile', method: 'get' })
}

// 修改个人信息
export function updateProfileApi(data) {
  return request({ url: '/auth/profile', method: 'put', data })
}

// 修改密码
export function changePasswordApi(data) {
  return request({ url: '/auth/password', method: 'put', data })
}
