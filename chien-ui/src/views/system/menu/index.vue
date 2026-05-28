<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd(null)">新增菜单</el-button>
        </div>
      </template>

      <el-table
        :data="menuTree"
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
        default-expand-all
      >
        <el-table-column prop="menuName" label="菜单名称" width="200" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <icon-renderer v-if="row.icon" :icon="row.icon" :size="18" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="primary">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="path" label="路由地址" />
        <el-table-column prop="component" label="组件路径" />
        <el-table-column prop="isVisible" label="可见" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isVisible === 1 ? 'success' : 'info'">
              {{ row.isVisible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.menuType !== 'F'" type="primary" link size="small" @click="handleAdd(row)">新增</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeForSelect"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="选择上级菜单（不选则为顶级）"
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="图标">
          <div class="icon-select" @click="openIconPicker">
            <icon-renderer v-if="form.icon" :icon="form.icon" :size="20" />
            <span class="icon-text">{{ form.icon || '点击选择图标' }}</span>
            <el-icon v-if="form.icon" class="icon-clear" @click.stop="form.icon = ''"><Close /></el-icon>
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址">
          <el-input v-model="form.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径">
          <el-input v-model="form.component" placeholder="如: system/user/index" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'M'" label="权限标识">
          <el-input v-model="form.perms" placeholder="如: system:user:list" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="是否可见">
          <el-radio-group v-model="form.isVisible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器弹窗 -->
    <el-dialog v-model="iconPickerVisible" title="选择图标" width="780px" destroy-on-close>
      <div class="icon-picker-toolbar">
        <el-input
          v-model="iconSearch"
          placeholder="搜索图标..."
          clearable
          class="icon-search"
        />
        <el-radio-group v-model="activeIconSet" size="small">
          <el-radio-button value="ep">Element Plus</el-radio-button>
          <el-radio-button value="mdi">Material</el-radio-button>
          <el-radio-button value="fa6">Font Awesome</el-radio-button>
          <el-radio-button value="heroicons">Heroicons</el-radio-button>
          <el-radio-button value="tabler">Tabler</el-radio-button>
          <el-radio-button value="carbon">Carbon</el-radio-button>
        </el-radio-group>
      </div>
      <div class="icon-grid" :key="activeIconSet">
        <div
          v-for="name in displayIcons"
          :key="name"
          class="icon-item"
          :class="{ active: form.icon === name }"
          @click="selectIcon(name)"
        >
          <icon-renderer :icon="name" :size="20" />
          <span class="icon-name">{{ name.split(':').pop() }}</span>
        </div>
      </div>
      <div v-if="displayIcons.length === 0 && iconSearch" class="empty-tip">未找到匹配的图标</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { listMenuApi, addMenuApi, editMenuApi, removeMenuApi } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const menuList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

// ========== 图标选择器 ==========
const iconPickerVisible = ref(false)
const iconSearch = ref('')
const activeIconSet = ref('ep')

// Element Plus 图标列表
const epIconNames = Object.keys(ElementPlusIconsVue)

// Iconify 图标集（常用精选）
const iconifySets = {
  mdi: [
    'mdi:home', 'mdi:account', 'mdi:account-group', 'mdi:cog', 'mdi:settings',
    'mdi:logout', 'mdi:login', 'mdi:lock', 'mdi:lock-open', 'mdi:key',
    'mdi:shield', 'mdi:shield-check', 'mdi:bell', 'mdi:email', 'mdi:message',
    'mdi:phone', 'mdi:map-marker', 'mdi:calendar', 'mdi:clock', 'mdi:calendar-clock',
    'mdi:chart-bar', 'mdi:chart-line', 'mdi:chart-pie', 'mdi:finance', 'mdi:trending-up',
    'mdi:view-dashboard', 'mdi:view-list', 'mdi:view-grid', 'mdi:format-list-bulleted', 'mdi:table',
    'mdi:file', 'mdi:file-document', 'mdi:folder', 'mdi:download', 'mdi:upload',
    'mdi:printer', 'mdi:database', 'mdi:server', 'mdi:cloud', 'mdi:cloud-upload',
    'mdi:magnify', 'mdi:magnify-plus', 'mdi:magnify-minus', 'mdi:eye', 'mdi:eye-off',
    'mdi:pencil', 'mdi:plus', 'mdi:minus', 'mdi:close', 'mdi:check',
    'mdi:delete', 'mdi:trash-can', 'mdi:refresh', 'mdi:reload', 'mdi:sync',
    'mdi:arrow-left', 'mdi:arrow-right', 'mdi:arrow-up', 'mdi:arrow-down',
    'mdi:chevron-left', 'mdi:chevron-right', 'mdi:chevron-up', 'mdi:chevron-down',
    'mdi:menu', 'mdi:dots-vertical', 'mdi:dots-horizontal', 'mdi:filter', 'mdi:sort',
    'mdi:tag', 'mdi:bookmark', 'mdi:star', 'mdi:heart', 'mdi:thumb-up',
    'mdi:camera', 'mdi:image', 'mdi:video', 'mdi:microphone', 'mdi:headphones',
    'mdi:wifi', 'mdi:bluetooth', 'mdi:power', 'mdi:flash', 'mdi:battery',
    'mdi:cart', 'mdi:shopping', 'mdi:cash', 'mdi:credit-card', 'mdi:bank',
    'mdi:school', 'mdi:book-open', 'mdi:library', 'mdi:teach', 'mdi:certificate',
    'mdi:factory', 'mdi:office-building', 'mdi:store', 'mdi:warehouse', 'mdi:domain',
    'mdi:car', 'mdi:truck', 'mdi:airplane', 'mdi:train', 'mdi:bike',
    'mdi:weather-sunny', 'mdi:weather-cloudy', 'mdi:weather-rainy', 'mdi:thermometer', 'mdi:water',
    'mdi:leaf', 'mdi:tree', 'mdi:flower', 'mdi:nature', 'mdi:paw',
  ],
  fa6: [
    'fa6-solid:house', 'fa6-solid:user', 'fa6-solid:users', 'fa6-solid:gear', 'fa6-solid:right-from-bracket',
    'fa6-solid:right-to-bracket', 'fa6-solid:lock', 'fa6-solid:lock-open', 'fa6-solid:key', 'fa6-solid:shield',
    'fa6-solid:bell', 'fa6-solid:envelope', 'fa6-solid:phone', 'fa6-solid:location-dot', 'fa6-solid:calendar',
    'fa6-solid:clock', 'fa6-solid:chart-bar', 'fa6-solid:chart-line', 'fa6-solid:chart-pie', 'fa6-solid:arrow-trend-up',
    'fa6-solid:gauge', 'fa6-solid:list', 'fa6-solid:table', 'fa6-solid:file', 'fa6-solid:folder',
    'fa6-solid:download', 'fa6-solid:upload', 'fa6-solid:print', 'fa6-solid:database', 'fa6-solid:server',
    'fa6-solid:cloud', 'fa6-solid:magnifying-glass', 'fa6-solid:eye', 'fa6-solid:eye-slash', 'fa6-solid:pen',
    'fa6-solid:plus', 'fa6-solid:minus', 'fa6-solid:xmark', 'fa6-solid:check', 'fa6-solid:trash',
    'fa6-solid:arrows-rotate', 'fa6-solid:arrow-left', 'fa6-solid:arrow-right', 'fa6-solid:bars', 'fa6-solid:ellipsis',
    'fa6-solid:filter', 'fa6-solid:tag', 'fa6-solid:bookmark', 'fa6-solid:star', 'fa6-solid:heart',
    'fa6-solid:camera', 'fa6-solid:image', 'fa6-solid:video', 'fa6-solid:microphone', 'fa6-solid:wifi',
    'fa6-solid:bolt', 'fa6-solid:cart-shopping', 'fa6-solid:credit-card', 'fa6-solid:money-bill', 'fa6-solid:building',
    'fa6-solid:store', 'fa6-solid:car', 'fa6-solid:plane', 'fa6-solid:train', 'fa6-solid:leaf',
    'fa6-solid:seedling', 'fa6-solid:dog', 'fa6-solid:cat', 'fa6-solid:fish', 'fa6-solid:bug',
  ],
  heroicons: [
    'heroicons:home', 'heroicons:user', 'heroicons:user-group', 'heroicons:cog-6-tooth', 'heroicons:arrow-right-start-on-rectangle',
    'heroicons:arrow-left-end-on-rectangle', 'heroicons:lock-closed', 'heroicons:lock-open', 'heroicons:key', 'heroicons:shield-check',
    'heroicons:bell', 'heroicons:envelope', 'heroicons:phone', 'heroicons:map-pin', 'heroicons:calendar',
    'heroicons:clock', 'heroicons:chart-bar', 'heroicons:presentation-chart-line', 'heroicons:chart-pie', 'heroicons:arrow-trending-up',
    'heroicons:squares-2x2', 'heroicons:list-bullet', 'heroicons:table-cells', 'heroicons:document', 'heroicons:folder',
    'heroicons:arrow-down-tray', 'heroicons:arrow-up-tray', 'heroicons:printer', 'heroicons:circle-stack', 'heroicons:server',
    'heroicons:cloud', 'heroicons:magnifying-glass', 'heroicons:eye', 'heroicons:eye-slash', 'heroicons:pencil',
    'heroicons:plus', 'heroicons:minus', 'heroicons:x-mark', 'heroicons:check', 'heroicons:trash',
    'heroicons:arrow-path', 'heroicons:arrow-left', 'heroicons:arrow-right', 'heroicons:bars-3', 'heroicons:ellipsis-horizontal',
    'heroicons:funnel', 'heroicons:tag', 'heroicons:bookmark', 'heroicons:star', 'heroicons:heart',
    'heroicons:camera', 'heroicons:photo', 'heroicons:film', 'heroicons:microphone', 'heroicons:wifi',
    'heroicons:bolt', 'heroicons:shopping-cart', 'heroicons:credit-card', 'heroicons:banknotes', 'heroicons:building-office',
    'heroicons:home-modern', 'heroicons:truck', 'heroicons:paper-airplane', 'heroicons:globe-alt', 'heroicons:language',
  ],
  tabler: [
    'tabler:home', 'tabler:user', 'tabler:users', 'tabler:settings', 'tabler:logout',
    'tabler:login', 'tabler:lock', 'tabler:lock-open', 'tabler:key', 'tabler:shield',
    'tabler:bell', 'tabler:mail', 'tabler:phone', 'tabler:map-pin', 'tabler:calendar',
    'tabler:clock', 'tabler:chart-bar', 'tabler:chart-line', 'tabler:chart-pie', 'tabler:trending-up',
    'tabler:layout-dashboard', 'tabler:list', 'tabler:table', 'tabler:file', 'tabler:folder',
    'tabler:download', 'tabler:upload', 'tabler:printer', 'tabler:database', 'tabler:server',
    'tabler:cloud', 'tabler:search', 'tabler:eye', 'tabler:eye-off', 'tabler:pencil',
    'tabler:plus', 'tabler:minus', 'tabler:x', 'tabler:check', 'tabler:trash',
    'tabler:refresh', 'tabler:arrow-left', 'tabler:arrow-right', 'tabler:menu', 'tabler:dots',
    'tabler:filter', 'tabler:tag', 'tabler:bookmark', 'tabler:star', 'tabler:heart',
    'tabler:camera', 'tabler:photo', 'tabler:video', 'tabler:microphone', 'tabler:wifi',
    'tabler:bolt', 'tabler:shopping-cart', 'tabler:credit-card', 'tabler:cash', 'tabler:building',
    'tabler:building-store', 'tabler:truck', 'tabler:plane', 'tabler:train', 'tabler:leaf',
    'tabler:dog', 'tabler:cat', 'tabler:fish', 'tabler:bug', 'tabler:plant',
    'tabler:sun', 'tabler:moon', 'tabler:cloud-rain', 'tabler:flame', 'tabler:droplet',
  ],
  carbon: [
    'carbon:home', 'carbon:user', 'carbon:user-multiple', 'carbon:settings', 'carbon:logout',
    'carbon:login', 'carbon:locked', 'carbon:unlocked', 'carbon:password', 'carbon:security',
    'carbon:notification', 'carbon:email', 'carbon:phone', 'carbon:location', 'carbon:calendar',
    'carbon:time', 'carbon:chart-bar', 'carbon:chart-line', 'carbon:chart-pie', 'carbon:chart-trend',
    'carbon:dashboard', 'carbon:list', 'carbon:data-table', 'carbon:document', 'carbon:folder',
    'carbon:download', 'carbon:upload', 'carbon:printer', 'carbon:data-base', 'carbon:server-proxy',
    'carbon:cloud', 'carbon:search', 'carbon:view', 'carbon:view-off', 'carbon:edit',
    'carbon:add', 'carbon:subtract', 'carbon:close', 'carbon:checkmark', 'carbon:trash-can',
    'carbon:renew', 'carbon:arrow-left', 'carbon:arrow-right', 'carbon:menu', 'carbon:overflow-menu-horizontal',
    'carbon:filter', 'carbon:tag', 'carbon:bookmark', 'carbon:star', 'carbon:favorite',
    'carbon:camera', 'carbon:image', 'carbon:video', 'carbon:microphone', 'carbon:wifi',
    'carbon:flash', 'carbon:shopping-cart', 'carbon:purchase', 'carbon:currency', 'carbon:building',
    'carbon:store', 'carbon:delivery-truck', 'carbon:airplane', 'carbon:train', 'carbon:leaf',
  ]
}

// 当前显示的图标列表
const displayIcons = computed(() => {
  const keyword = iconSearch.value.toLowerCase()

  if (activeIconSet.value === 'ep') {
    const names = epIconNames.map(n => n)
    if (!keyword) return names
    return names.filter(n => n.toLowerCase().includes(keyword))
  }

  const icons = iconifySets[activeIconSet.value] || []
  if (!keyword) return icons
  return icons.filter(n => n.toLowerCase().includes(keyword))
})

function openIconPicker() {
  iconSearch.value = ''
  // 自动选中当前图标所在的图标集
  if (form.icon) {
    if (!form.icon.includes(':')) {
      activeIconSet.value = 'ep'
    } else {
      const prefix = form.icon.split(':')[0]
      const setMap = { mdi: 'mdi', fa6: 'fa6', heroicons: 'heroicons', tabler: 'tabler', carbon: 'carbon' }
      activeIconSet.value = setMap[prefix] || 'ep'
    }
  }
  iconPickerVisible.value = true
}

function selectIcon(name) {
  form.icon = name
  iconPickerVisible.value = false
}

// ========== 业务逻辑 ==========
const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  isVisible: 1,
  menuType: 'M',
  perms: ''
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

const menuTree = computed(() => {
  const list = menuList.value
  const rootMenus = list.filter(m => m.parentId === 0)
  return buildTree(list, rootMenus)
})

const menuTreeForSelect = computed(() => {
  return [{ id: 0, menuName: '顶级菜单', children: menuTree.value }]
})

function buildTree(allMenus, parents) {
  return parents.map(parent => {
    const children = allMenus.filter(m => m.parentId === parent.id)
    return {
      ...parent,
      children: children.length > 0 ? buildTree(allMenus, children) : []
    }
  })
}

onMounted(() => { fetchList() })

async function fetchList() {
  loading.value = true
  try {
    const res = await listMenuApi()
    menuList.value = res.data
  } catch (e) {} finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.parentId = 0
  form.menuName = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sortOrder = 0
  form.isVisible = 1
  form.menuType = 'M'
  form.perms = ''
}

function handleAdd(parent) {
  resetForm()
  if (parent) form.parentId = parent.id
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await editMenuApi(form)
      ElMessage.success('修改成功')
    } else {
      await addMenuApi(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {} finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除菜单 "' + row.menuName + '" 吗？', '提示', { type: 'warning' })
    await removeMenuApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {}
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 图标选择触发器 */
.icon-select {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  min-width: 180px;
  height: 32px;
  transition: border-color 0.2s;
}

.icon-select:hover {
  border-color: #409EFF;
}

.icon-text {
  font-size: 13px;
  color: #606266;
}

.icon-clear {
  margin-left: auto;
  font-size: 14px;
  color: #909399;
}

.icon-clear:hover {
  color: #f56c6c;
}

/* 图标选择器工具栏 */
.icon-picker-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.icon-search {
  width: 100%;
}

/* 图标网格 */
.icon-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 420px;
  overflow-y: auto;
  padding: 4px 0;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
  border: 1px solid transparent;
}

.icon-item:hover {
  background: #ecf5ff;
  color: #409EFF;
  border-color: #d9ecff;
}

.icon-item.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

.icon-name {
  font-size: 10px;
  color: #909399;
  text-align: center;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-item.active .icon-name {
  color: #fff;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 14px;
}
</style>
