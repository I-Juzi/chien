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

          <!-- 通知铃铛 -->
          <el-popover placement="bottom" :width="320" trigger="click">
            <template #reference>
              <el-badge :value="notifyCount" :hidden="notifyCount === 0" :max="99" class="notify-badge">
                <el-icon class="notify-icon" @click="fetchNotices"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notify-header">
              <span style="font-weight: 600;">通知</span>
              <el-button type="primary" link size="small" @click="router.push('/system/notice')">查看全部</el-button>
            </div>
            <div v-if="notices.length === 0" style="color: #909399; text-align: center; padding: 20px;">
              暂无通知
            </div>
            <div v-else class="notify-list">
              <div v-for="item in notices" :key="item.id" class="notify-item" @click="openNotice(item)">
                <div class="notify-item-title">{{ item.title }}</div>
                <div class="notify-item-time">{{ item.createTime }}</div>
              </div>
            </div>
          </el-popover>

          <!-- 用户信息 -->
          <el-dropdown class="user-dropdown" @command="handleCommand">
            <span class="username">{{ userStore.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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

    <!-- 系统公告弹窗 -->
    <el-dialog v-model="announceVisible" title="系统公告" width="480px" :close-on-click-modal="false" @close="markAsRead(announceData?.id)">
      <div v-if="announceData">
        <div style="margin-bottom: 8px;">
          <el-tag :type="announceData.noticeType === 1 ? 'info' : 'warning'" size="small">
            {{ announceData.noticeType === 1 ? '通知' : '公告' }}
          </el-tag>
          <span style="color: #909399; font-size: 12px; margin-left: 8px;">{{ announceData.createTime }}</span>
        </div>
        <h3 style="margin: 8px 0 12px;">{{ announceData.title }}</h3>
        <el-divider style="margin: 0 0 12px;" />
        <div style="white-space: pre-wrap; line-height: 1.8; max-height: 300px; overflow-y: auto;">
          {{ announceData.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="closeAnnounceToday">今日不再提示</el-button>
        <el-button type="primary" @click="announceVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useTabsStore } from '@/stores/tabs'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, ArrowDown, Close, HomeFilled, Bell } from '@element-plus/icons-vue'
import { publishedNoticeApi } from '@/api/notice'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const tabsStore = useTabsStore()

const isCollapse = ref(false)
const contextMenu = ref({ visible: false, left: 0, top: 0, tab: null })
const notices = ref([])
const notifyCount = ref(0)
const announceVisible = ref(false)
const announceData = ref(null)
let eventSource = null

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

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  fetchNotices()
  connectSse()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
  disconnectSse()
})

// SSE 连接
function connectSse() {
  const token = getToken()
  if (!token) return
  disconnectSse()

  eventSource = new EventSource('/admin/sse/subscribe?token=' + token)

  eventSource.addEventListener('connected', () => {
    console.log('[SSE] 连接成功')
  })

  eventSource.addEventListener('notice', (event) => {
    try {
      const notice = JSON.parse(event.data)
      // 更新通知角标
      const readIds = JSON.parse(sessionStorage.getItem('chien_notice_read') || '[]')
      if (!readIds.includes(notice.id)) {
        notifyCount.value++
        notices.value.unshift(notice)
      }
      // 弹出提示，关闭后标记已读
      ElMessageBox.alert(notice.content, '新通知：' + notice.title, {
        confirmButtonText: '知道了'
      }).then(() => {
        markAsRead(notice.id)
      }).catch(() => {
        markAsRead(notice.id)
      })
    } catch (e) { console.error('[SSE] 解析通知失败', e) }
  })

  eventSource.onerror = () => {
    console.warn('[SSE] 连接断开，5秒后重连...')
    disconnectSse()
    setTimeout(connectSse, 5000)
  }
}

function disconnectSse() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

async function fetchNotices() {
  try {
    const res = await publishedNoticeApi()
    const allNotices = res.data || []
    const list = allNotices.filter(n => n.noticeType === 1)
    notices.value = list

    // 计算未读数量
    const readIds = JSON.parse(sessionStorage.getItem('chien_notice_read') || '[]')
    notifyCount.value = list.filter(n => !readIds.includes(n.id)).length

    // 检查是否需要弹出公告
    showAnnouncementIfNeed(allNotices)
  } catch (e) { /* handled */ }
}

function openNotice(item) {
  // 标记为已读
  const readIds = JSON.parse(sessionStorage.getItem('chien_notice_read') || '[]')
  if (!readIds.includes(item.id)) {
    readIds.push(item.id)
    sessionStorage.setItem('chien_notice_read', JSON.stringify(readIds))
    notifyCount.value = Math.max(0, notifyCount.value - 1)
  }

  ElMessageBox.alert(item.content, item.title, {
    confirmButtonText: '知道了',
    dangerouslyUseHTMLString: false
  })
}

function showAnnouncementIfNeed(allNotices) {
  // 检查今日是否已关闭提示
  const today = new Date().toISOString().slice(0, 10) // 2026-05-29
  const dismissedDate = sessionStorage.getItem('chien_notice_dismissed')
  if (dismissedDate === today) return

  // 取最新的已发布公告
  const latest = allNotices.find(n => n.status === 1)
  if (latest) {
    announceData.value = latest
    announceVisible.value = true
  }
}

function closeAnnounceToday() {
  const today = new Date().toISOString().slice(0, 10)
  sessionStorage.setItem('chien_notice_dismissed', today)
  markAsRead(announceData.value?.id)
  announceVisible.value = false
}

function markAsRead(id) {
  if (!id) return
  const readIds = JSON.parse(sessionStorage.getItem('chien_notice_read') || '[]')
  if (!readIds.includes(id)) {
    readIds.push(id)
    sessionStorage.setItem('chien_notice_read', JSON.stringify(readIds))
    notifyCount.value = Math.max(0, notifyCount.value - 1)
  }
}


async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/system/profile')
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

/* 通知铃铛 */
.notify-badge {
  cursor: pointer;
  flex-shrink: 0;
}

.notify-icon {
  font-size: 18px;
  color: #5a5e66;
  padding: 4px;
}

.notify-icon:hover {
  color: #409EFF;
}

.notify-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}

.notify-list {
  max-height: 300px;
  overflow-y: auto;
}

.notify-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.notify-item:last-child {
  border-bottom: none;
}

.notify-item:hover {
  background: #f5f7fa;
}

.notify-item-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notify-item-time {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
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
