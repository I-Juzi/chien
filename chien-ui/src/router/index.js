import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('@/layout/index.vue')

// 静态路由
const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/redirect',
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/404',
    component: () => import('@/views/404.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      }
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 用于存储动态添加的路由名称
const addedRouteNames = []

// 重置路由（用于退出登录时）
export function resetRouter() {
  addedRouteNames.forEach(name => {
    router.removeRoute(name)
  })
  // 移除兜底的 404 路由
  router.removeRoute('NotFound')
  addedRouteNames.length = 0
}

// 记录动态添加的路由
export function addAddedRouteName(name) {
  addedRouteNames.push(name)
}

export { constantRoutes }
export default router
