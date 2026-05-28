import request from '@/utils/request'

export function loginApi(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function getUserInfoApi() {
  return request({ url: '/auth/info', method: 'get' })
}

export function logoutApi() {
  return request({ url: '/auth/logout', method: 'post' })
}
