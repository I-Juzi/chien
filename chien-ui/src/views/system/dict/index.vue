<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据字典</span>
          <el-button type="primary" @click="handleAddType">新增字典</el-button>
        </div>
      </template>

      <!-- 字典类型表格 -->
      <el-table :data="typeList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="dictName" label="字典名称" width="180" />
        <el-table-column prop="dictType" label="字典类型" min-width="200">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain" style="cursor:pointer" @click="handleViewData(row)">{{ row.dictType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewData(row)">数据</el-button>
            <el-button type="primary" link size="small" @click="handleEditType(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 字典类型 新增/编辑 弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如：用户性别" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如：sys_user_gender" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据 抽屉 -->
    <el-drawer v-model="dataDrawerVisible" :title="'字典数据 — ' + currentType.dictName" size="600px" destroy-on-close>
      <template #header>
        <div class="drawer-header">
          <span>字典数据 — {{ currentType.dictName }}</span>
          <el-tag type="primary" effect="plain" size="small">{{ currentType.dictType }}</el-tag>
        </div>
      </template>

      <div style="margin-bottom: 12px; text-align: right;">
        <el-button type="primary" size="small" @click="handleAddData">新增数据</el-button>
      </div>

      <el-table :data="dataList" border v-loading="dataLoading">
        <el-table-column prop="dictLabel" label="字典标签" width="120" />
        <el-table-column prop="dictValue" label="字典键值" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditData(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 字典数据 新增/编辑 弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如：男" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如：1" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dataForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  listDictTypeApi, addDictTypeApi, editDictTypeApi, removeDictTypeApi,
  listDictDataByTypeApi, addDictDataApi, editDictDataApi, removeDictDataApi
} from '@/api/dict'
import { ElMessage, ElMessageBox } from 'element-plus'

// ========== 字典类型 ==========
const loading = ref(false)
const submitLoading = ref(false)
const typeList = ref([])
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const typeFormRef = ref(null)

const typeForm = reactive({ id: null, dictName: '', dictType: '', status: 1, remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

async function fetchTypeList() {
  loading.value = true
  try {
    const res = await listDictTypeApi()
    typeList.value = res.data
  } catch {} finally {
    loading.value = false
  }
}

function resetTypeForm() {
  typeForm.id = null
  typeForm.dictName = ''
  typeForm.dictType = ''
  typeForm.status = 1
  typeForm.remark = ''
}

function handleAddType() {
  resetTypeForm()
  typeDialogTitle.value = '新增字典类型'
  typeDialogVisible.value = true
}

function handleEditType(row) {
  Object.assign(typeForm, { ...row })
  typeDialogTitle.value = '编辑字典类型'
  typeDialogVisible.value = true
}

async function handleSubmitType() {
  const valid = await typeFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (typeForm.id) {
      await editDictTypeApi(typeForm)
      ElMessage.success('修改成功')
    } else {
      await addDictTypeApi(typeForm)
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    fetchTypeList()
  } catch {} finally {
    submitLoading.value = false
  }
}

async function handleDeleteType(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典「${row.dictName}」吗？关联的字典数据也将失效。`, '提示', { type: 'warning' })
    await removeDictTypeApi(row.id)
    ElMessage.success('删除成功')
    fetchTypeList()
  } catch {}
}

// ========== 字典数据 ==========
const dataLoading = ref(false)
const dataDrawerVisible = ref(false)
const dataList = ref([])
const currentType = ref({})
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataFormRef = ref(null)

const dataForm = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', sortOrder: 0, status: 1, remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

async function handleViewData(row) {
  currentType.value = row
  dataDrawerVisible.value = true
  await fetchDataList(row.dictType)
}

async function fetchDataList(dictType) {
  dataLoading.value = true
  try {
    const res = await listDictDataByTypeApi(dictType)
    dataList.value = res.data
  } catch {} finally {
    dataLoading.value = false
  }
}

function resetDataForm() {
  dataForm.id = null
  dataForm.dictType = currentType.value.dictType
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.sortOrder = 0
  dataForm.status = 1
  dataForm.remark = ''
}

function handleAddData() {
  resetDataForm()
  dataDialogTitle.value = '新增字典数据'
  dataDialogVisible.value = true
}

function handleEditData(row) {
  Object.assign(dataForm, { ...row })
  dataDialogTitle.value = '编辑字典数据'
  dataDialogVisible.value = true
}

async function handleSubmitData() {
  const valid = await dataFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (dataForm.id) {
      await editDictDataApi(dataForm)
      ElMessage.success('修改成功')
    } else {
      await addDictDataApi(dataForm)
      ElMessage.success('新增成功')
    }
    dataDialogVisible.value = false
    fetchDataList(currentType.value.dictType)
  } catch {} finally {
    submitLoading.value = false
  }
}

async function handleDeleteData(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」吗？`, '提示', { type: 'warning' })
    await removeDictDataApi(row.id)
    ElMessage.success('删除成功')
    fetchDataList(currentType.value.dictType)
  } catch {}
}

onMounted(() => { fetchTypeList() })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
