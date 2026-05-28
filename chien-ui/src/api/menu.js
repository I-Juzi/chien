import request from '@/utils/request'

export function listMenuApi() {
  return request({ url: '/system/menu/list', method: 'get' })
}

export function getMenuApi(menuId) {
  return request({ url: `/system/menu/${menuId}`, method: 'get' })
}

export function addMenuApi(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

export function editMenuApi(data) {
  return request({ url: '/system/menu', method: 'put', data })
}

export function removeMenuApi(menuIds) {
  return request({ url: `/system/menu/${menuIds}`, method: 'delete' })
}

export function getMenuTreeApi() {
  return request({ url: '/system/menu/tree', method: 'get' })
}
