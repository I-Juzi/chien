import request from '@/utils/request'

export function listUserApi() {
  return request({ url: '/system/user/list', method: 'get' })
}

export function getUserApi(userId) {
  return request({ url: `/system/user/${userId}`, method: 'get' })
}

export function addUserApi(data) {
  return request({ url: '/system/user', method: 'post', data })
}

export function editUserApi(data) {
  return request({ url: '/system/user', method: 'put', data })
}

export function removeUserApi(userIds) {
  return request({ url: `/system/user/${userIds}`, method: 'delete' })
}

export function resetPwdApi(data) {
  return request({ url: '/system/user/resetPwd', method: 'put', data })
}

export function changeRoleApi(userId, roleIds) {
  return request({ url: '/system/user/changeRole', method: 'put', params: { userId }, data: roleIds })
}
