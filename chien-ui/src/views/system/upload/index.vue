<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>文件上传</span>
      </template>

      <!-- 上传区域 -->
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-exceed="handleExceed"
        :file-list="fileList"
        :auto-upload="false"
        multiple
        drag
        :limit="10"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持图片、文档、压缩包等格式，单个文件最大 10MB，最多同时上传 10 个
          </div>
        </template>
      </el-upload>

      <div style="margin-top: 16px; text-align: right;">
        <el-button type="primary" @click="submitUpload" :loading="uploading">
          开始上传
        </el-button>
        <el-button @click="clearFiles">清空</el-button>
      </div>
    </el-card>

    <!-- 上传记录 -->
    <el-card style="margin-top: 16px;" v-if="uploadRecords.length > 0">
      <template #header>
        <div class="card-header">
          <span>上传记录</span>
          <el-button type="danger" link size="small" @click="uploadRecords = []">清空记录</el-button>
        </div>
      </template>

      <el-table :data="uploadRecords" border stripe size="small">
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="extension" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.extension }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="预览" width="100">
          <template #default="{ row }">
            <el-image
              v-if="isImage(row.extension)"
              :src="row.url"
              :preview-src-list="[row.url]"
              style="width: 40px; height: 40px;"
              fit="cover"
            />
            <span v-else style="color: #909399; font-size: 12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="访问地址" min-width="300">
          <template #default="{ row }">
            <el-input :model-value="row.url" size="small" readonly>
              <template #append>
                <el-button @click="copyUrl(row.url)">复制</el-button>
              </template>
            </el-input>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const uploadRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const uploadRecords = ref([])

// 上传地址
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload'

// 请求头（携带 Token）
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

// 上传前校验
function beforeUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(`文件 "${file.name}" 超过 10MB 限制`)
    return false
  }
  return true
}

// 上传成功
function handleSuccess(response, file) {
  if (response.code === 200) {
    uploadRecords.value.unshift(response.data)
    ElMessage.success(`"${file.name}" 上传成功`)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 上传失败
function handleError() {
  ElMessage.error('上传失败，请检查网络或文件')
}

// 超出数量限制
function handleExceed() {
  ElMessage.warning('最多同时上传 10 个文件')
}

// 提交上传
function submitUpload() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  uploadRef.value.submit()
  // submit 后等所有文件处理完
  setTimeout(() => { uploading.value = false }, 2000)
}

// 清空文件列表
function clearFiles() {
  uploadRef.value.clearFiles()
}

// 复制链接
function copyUrl(url) {
  const fullUrl = window.location.origin + url
  navigator.clipboard.writeText(fullUrl)
  ElMessage.success('链接已复制')
}

// 判断是否图片
function isImage(ext) {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(ext.toLowerCase())
}

// 格式化文件大小
function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
