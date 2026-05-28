import request from '@/utils/request'

export function listRoleApi() {
  return request({ url: '/system/role/list', method: 'get' })
}

export function getRoleApi(roleId) {
  return request({ url: `/system/role/${roleId}`, method: 'get' })
}

export function addRoleApi(data) {
  return request({ url: '/system/role', method: 'post', data })
}

export function editRoleApi(data) {
  return request({ url: '/system/role', method: 'put', data })
}

export function removeRoleApi(roleIds) {
  return request({ url: `/system/role/${roleIds}`, method: 'delete' })
}

export function changeMenuApi(roleId, menuIds) {
  return request({ url: '/system/role/changeMenu', method: 'put', params: { roleId }, data: menuIds })
}
