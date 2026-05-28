import router, { addAddedRouteName } from '@/router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 白名单路由（不需要登录即可访问）
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，跳转到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      const userStore = useUserStore()
      const permissionStore = usePermissionStore()

      if (userStore.menus.length > 0) {
        // 已有用户信息（含菜单数据）
        next()
      } else {
        try {
          // 获取用户信息
          await userStore.getInfo()

          // 根据用户菜单动态生成路由
          const accessRoutes = await permissionStore.generateRoutes(userStore.menus)

          // 动态添加路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
            addAddedRouteName(route.name)
            if (route.children) {
              route.children.forEach(child => {
                addAddedRouteName(child.name)
              })
            }
          })

          // 添加 404 路由（放在最后）
          router.addRoute({ path: '/:pathMatch(.*)*', name: 'NotFound', redirect: '/404', meta: { hidden: true } })

          // 使用 replace 确保 addRoute 生效
          next({ ...to, replace: true })
        } catch (error) {
          // 获取用户信息失败，清除 token 并跳转登录
          console.error('获取用户信息失败:', error)
          userStore.resetToken()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
