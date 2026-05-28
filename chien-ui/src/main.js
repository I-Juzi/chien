import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import IconRenderer from './components/IconRenderer.vue'

// 全局样式
import './assets/main.css'

// 路由权限守卫（必须在 router 之后导入）
import './permission'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册通用图标组件
app.component('IconRenderer', IconRenderer)

// 告知 Vue 忽略 iconify-icon 自定义元素
app.config.compilerOptions.isCustomElement = (tag) => tag === 'iconify-icon'

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn, size: 'default' })

app.mount('#app')
