<template>
  <div class="page-container">
    <!-- 上传区域 -->
    <el-card>
      <template #header>
        <span>文件上传</span>
      </template>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-exceed="handleExceed"
        :on-change="handleChange"
        :on-remove="handleChange"
        multiple
        drag
        :limit="10"
        list-type="picture"
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
        <el-button type="primary" @click="submitUpload" :loading="uploading">开始上传</el-button>
        <el-button @click="clearFiles">清空</el-button>
      </div>
    </el-card>

    <!-- 文件列表 -->
    <el-card style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>文件列表</span>
          <el-button type="primary" link size="small" @click="fetchFileList">刷新</el-button>
        </div>
      </template>

      <el-table :data="fileList" border stripe v-loading="loading" size="small">
        <el-table-column label="缩略图" width="80">
          <template #default="{ row }">
            <el-image
              v-if="isImage(row.extension)"
              :src="row.url"
              :preview-src-list="[row.url]"
              style="width: 50px; height: 50px; border-radius: 4px;"
              fit="cover"
            />
            <icon-renderer v-else :icon="getFileIcon(row.extension)" :size="28" />
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadUser" label="上传者" width="100" />
        <el-table-column label="访问地址" min-width="250">
          <template #default="{ row }">
            <el-input :model-value="row.url" size="small" readonly>
              <template #append>
                <el-button @click="copyUrl(row.url)">复制</el-button>
              </template>
            </el-input>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFileApi, deleteFileApi, uploadFiles } from '@/api/file'
import IconRenderer from '@/components/IconRenderer.vue'

const uploadRef = ref(null)
const uploading = ref(false)
const loading = ref(false)
const fileList = ref([])
const selectedFiles = ref([])

onMounted(() => { fetchFileList() })

async function fetchFileList() {
  loading.value = true
  try {
    const res = await listFileApi()
    fileList.value = res.data
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

// 文件选择变化时记录文件
function handleChange(file, newFileList) {
  selectedFiles.value = newFileList.map(f => f.raw)
}

function handleExceed() {
  ElMessage.warning('最多同时上传 10 个文件')
}

// 手动批量上传
async function submitUpload() {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  // 校验大小
  for (const f of selectedFiles.value) {
    if (f.size > 10 * 1024 * 1024) {
      ElMessage.error(`文件 "${f.name}" 超过 10MB 限制`)
      return
    }
  }

  uploading.value = true
  try {
    const res = await uploadFiles(selectedFiles.value)
    if (res.code === 200) {
      ElMessage.success(`成功上传 ${res.data.length} 个文件`)
      uploadRef.value?.clearFiles()
      selectedFiles.value = []
      fetchFileList()
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    // 错误已在 request.js 拦截器中提示，此处不重复
  } finally {
    uploading.value = false
  }
}

function clearFiles() {
  uploadRef.value?.clearFiles()
  selectedFiles.value = []
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除文件 "' + row.originalName + '" 吗？', '提示', { type: 'warning' })
    await deleteFileApi(row.id)
    ElMessage.success('删除成功')
    fetchFileList()
  } catch (e) { /* cancel */ }
}

function copyUrl(url) {
  navigator.clipboard.writeText(window.location.origin + url)
  ElMessage.success('链接已复制')
}

function isImage(ext) {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(ext?.toLowerCase())
}

function getFileIcon(ext) {
  if (!ext) return 'ep:document'
  const e = ext.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(e)) return 'ep:picture'
  if (['doc', 'docx'].includes(e)) return 'mdi:file-word'
  if (['xls', 'xlsx'].includes(e)) return 'mdi:file-excel'
  if (['ppt', 'pptx'].includes(e)) return 'mdi:file-powerpoint'
  if (e === 'pdf') return 'mdi:file-pdf-box'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(e)) return 'ep:folder-opened'
  if (['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'].includes(e)) return 'ep:video-camera'
  if (['mp3', 'wav', 'flac', 'aac', 'ogg'].includes(e)) return 'ep:headset'
  if (['js', 'ts', 'java', 'py', 'html', 'css', 'json', 'xml', 'vue'].includes(e)) return 'ep:monitor'
  return 'ep:document'
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
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
