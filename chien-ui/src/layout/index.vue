<template>
  <div class="app-wrapper">
    <el-container class="app-container">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
          <span v-show="!isCollapse" class="logo-title">Chien Admin</span>
        </div>
        <el-scrollbar>
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :collapse-transition="false"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            @select="handleMenuSelect"
          >
            <!-- 首页 - 硬编码 -->
            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <template #title>首页</template>
            </el-menu-item>

            <!-- 动态菜单 -->
            <template v-for="item in menuRoutes" :key="item.path">
              <el-sub-menu :index="item.path">
                <template #title>
                  <icon-renderer v-if="item.meta?.icon" :icon="item.meta.icon" :size="18" />
                  <span>{{ item.meta?.title }}</span>
                </template>

                <template v-for="child in item.children" :key="child.path">
                  <template v-if="!child.meta?.hidden">
                    <template v-if="!child.children || child.children.length === 0">
                      <el-menu-item :index="child.path">
                        <icon-renderer v-if="child.meta?.icon" :icon="child.meta.icon" :size="18" />
                        <template #title>{{ child.meta?.title }}</template>
                      </el-menu-item>
                    </template>
                    <el-sub-menu v-else :index="child.path">
                      <template #title>
                        <icon-renderer v-if="child.meta?.icon" :icon="child.meta.icon" :size="18" />
                        <span>{{ child.meta?.title }}</span>
                      </template>
                      <el-menu-item
                        v-for="grandChild in child.children"
                        :key="grandChild.path"
                        :index="grandChild.path"
                      >
                        <template #title>{{ grandChild.meta?.title }}</template>
                      </el-menu-item>
                    </el-sub-menu>
                  </template>
                </template>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-container class="main-container">
        <!-- 标签页导航栏（含折叠按钮和用户信息） -->
        <div class="tabs-bar">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <div class="tabs-wrapper">
            <div
              v-for="tab in tabsStore.tabs"
              :key="tab.path"
              class="tab-item"
              :class="{ active: tabsStore.activeTab === tab.path }"
              @click="handleTabClick(tab)"
              @contextmenu.prevent="openContextMenu($event, tab)"
            >
              <span class="tab-title">{{ tab.title }}</span>
              <el-icon
                v-if="!tab.affix"
                class="tab-close"
                @click.stop="handleTabClose(tab.path)"
              >
                <Close />
              </el-icon>
            </div>
          </div>

          <!-- 标签操作 -->
          <el-dropdown class="tabs-action" @command="handleTabsAction" trigger="click">
            <el-icon class="tabs-action-btn"><ArrowDown /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="closeOther">关闭其他</el-dropdown-item>
                <el-dropdown-item command="closeRight">关闭右侧</el-dropdown-item>
                <el-dropdown-item command="closeAll">关闭全部</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 用户信息 -->
          <el-dropdown class="user-dropdown" @command="handleCommand">
            <span class="username">{{ userStore.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 右键菜单 -->
        <ul
          v-show="contextMenu.visible"
          class="context-menu"
          :style="{ left: contextMenu.left + 'px', top: contextMenu.top + 'px' }"
        >
          <li v-if="!contextMenu.tab?.affix" @click="handleTabClose(contextMenu.tab?.path)">关闭</li>
          <li @click="handleTabsAction('closeOther')">关闭其他</li>
          <li @click="handleTabsAction('closeRight')">关闭右侧</li>
        </ul>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <keep-alive>
                <component :is="Component" />
              </keep-alive>
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useTabsStore } from '@/stores/tabs'
import { Fold, Expand, ArrowDown, Close, HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const tabsStore = useTabsStore()

const isCollapse = ref(false)
const contextMenu = ref({ visible: false, left: 0, top: 0, tab: null })

const activeMenu = computed(() => route.path)

// 侧边栏菜单：只保留有多级子菜单的顶级目录（排除首页 "/" 路由）
const menuRoutes = computed(() => {
  return permissionStore.routes.filter(r => {
    if (r.meta?.hidden) return false
    // 排除 "/" 路由（首页已硬编码）
    if (r.path === '/') return false
    // 只显示有子菜单的目录
    return r.children && r.children.length > 0
  })
})

// 监听路由变化，自动添加标签页
watch(
  () => route.path,
  (path) => {
    if (path === '/login' || path === '/404') return
    const title = route.meta?.title || '未命名页面'
    const icon = route.meta?.icon
    const affix = path === '/dashboard'
    tabsStore.addTab({ path, title, icon, affix })
  },
  { immediate: true }
)

function handleMenuSelect(index) {
  if (index && index !== route.path) {
    router.push(index).catch(() => {})
  }
}

function handleTabClick(tab) {
  if (tab.path !== route.path) {
    router.push(tab.path)
  }
}

function handleTabClose(targetPath) {
  const nextPath = tabsStore.removeTab(targetPath)
  if (nextPath !== route.path) {
    router.push(nextPath)
  }
  closeContextMenu()
}

function handleTabsAction(command) {
  const currentPath = route.path
  if (command === 'closeOther') {
    tabsStore.closeOtherTabs(currentPath)
  } else if (command === 'closeRight') {
    tabsStore.closeRightTabs(currentPath)
  } else if (command === 'closeAll') {
    tabsStore.closeOtherTabs('/dashboard')
    if (route.path !== '/dashboard') {
      router.push('/dashboard')
    }
  }
  closeContextMenu()
}

function openContextMenu(e, tab) {
  contextMenu.value = { visible: true, left: e.clientX, top: e.clientY, tab }
}

function closeContextMenu() {
  contextMenu.value.visible = false
}

function handleDocumentClick() {
  closeContextMenu()
}

onMounted(() => document.addEventListener('click', handleDocumentClick))
onBeforeUnmount(() => document.removeEventListener('click', handleDocumentClick))

async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100%;
}

.app-container {
  height: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: #2b2f3a;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-title {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  margin-left: 10px;
  white-space: nowrap;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 标签页导航栏 ========== */
.tabs-bar {
  height: 40px;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  padding: 0 8px;
  flex-shrink: 0;
  gap: 6px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: #5a5e66;
  flex-shrink: 0;
  padding: 4px;
}

.collapse-btn:hover {
  color: #409EFF;
}

.tabs-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  overflow-x: auto;
  overflow-y: hidden;
  gap: 4px;
  scrollbar-width: none;
}

.tabs-wrapper::-webkit-scrollbar {
  display: none;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #d8dce5;
  border-radius: 3px;
  background: #fff;
  color: #495060;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: all 0.2s;
  user-select: none;
}

.tab-item:hover {
  color: #409EFF;
  border-color: #409EFF;
}

.tab-item.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

.tab-item.active .tab-close:hover {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.tab-title {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-close {
  font-size: 12px;
  border-radius: 50%;
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-close:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #f56c6c;
}

.tabs-action {
  flex-shrink: 0;
}

.tabs-action-btn {
  font-size: 14px;
  cursor: pointer;
  color: #5a5e66;
  padding: 4px;
  border-radius: 3px;
}

.tabs-action-btn:hover {
  background: #f0f2f5;
  color: #409EFF;
}

/* 用户信息 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0 4px;
}

.username {
  font-size: 13px;
  color: #5a5e66;
}

.dropdown-icon {
  font-size: 12px;
  color: #5a5e66;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 3000;
  list-style: none;
  padding: 4px 0;
  margin: 0;
  background: #fff;
  border-radius: 4px;
  box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.15);
  font-size: 12px;
}

.context-menu li {
  padding: 6px 16px;
  cursor: pointer;
  color: #495060;
  white-space: nowrap;
}

.context-menu li:hover {
  background: #f0f2f5;
  color: #409EFF;
}

/* 主内容区 */
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 侧边栏菜单样式调整 */
.sidebar-container :deep(.el-menu) {
  border-right: none;
}

.sidebar-container :deep(.el-menu-item.is-active) {
  background-color: #263445 !important;
}
</style>
