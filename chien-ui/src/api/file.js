import request from '@/utils/request'

// 上传单个文件
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量上传
export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  return request({
    url: '/file/upload/batch',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 文件列表
export function listFileApi() {
  return request({ url: '/file/list', method: 'get' })
}

// 删除文件
export function deleteFileApi(id) {
  return request({ url: '/file/' + id, method: 'delete' })
}
