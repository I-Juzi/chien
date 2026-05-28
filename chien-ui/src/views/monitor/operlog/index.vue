<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <el-button type="danger" @click="handleClean">清空</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="模块标题">
          <el-input v-model="queryParams.title" placeholder="请输入模块标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable>
            <el-option label="新增" :value="1" />
            <el-option label="修改" :value="2" />
            <el-option label="删除" :value="3" />
            <el-option label="导出" :value="4" />
            <el-option label="导入" :value="5" />
            <el-option label="其他" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border v-loading="loading">
        <el-table-column prop="id" label="日志ID" width="70" align="center" />
        <el-table-column prop="title" label="模块标题" width="120" />
        <el-table-column prop="businessType" label="业务类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="businessTagType(row.businessType)">{{ businessTypeText(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="operIp" label="操作IP" width="130" />
        <el-table-column prop="requestMethod" label="请求方式" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="methodTagType(row.requestMethod)" size="small">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operUrl" label="请求URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.costTime > 1000 ? '#f56c6c' : '' }">{{ row.costTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ businessTypeText(detail.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">
          <el-tag :type="methodTagType(detail.requestMethod)" size="small">{{ detail.requestMethod }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'">{{ detail.status === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="detail-pre">{{ formatJson(detail.operParam) }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div class="detail-pre">{{ formatJson(detail.jsonResult) }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误信息" :span="2">
          <div class="detail-pre error">{{ detail.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listOperLogApi, getOperLogApi, removeOperLogApi, cleanOperLogApi } from '@/api/operlog'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  businessType: null,
  status: null
})

const detailVisible = ref(false)
const detail = ref({})

// 业务类型文本
const businessTypeMap = { 0: '其他', 1: '新增', 2: '修改', 3: '删除', 4: '导出', 5: '导入' }
function businessTypeText(type) {
  return businessTypeMap[type] || '其他'
}

function businessTagType(type) {
  const map = { 1: 'success', 2: 'primary', 3: 'danger', 4: 'warning', 5: 'info' }
  return map[type] || 'info'
}

function methodTagType(method) {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function formatJson(str) {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listOperLogApi(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch {} finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  fetchList()
}

function handleReset() {
  queryParams.title = ''
  queryParams.businessType = null
  queryParams.status = null
  queryParams.pageNum = 1
  fetchList()
}

async function handleDetail(row) {
  try {
    const res = await getOperLogApi(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该日志记录吗？', '提示', { type: 'warning' })
    await removeOperLogApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {}
}

async function handleClean() {
  try {
    await ElMessageBox.confirm('确认清空所有操作日志吗？此操作不可恢复！', '警告', { type: 'error', confirmButtonText: '确认清空' })
    await cleanOperLogApi()
    ElMessage.success('已清空')
    fetchList()
  } catch {}
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.detail-pre.error {
  color: #f56c6c;
  background: #fef0f0;
}
</style>
