<template>
  <div class="online-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线用户</span>
          <el-button type="primary" :icon="Refresh" @click="fetchList">刷新</el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="登录IP" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="lastActiveTime" label="最后活跃" width="180" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleKick(row)">强退</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tableData.length === 0" class="empty-tip">暂无在线用户</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOnlineList, kickByToken } from '@/api/online'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const tableData = ref([])

async function fetchList() {
  try {
    const res = await getOnlineList()
    tableData.value = res.data || []
  } catch {
    // error already handled by request interceptor
  }
}

async function handleKick(row) {
  try {
    await ElMessageBox.confirm(
      `确定要强退用户「${row.username}」吗？`,
      '强退确认',
      { type: 'warning', confirmButtonText: '确定强退', cancelButtonText: '取消' }
    )
    await kickByToken(row.token)
    ElMessage.success(`已强退用户「${row.username}」`)
    fetchList()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.online-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 14px;
}
</style>
