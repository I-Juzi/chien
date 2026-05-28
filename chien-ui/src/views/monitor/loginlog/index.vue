<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>登录日志</span>
          <el-button type="danger" @click="handleClean">清空</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
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
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="登录IP" width="130" />
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="消息" min-width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listLoginLogApi, removeLoginLogApi, cleanLoginLogApi } from '@/api/loginlog'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: null
})

async function fetchList() {
  loading.value = true
  try {
    const res = await listLoginLogApi(queryParams)
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
  queryParams.username = ''
  queryParams.status = null
  queryParams.pageNum = 1
  fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该日志记录吗？', '提示', { type: 'warning' })
    await removeLoginLogApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {}
}

async function handleClean() {
  try {
    await ElMessageBox.confirm('确认清空所有登录日志吗？此操作不可恢复！', '警告', { type: 'error', confirmButtonText: '确认清空' })
    await cleanLoginLogApi()
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
</style>
