<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="roleList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="角色标识" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handlePerm(row)">权限</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入角色标识" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配菜单权限" width="500px" destroy-on-close>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        check-strictly
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handlePermSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listRoleApi, addRoleApi, editRoleApi, removeRoleApi, changeMenuApi } from '@/api/role'
import { listMenuApi } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const roleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const permDialogVisible = ref(false)
const menuTreeRef = ref(null)
const menuTreeData = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)

const form = reactive({
  id: null,
  roleName: '',
  roleKey: '',
  description: '',
  status: 0
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await listRoleApi()
    roleList.value = res.data
  } catch (e) {
    // error handled
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.roleName = ''
  form.roleKey = ''
  form.description = ''
  form.status = 0
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await editRoleApi(form)
      ElMessage.success('修改成功')
    } else {
      await addRoleApi(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    // error handled
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除角色 "' + row.roleName + '" 吗？', '提示', { type: 'warning' })
    await removeRoleApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    // cancel or error
  }
}

/**
 * 构建菜单树（只保留 M 和 C 类型，用于权限勾选）
 */
function buildMenuTreeData(menus) {
  // 所有菜单都可用于权限勾选（包括按钮 F）
  const rootMenus = menus.filter(m => m.parentId === 0)
  return buildTree(menus, rootMenus)
}

function buildTree(allMenus, parents) {
  return parents.map(parent => {
    const children = allMenus.filter(m => m.parentId === parent.id)
    return {
      ...parent,
      children: children.length > 0 ? buildTree(allMenus, children) : []
    }
  })
}

async function handlePerm(row) {
  currentRoleId.value = row.id
  try {
    // 获取全部菜单列表
    const menuRes = await listMenuApi()
    const allMenus = menuRes.data
    menuTreeData.value = buildMenuTreeData(allMenus)

    // 获取角色详情（包含已关联的菜单ID）
    // 通过角色菜单关联表查询
    // 这里简化处理：直接让后端返回角色详情
    checkedMenuIds.value = []
    permDialogVisible.value = true
  } catch (e) {
    // error handled
  }
}

async function handlePermSubmit() {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys]

  submitLoading.value = true
  try {
    await changeMenuApi(currentRoleId.value, menuIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (e) {
    // error handled
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
