import request from '@/utils/request'

// ========== 字典类型 ==========
export function listDictTypeApi() {
  return request({ url: '/dict/type/list', method: 'get' })
}

export function addDictTypeApi(data) {
  return request({ url: '/dict/type', method: 'post', data })
}

export function editDictTypeApi(data) {
  return request({ url: '/dict/type', method: 'put', data })
}

export function removeDictTypeApi(id) {
  return request({ url: `/dict/type/${id}`, method: 'delete' })
}

// ========== 字典数据 ==========
export function listDictDataApi() {
  return request({ url: '/dict/data/list', method: 'get' })
}

export function listDictDataByTypeApi(dictType) {
  return request({ url: `/dict/data/type/${encodeURIComponent(dictType)}`, method: 'get' })
}

export function addDictDataApi(data) {
  return request({ url: '/dict/data', method: 'post', data })
}

export function editDictDataApi(data) {
  return request({ url: '/dict/data', method: 'put', data })
}

export function removeDictDataApi(id) {
  return request({ url: `/dict/data/${id}`, method: 'delete' })
}
