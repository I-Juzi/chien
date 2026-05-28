import { defineStore } from 'pinia'
import { ref } from 'vue'
import { constantRoutes, resetRouter, addAddedRouteName } from '@/router'

const Layout = () => import('@/layout/index.vue')

// 动态导入 views 下的所有 .vue 文件
const viewModules = import.meta.glob('@/views/**/*.vue')

/**
 * 获取组件
 * @param {string} componentPath 组件路径，如 system/user/index
 */
function getComponent(componentPath) {
  const key = `/src/views/${componentPath}.vue`
  const fn = viewModules[key]
  if (!fn) {
    console.warn(`[Router] 组件未找到: ${key}`)
  }
  return fn
}

/**
 * 根据后端返回的菜单树生成路由
 * 后端返回的树形结构：顶级菜单 path 已带 / (如 /system)，子菜单 path 不带 / (如 user)
 *
 * @param {Array} menus 后端返回的菜单树
 * @param {string} parentPath 父级完整路径
 * @returns {Array} 路由配置数组
 */
function filterAsyncRoutes(menus, parentPath = '') {
  const routes = []

  menus.forEach(menu => {
    // 按钮权限，不生成路由
    if (menu.menuType === 'F') return

    // 计算完整路径
    const fullPath = parentPath
      ? `${parentPath}/${menu.path}`
      : menu.path  // 顶级菜单的 path 已包含 / 如 /system

    const route = {
      path: fullPath,
      name: menu.menuName + '_' + menu.id,
      meta: {
        title: menu.menuName,
        icon: menu.icon,
        hidden: menu.isVisible === 0
      }
    }

    // 顶级目录 → 使用 Layout 组件
    if (menu.parentId === 0) {
      route.component = Layout

      if (menu.children && menu.children.length > 0) {
        // 递归处理子菜单（传递当前完整路径作为 parentPath）
        route.children = filterAsyncRoutes(menu.children, fullPath)

        // 重定向到第一个非按钮子菜单
        const firstChild = menu.children.find(c => c.menuType !== 'F')
        if (firstChild) {
          route.redirect = fullPath + '/' + firstChild.path
        }
      }
    } else {
      // 子菜单页面 → 使用 views 下的组件
      if (menu.component) {
        const componentFn = getComponent(menu.component)
        if (componentFn) {
          route.component = componentFn
        }
      }
      // 如果有子菜单（三级或更多）
      if (menu.children && menu.children.length > 0) {
        route.children = filterAsyncRoutes(menu.children, fullPath)
      }
    }

    routes.push(route)
  })

  return routes
}

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref(constantRoutes)
  const addRoutes = ref([])

  /**
   * 根据后端菜单生成前端路由
   */
  function generateRoutes(menus) {
    return new Promise((resolve) => {
      const asyncRoutes = filterAsyncRoutes(menus)
      addRoutes.value = asyncRoutes
      routes.value = constantRoutes.concat(asyncRoutes)
      resolve(asyncRoutes)
    })
  }

  /**
   * 重置路由
   */
  function resetRoutes() {
    routes.value = constantRoutes
    addRoutes.value = []
    resetRouter()
  }

  return { routes, addRoutes, generateRoutes, resetRoutes }
})
