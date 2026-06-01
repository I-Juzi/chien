<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知公告</span>
          <el-button type="primary" @click="handleAdd">发布公告</el-button>
        </div>
      </template>

      <el-table :data="noticeList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 1 ? 'info' : 'warning'" size="small">
              {{ row.noticeType === 1 ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="创建者" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">通知</el-radio>
            <el-radio :value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewVisible" title="公告详情" width="600px">
      <h3 style="margin: 0 0 10px;">{{ viewData.title }}</h3>
      <div style="color: #909399; font-size: 12px; margin-bottom: 16px;">
        <span>{{ viewData.createBy }}</span>
        <span style="margin-left: 16px;">{{ viewData.createTime }}</span>
        <el-tag :type="viewData.noticeType === 1 ? 'info' : 'warning'" size="small" style="margin-left: 16px;">
          {{ viewData.noticeType === 1 ? '通知' : '公告' }}
        </el-tag>
      </div>
      <el-divider />
      <div style="white-space: pre-wrap; line-height: 1.8;">{{ viewData.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listNoticeApi, addNoticeApi, editNoticeApi, removeNoticeApi } from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const noticeList = ref([])
const dialogVisible = ref(false)
const viewVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const viewData = ref({})

const form = reactive({
  id: null,
  title: '',
  content: '',
  noticeType: 1,
  status: 0
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

onMounted(() => { fetchList() })

async function fetchList() {
  loading.value = true
  try {
    const res = await listNoticeApi()
    noticeList.value = res.data
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.title = ''
  form.content = ''
  form.noticeType = 1
  form.status = 0
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '发布公告'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = row
  viewVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await editNoticeApi(form)
      ElMessage.success('修改成功')
    } else {
      await addNoticeApi(form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) { /* handled */ } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除公告 "' + row.title + '" 吗？', '提示', { type: 'warning' })
    await removeNoticeApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) { /* cancel */ }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
