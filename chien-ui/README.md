# Chien UI — 前端项目

Chien Admin 的前端模块，基于 Vue 3 + Element Plus 构建的后台管理系统前端。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| Vite | 8.0 | 构建工具 |
| Element Plus | 2.14 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.16 | HTTP 请求 |
| NProgress | 0.2 | 路由切换进度条 |
| Iconify | - | 图标库（通过 CDN 引入） |
| EventSource | - | SSE 客户端（浏览器原生） |

## 项目结构

```
chien-ui/
├── index.html                      # 入口 HTML（引入 Iconify CDN）
├── package.json
├── vite.config.js                  # Vite 配置（代理、别名）
├── .env                            # 开发环境变量
├── .env.production                 # 生产环境变量
├── .env.staging                    # 测试环境变量
└── src/
    ├── main.js                     # 应用入口（注册 Element Plus、Pinia、Router）
    ├── App.vue                     # 根组件
    ├── permission.js               # 路由权限守卫（登录校验、动态路由加载）
    │
    ├── api/                        # 接口请求模块
    │   ├── auth.js                 # 认证（登录、登出、用户信息、个人中心、Token 刷新）
    │   ├── user.js                 # 用户管理
    │   ├── role.js                 # 角色管理
    │   ├── menu.js                 # 菜单管理
    │   ├── dict.js                 # 数据字典
    │   ├── file.js                 # 文件上传与管理
    │   ├── notice.js               # 通知公告
    │   ├── online.js               # 在线用户
    │   ├── operlog.js              # 操作日志
    │   ├── loginlog.js             # 登录日志
    │   └── profile.js              # 个人中心
    │
    ├── router/
    │   └── index.js                # 静态路由配置（登录、404、首页、重定向）
    │
    ├── stores/                     # Pinia 状态管理
    │   ├── user.js                 # 用户状态（登录、Token、权限、菜单）
    │   ├── permission.js           # 路由权限（动态路由生成）
    │   └── tabs.js                 # 标签页管理
    │
    ├── utils/
    │   ├── request.js              # Axios 封装（拦截器、Token 刷新、错误处理）
    │   └── auth.js                 # Token 存取（访问令牌 + 刷新令牌）
    │
    ├── components/
    │   └── IconRenderer.vue        # 通用图标组件（支持 Element Plus + Iconify）
    │
    ├── layout/
    │   └── index.vue               # 主布局（侧边栏 + 标签页 + 通知铃铛 + 用户菜单）
    │
    └── views/
        ├── login/index.vue         # 登录页
        ├── 404.vue                 # 404 页面
        ├── dashboard/index.vue     # 首页（快捷入口 + 系统公告）
        ├── redirect/index.vue      # 路由重定向
        ├── system/                 # 系统管理
        │   ├── user/index.vue      # 用户管理
        │   ├── role/index.vue      # 角色管理（含权限分配）
        │   ├── menu/index.vue      # 菜单管理（含图标选择器）
        │   ├── dict/index.vue      # 数据字典
        │   ├── upload/index.vue    # 文件上传与管理
        │   ├── notice/index.vue    # 通知公告管理
        │   └── profile/index.vue   # 个人中心（编辑资料、修改密码、头像上传）
        └── monitor/                # 系统监控
            ├── online/index.vue    # 在线用户（强退操作）
            ├── operlog/index.vue   # 操作日志
            └── loginlog/index.vue  # 登录日志
```

## 环境变量

| 变量 | 说明 | 开发环境 | 生产环境 |
|------|------|----------|----------|
| `VITE_APP_TITLE` | 页面标题 | Chien Admin | Chien Admin |
| `VITE_APP_PORT` | 开发服务器端口 | 8082 | 80 |
| `VITE_APP_BASE_API` | API 基础路径 | /admin | /admin |
| `VITE_APP_API_TARGET` | 后端代理地址 | http://localhost:8080 | http://localhost:8080 |

## 快速启动

```bash
# 安装依赖
npm install

# 开发环境启动（端口 8082）
npm run dev

# 生产环境构建
npm run build

# 预览构建结果
npm run preview
```

## 核心机制

### 动态路由

1. 用户登录后，调用 `GET /auth/info` 获取菜单树
2. `permission.js` 中的 `filterAsyncRoutes` 将菜单树转为 Vue Router 路由配置
3. 通过 `router.addRoute()` 动态注册路由
4. 404 兜底路由放在最后注册，确保动态路由优先匹配

### 权限控制

- **路由级**：`permission.js` 路由守卫检查 Token 和菜单数据
- **按钮级**：通过 `permissions` 集合控制（后端返回，前端可用 `v-if` 判断）
- **菜单隐藏**：`is_visible=0` 的菜单生成路由但不在侧边栏显示

### Token 双令牌机制

- **访问令牌**（access token）：有效期 2 小时，用于接口认证
- **刷新令牌**（refresh token）：有效期 7 天，用于静默刷新访问令牌
- 存储位置：`localStorage`
- 刷新逻辑：`request.js` 响应拦截器捕获 401 → 自动调用 `/auth/refresh` → 换取新访问令牌 → 重试原请求

### SSE 实时通知

- 登录后自动连接 `/sse/subscribe?token=xxx`
- 监听 `notice` 事件，收到后弹窗提醒 + 铃铛角标 +1
- 断线自动重连（5 秒间隔）
- 已读状态通过 `sessionStorage` 记录

### 标签页导航

- 监听路由变化自动添加标签页
- 支持关闭当前、关闭其他、关闭右侧、关闭全部
- 右键菜单操作
- 首页标签固定不可关闭（affix）

### 图标系统

- **Element Plus 图标**：`ep:xxx` 格式（如 `ep:home-filled`）
- **Iconify 图标**：支持 Material Design、Font Awesome、Heroicons、Tabler、Carbon
- 通过 `IconRenderer` 组件统一渲染
- Iconify 通过 CDN `<script>` 引入，无需 npm 安装

## API 模块说明

| 模块 | 文件 | 接口前缀 | 说明 |
|------|------|----------|------|
| 认证 | auth.js | /auth | 登录、登出、用户信息、个人中心、Token 刷新 |
| 用户 | user.js | /system/user | 用户 CRUD、重置密码、分配角色 |
| 角色 | role.js | /system/role | 角色 CRUD、分配菜单权限 |
| 菜单 | menu.js | /system/menu | 菜单 CRUD、菜单树 |
| 字典 | dict.js | /dict | 字典类型和字典数据 CRUD |
| 文件 | file.js | /file | 文件上传、文件列表、删除 |
| 公告 | notice.js | /notice | 通知公告 CRUD |
| 在线 | online.js | /online | 在线用户列表、强退 |
| 操作日志 | operlog.js | /operlog | 操作日志查询、删除 |
| 登录日志 | loginlog.js | /loginlog | 登录日志查询、删除 |
| 个人中心 | profile.js | /auth/profile | 个人信息、修改密码 |

## 布局结构

```
┌──────────────────────────────────────────────┐
│  侧边栏   │          标签页导航栏              │
│           │  [折叠] [标签1][标签2] ... [铃铛] [用户名▼] │
│  首页     ├──────────────────────────────────────┤
│  系管理   │                                      │
│   ├用户   │                                      │
│   ├角色   │          主内容区域                   │
│   ├菜单   │         <router-view>                │
│   ├字典   │          + keep-alive                │
│   ├文件   │                                      │
│   └公告   │                                      │
│  系监控   │                                      │
│   ├在线   │                                      │
│   ├操作日志│                                     │
│   └登录日志│                                     │
└──────────────────────────────────────────────┘
```

## 环境要求

- Node.js 20.19+ 或 22.12+
- npm 9+
