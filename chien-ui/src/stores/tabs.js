import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 标签页管理 Store
 * 类似浏览器标签页，支持打开、关闭、切换
 */
export const useTabsStore = defineStore('tabs', () => {
  // 已打开的标签页列表
  const tabs = ref([
    { path: '/dashboard', title: '首页', affix: true }
  ])

  // 当前激活的标签路径
  const activeTab = ref('/dashboard')

  /**
   * 添加标签页（已存在则只激活）
   */
  function addTab(tab) {
    activeTab.value = tab.path
    const exists = tabs.value.some(t => t.path === tab.path)
    if (!exists) {
      tabs.value.push({
        path: tab.path,
        title: tab.title,
        icon: tab.icon,
        affix: tab.affix || false
      })
    }
  }

  /**
   * 关闭标签页，返回下一个应该激活的路径
   */
  function removeTab(targetPath) {
    const index = tabs.value.findIndex(t => t.path === targetPath)
    if (index === -1) return activeTab.value

    // 不允许关闭固定的标签
    if (tabs.value[index].affix) return activeTab.value

    tabs.value.splice(index, 1)

    // 如果关闭的是当前激活标签，切换到相邻标签
    if (activeTab.value === targetPath) {
      const nextTab = tabs.value[index] || tabs.value[index - 1]
      activeTab.value = nextTab ? nextTab.path : '/dashboard'
    }

    return activeTab.value
  }

  /**
   * 关闭其他标签页
   */
  function closeOtherTabs(targetPath) {
    tabs.value = tabs.value.filter(t => t.affix || t.path === targetPath)
    if (!tabs.value.some(t => t.path === activeTab.value)) {
      activeTab.value = targetPath
    }
  }

  /**
   * 关闭右侧标签页
   */
  function closeRightTabs(targetPath) {
    const index = tabs.value.findIndex(t => t.path === targetPath)
    if (index === -1) return
    tabs.value = tabs.value.filter((t, i) => i <= index || t.affix)
    if (!tabs.value.some(t => t.path === activeTab.value)) {
      activeTab.value = tabs.value[tabs.value.length - 1].path
    }
  }

  return {
    tabs,
    activeTab,
    addTab,
    removeTab,
    closeOtherTabs,
    closeRightTabs
  }
})
