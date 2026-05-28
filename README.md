# Chien Admin — 后台管理系统

Chien Admin 是一个基于前后端分离架构的后台权限管理系统，采用 RBAC（基于角色的访问控制）模型，支持动态菜单、JWT 认证、在线用户管理、操作日志、数据字典等功能。

## 技术栈

### 后端（chien-admin）

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.5 | 应用框架 |
| Spring Security | - | 安全认证框架 |
| MyBatis-Plus | 3.5.13 | ORM 持久层框架 |
| JWT (jjwt) | 0.12.6 | Token 认证 |
| MySQL | - | 关系型数据库 |
| Redis | - | 在线用户会话存储、接口限流 |
| SpringDoc OpenAPI | 2.8.6 | Swagger API 文档 |
| Lombok | - | 简化代码 |
| Java | 17 | 开发语言 |

### 前端（chien-ui）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| Vite | 8.0 | 构建工具 |
| Element Plus | 2.14 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.16 | HTTP 请求 |
| Iconify | - | 图标库（Material、Font Awesome、Tabler 等） |

## 项目结构

```
chien/
├── pom.xml
├── HELP.md
├── chien-admin/                           # 后端模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/juzi/chien/admin/
│       │   ├── ChienAdminApplication.java
│       │   ├── common/
│       │   │   ├── Result.java              # 统一返回结果
│       │   │   ├── BusinessException.java    # 自定义业务异常
│       │   │   ├── ErrorCode.java            # 错误码枚举
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── Log.java                  # 操作日志注解
│       │   │   ├── BusinessType.java         # 业务操作类型枚举
│       │   │   └── RateLimit.java            # 接口限流注解
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   └── OpenApiConfig.java        # Swagger 配置
│       │   ├── aspect/
│       │   │   ├── LogAspect.java            # 操作日志 AOP 切面
│       │   │   └── RateLimitAspect.java      # 接口限流 AOP 切面
│       │   ├── filter/
│       │   │   ├── XssFilter.java            # XSS 过滤器
│       │   │   └── XssHttpServletRequestWrapper.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── UserController.java
│       │   │   ├── RoleController.java
│       │   │   ├── MenuController.java
│       │   │   ├── OnlineUserController.java
│       │   │   ├── SysOperLogController.java
│       │   │   ├── SysLoginLogController.java
│       │   │   ├── SysDictTypeController.java
│       │   │   └── SysDictDataController.java
│       │   ├── domain/
│       │   │   ├── entity/
│       │   │   │   ├── SysUser.java
│       │   │   │   ├── SysRole.java
│       │   │   │   ├── SysMenu.java
│       │   │   │   ├── SysUserRole.java
│       │   │   │   ├── SysRoleMenu.java
│       │   │   │   ├── SysOperLog.java
│       │   │   │   ├── SysLoginLog.java
│       │   │   │   ├── SysDictType.java
│       │   │   │   └── SysDictData.java
│       │   │   └── vo/
│       │   │       ├── LoginVO.java
│       │   │       ├── MenuTreeVO.java
│       │   │       └── OnlineUserVO.java
│       │   ├── mapper/
│       │   ├── security/
│       │   │   ├── JwtUtils.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── LoginUser.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── service/
│       │       ├── OnlineUserService.java
│       │       ├── SysOperLogService.java
│       │       ├── SysLoginLogService.java
│       │       ├── SysDictTypeService.java
│       │       ├── SysDictDataService.java
│       │       └── impl/
│       └── resources/
│           ├── application.yml
│           └── mapper/
└── chien-ui/                              # 前端模块
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── permission.js                  # 路由权限守卫
        ├── components/
        │   └── IconRenderer.vue            # 通用图标组件（支持 Element Plus + Iconify）
        ├── api/
        │   ├── auth.js
        │   ├── user.js
        │   ├── role.js
        │   ├── menu.js
        │   ├── online.js
        │   ├── operlog.js
        │   ├── loginlog.js
        │   └── dict.js
        ├── router/index.js
        ├── stores/
        │   ├── user.js
        │   ├── permission.js
        │   └── tabs.js
        ├── layout/index.vue
        ├── utils/
        │   ├── request.js
        │   └── auth.js
        └── views/
            ├── login/
            ├── dashboard/
            ├── 404.vue
            ├── redirect/
            ├── system/
            │   ├── user/
            │   ├── role/
            │   ├── menu/
            │   └── dict/                  # 数据字典
            └── monitor/
                ├── online/                # 在线用户
                ├── operlog/               # 操作日志
                └── loginlog/              # 登录日志
```

## 数据库设计

### 表结构（共 10 张表）

**RBAC 权限表**

| 表名 | 说明 |
|------|------|
| `chien-user` | 用户表 |
| `chien-role` | 角色表 |
| `chien-menu` | 菜单权限表 |
| `chien-user_role` | 用户-角色关联表 |
| `chien-role_menu` | 角色-菜单关联表 |

**业务功能表**

| 表名 | 说明 |
|------|------|
| `chien-oper_log` | 操作日志表 |
| `chien-login_log` | 登录日志表 |
| `chien-dict_type` | 字典类型表 |
| `chien-dict_data` | 字典数据表 |
| `chien-online_user` | （Redis 实现，不建表）在线用户会话 |

### ER 关系

```
chien-user  ←——→  chien-user_role  ←——→  chien-role
                                                ↑
                                        chien-role_menu
                                                ↑
                                          chien-menu

chien-dict_type  ←——→  chien-dict_data（通过 dict_type 关联）
```

### 菜单类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| M | 目录（顶级菜单分组） | 系统管理、系统监控、系统工具 |
| C | 菜单（可访问的页面） | 用户管理、角色管理、在线用户 |
| F | 按钮（页面内操作权限） | 用户查询、用户新增、强退用户 |

## 功能模块

### 1. 认证登录

- 基于 Spring Security + JWT 的无状态认证
- 登录返回 Token，前端存入 localStorage
- 请求拦截器自动携带 Token（`Authorization: Bearer xxx`）
- Token 有效期 24 小时
- 登录接口限流：同一 IP 60 秒内最多 5 次请求，防暴力破解
- 自动记录登录日志（IP、浏览器、操作系统、成功/失败）

### 2. 权限管理

- **用户管理**：增删改查用户，分配角色，重置密码
- **角色管理**：增删改查角色，分配菜单权限
- **菜单管理**：管理菜单树结构，配置路由和权限标识，可视化图标选择器
- **动态路由**：登录后根据用户角色返回的菜单树，前端动态生成路由
- **权限控制**：基于 `@PreAuthorize` 注解的方法级别权限校验

### 3. 在线用户管理

- 基于 Redis 存储在线会话，30 分钟无活动自动过期
- 管理员可查看所有在线用户（用户名、IP、登录时间、最后活跃时间）
- 支持强退指定用户（删除 Redis 会话，Token 立即失效）
- 支持按用户踢出所有设备会话

### 4. 操作日志

- 基于 AOP + 自定义 `@Log` 注解，零侵入记录操作日志
- 记录内容：操作人、IP、请求参数、返回结果、耗时、异常信息
- 异步写入数据库，不影响主请求性能
- 支持按模块标题、业务类型、状态搜索
- 支持查看详情（格式化 JSON 参数和返回值）

### 5. 登录日志

- 自动记录每次登录的用户名、IP、浏览器、操作系统
- 记录登录成功和失败（含失败原因）
- 支持按用户名、状态搜索和清空

### 6. 数据字典

- 字典类型 + 字典数据两级管理
- 支持前端通过接口动态获取下拉选项，无需硬编码
- 预置示例：用户性别、系统状态、菜单类型
- 接口：`GET /dict/data/type/{dictType}`

### 7. 自定义业务异常

- `BusinessException` 统一业务异常类，带错误码
- `ErrorCode` 枚举集中管理错误码（通用、用户、角色、菜单、字典、文件等模块）
- 全局异常处理器自动捕获并返回统一格式

### 8. 接口限流

- 基于 Redis + Lua 脚本的滑动窗口限流
- 自定义 `@RateLimit` 注解，支持按 IP 或用户维度
- 可配置时间窗口和最大请求数
- 已加到登录接口，其他接口按需添加

### 9. XSS 过滤

- 全局 `XssFilter` 自动拦截所有请求
- 覆盖 URL 参数、表单参数、Header、JSON 请求体（`@RequestBody`）
- 清洗规则：转义尖括号，移除 `<script>`、`javascript:`、`on事件=`、`eval()` 等危险模式

### 10. 前端特性

- **动态菜单**：根据后端返回的菜单树自动生成侧边栏
- **标签页导航**：浏览器标签式体验，支持关闭、切换、右键菜单、批量关闭
- **Keep-Alive**：页面切换保留状态
- **响应式布局**：侧边栏可折叠
- **图标选择器**：支持 Element Plus、Material Design、Font Awesome、Heroicons、Tabler、Carbon 六大图标集

## API 文档

项目集成了 SpringDoc OpenAPI（Swagger），启动后端后访问：

```
http://localhost:8080/admin/swagger-ui.html
```

无需登录即可查看所有接口文档。页面右上角「Authorize」按钮可输入 JWT Token 测试需认证的接口。

## 接口列表

### 认证相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/admin/auth/login` | 用户登录（限流：5次/分钟） | 否 |
| GET | `/admin/auth/info` | 获取当前用户信息 | 是 |
| POST | `/admin/auth/logout` | 退出登录 | 是 |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/system/user/list` | 用户列表 |
| GET | `/admin/system/user/{userId}` | 用户详情 |
| POST | `/admin/system/user` | 新增用户 |
| PUT | `/admin/system/user` | 修改用户 |
| DELETE | `/admin/system/user/{userIds}` | 删除用户 |
| PUT | `/admin/system/user/resetPwd` | 重置密码 |
| PUT | `/admin/system/user/changeRole` | 分配角色 |

### 角色管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/system/role/list` | 角色列表 |
| POST | `/admin/system/role` | 新增角色 |
| PUT | `/admin/system/role` | 修改角色 |
| DELETE | `/admin/system/role/{roleIds}` | 删除角色 |
| PUT | `/admin/system/role/changeMenu` | 分配菜单权限 |

### 菜单管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/system/menu/list` | 菜单列表 |
| GET | `/admin/system/menu/tree` | 当前用户菜单树 |
| POST | `/admin/system/menu` | 新增菜单 |
| PUT | `/admin/system/menu` | 修改菜单 |
| DELETE | `/admin/system/menu/{menuIds}` | 删除菜单 |

### 在线用户

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/online/list` | 在线用户列表 |
| DELETE | `/admin/online/kick/{token}` | 强退用户（按 Token） |
| DELETE | `/admin/online/kick/user/{userId}` | 强退用户（按用户ID） |

### 操作日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/operlog/list` | 分页查询 |
| GET | `/admin/operlog/{id}` | 日志详情 |
| DELETE | `/admin/operlog/{id}` | 删除日志 |
| DELETE | `/admin/operlog/clean` | 清空日志 |

### 登录日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/loginlog/list` | 分页查询 |
| DELETE | `/admin/loginlog/{id}` | 删除日志 |
| DELETE | `/admin/loginlog/clean` | 清空日志 |

### 字典管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/dict/type/list` | 字典类型列表 |
| POST | `/admin/dict/type` | 新增字典类型 |
| PUT | `/admin/dict/type` | 修改字典类型 |
| DELETE | `/admin/dict/type/{id}` | 删除字典类型 |
| GET | `/admin/dict/data/list` | 字典数据列表 |
| GET | `/admin/dict/data/type/{dictType}` | 按类型查询字典数据 |
| POST | `/admin/dict/data` | 新增字典数据 |
| PUT | `/admin/dict/data` | 修改字典数据 |
| DELETE | `/admin/dict/data/{id}` | 删除字典数据 |

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 20.19+ 或 22.12+

## 快速启动

### 后端

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE chien DEFAULT CHARACTER SET utf8mb4;"

# 2. 导入表结构和初始数据（SQL 文件待提供）

# 3. 修改配置
#    编辑 chien-admin/src/main/resources/application.yml
#    配置 MySQL 和 Redis 连接信息

# 4. 启动
cd chien-admin
mvn spring-boot:run
```

后端启动后运行在 `http://localhost:8080/admin`，Swagger 文档地址：`http://localhost:8080/admin/swagger-ui.html`

### 前端

```bash
cd chien-ui
npm install
npm run dev
```

前端启动后运行在 `http://localhost:8082`，开发环境自动代理 `/admin` 请求到后端。

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

## 配置说明

### 后端 application.yml 关键配置

```yaml
server:
  port: 8080                    # 服务端口
  servlet:
    context-path: /admin        # 接口前缀

spring:
  datasource:                   # MySQL 连接
    url: jdbc:mysql://127.0.0.1:3306/chien
    username: root
    password: txzk1688
  data:
    redis:                      # Redis 连接
      host: 127.0.0.1
      port: 6379

jwt:
  secret: chien-admin-secret-key-must-be-at-least-256-bits-long!!
  expiration: 86400000          # Token 有效期（24小时）

springdoc:
  api-docs:
    path: /v3/api-docs          # API 文档路径
  swagger-ui:
    path: /swagger-ui.html      # Swagger UI 路径
```

### 前端 .env 配置

```
VITE_APP_PORT = 8082                            # 开发端口
VITE_APP_BASE_API = /admin                      # API 前缀
VITE_APP_API_TARGET = http://localhost:8080      # 后端地址
```

### 限流注解使用示例

```java
// 登录接口：同一 IP 60 秒内最多 5 次
@RateLimit(key = "login", count = 5, period = 60, dimension = "ip")

// 按用户限流：同一用户 10 秒内最多 3 次
@RateLimit(count = 3, period = 10, dimension = "user")
```

### 操作日志注解使用示例

```java
// 标记需要记录日志的方法
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public Result<Void> add(@RequestBody SysUser user) { ... }

@Log(title = "用户管理", businessType = BusinessType.DELETE)
@DeleteMapping("/{id}")
public Result<Void> remove(@PathVariable Long id) { ... }
```

### 自定义业务异常使用示例

```java
// 直接抛出
throw new BusinessException("用户名已存在");

// 使用错误码枚举
throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);

// 枚举 + 自定义消息
throw new BusinessException(ErrorCode.USER_DISABLED, "用户 admin 已被停用");
```
