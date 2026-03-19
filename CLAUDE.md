# ONE家项目计划看板 - Claude 上下文

## 项目概述

内部项目需求看板系统（ONE家项目计划看板），供信息部管理多部门需求，实现进度透明化。

- **代码仓库**：https://github.com/yangxt127981/dashboard（当前版本: V5.3）
- **本地路径**：/Users/hansonyang/Project/dashboard
- **生产地址**：http://dashboard.meione.cc（阿里云 47.103.56.254）

---

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.3 + MyBatis + PageHelper |
| 语言 | Java 17（本机默认 Java 25，启动必须手动指定 Java 17）|
| 数据库 | MySQL，库名 `dashboard_db`，本地 root 无密码 |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router 4 + Axios |

---

## 启动方式

```bash
# 1. 启动 MySQL（每次重启电脑后需执行）
mysqld_safe --datadir=/opt/homebrew/var/mysql &

# 2. 启动后端（必须指定 Java 17）
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
cd /Users/hansonyang/Project/dashboard/backend
mvn spring-boot:run

# 3. 启动前端
cd /Users/hansonyang/Project/dashboard/frontend
npm run dev
```

- 后端：http://localhost:8080
- 前端：http://localhost:5173

---

## 内置账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | ADMIN（增删改查）|
| user | user123 | USER（只读）|
| IOA 普通用户 | IOA_SSO（自动创建）| USER（只读）|
| IOA 指定用户 | IOA_SSO（自动升级）| MANAGER（增改查，无删除）|

**MANAGER 角色 IOA 用户列表**（硬编码在 `AuthController.java`，大小写不敏感）：
`yangxiaotong`、`zhaoyiqun`、`dingying`、`liuqiushi`、`M81496`、`M20506`、`M00828`

---

## 已实现功能（V5.3 完整版）

### 登录鉴权
- **账号密码登录**：POST `/api/auth/login`，返回 UUID Token
- **IOA 一键登录**：POST `/api/auth/ioa/login`
  - 前端从本机 IOA 客户端（`sso.wawo.cc:54339`）拿 Ticket
  - 后端调 IOA CheckTicket API（`ioa.wawo.cc:27800`）验证
  - 首次登录自动创建 USER 角色账号
  - 特定 IOA 用户自动升级为 MANAGER 角色
- **登出**：POST `/api/auth/logout`，Token 失效
- Token 存内存 `ConcurrentHashMap`，由 `AuthInterceptor` 拦截所有 `/api/**`（除 `/api/auth/**`）
- **RBAC 权限**：登录时计算权限集合随 token 一起返回 `permissions[]`，前端存 Pinia + localStorage；前端按钮/菜单 v-if 隐藏 + 后端接口双重校验

### 需求列表
- 分页（PageHelper），默认每页 10 条
- **Tab 页**：全部 / 进行中（设计中/开发中/测试中）/ 未开始 / 已上线 / 已取消，Tab 页签实时显示各状态记录数量
- **筛选**：需求名称模糊搜索、部门下拉（仅限下拉选取）、所属模块筛选、优先级多选、状态多选（仅"全部"Tab 可用）、产品对接人模糊搜索；高级查询可折叠/展开
- **排序**：优先级（紧急→高→中→低）、计划开始/完成时间、实际开始/完成时间、创建时间、更新时间（升降序），后端全局排序
- **列配置**：可隐藏/显示列，配置持久化到 `localStorage`
- 列表右上角有刷新按钮（Icon 形式）

### 需求字段
需求名称、所属模块、需求方部门、需求对接人、产品对接人、优先级、计划开始、计划完成、实际开始、实际完成、状态、需求描述、创建时间、更新时间

优先级枚举：`紧急 / 高 / 中 / 低`
状态枚举：`未开始 / 设计中 / 开发中 / 测试中 / 已上线 / 已取消`

产品对接人选项（硬编码在 Board.vue `productOwnerOptions`）：`刘秋诗`、`赵轶群`、`丁滢`、`Hanson`、`张明洋`

需求方部门：从 `sys_department` 数据库动态加载，**只能从下拉选项选取，不允许自定义输入**

### 需求 CRUD
权限由 `sys_permission` 权限点控制，前端 v-if 隐藏 + 后端接口校验：
- 新增需求：需 `requirement:create` 权限
- 编辑需求：需 `requirement:edit` 权限
- 取消需求：需 `requirement:cancel` 权限
- 删除需求：需 `requirement:delete` 权限（仅 ADMIN 内置此权限）
- 需求名称（`functionName`）有非空校验，空值返回 400

### 图片附件
- 上传：POST `/api/upload`，保存到 `uploads/` 目录
- 限制：最多 **5 张**，仅支持 **PNG、JPG/JPEG** 格式，单文件 ≤ 10MB
- 前端：`beforeUpload` 校验格式和数量，达 5 张隐藏上传按钮；支持 **Ctrl+V / Cmd+V 粘贴图片直接上传**（弹窗打开时监听 `paste` 事件）
- 后端：校验 ContentType 仅允许 `image/png`、`image/jpeg`
- 附件关联需求：`attachment` 表，支持多附件
- 接口：GET/POST `/api/requirements/{id}/attachments`，DELETE `/api/attachments/{id}`
- 生产 Nginx 配置 `client_max_body_size 10m`，Spring Boot `max-file-size: 10MB`

### 操作日志
- 每次创建、编辑、删除自动记录日志
- 日志含：操作人、操作类型、变更前/后内容（JSON）
- 接口：GET `/api/requirements/{id}/logs`

### 数据统计看板
- 折叠/展开面板
- 三维度饼图：部门分布、优先级分布、状态分布
- 三图联动：点击部门图，另两图随之过滤；再次点击取消联动

### 权限管理（V5.2）
基于 RBAC 模型，内置三个角色权限硬编码不可修改，支持创建自定义角色。

**内置角色权限**（`PermissionService.java` 硬编码，不走数据库）：
| 角色 | 权限 |
|------|------|
| ADMIN | 全部 27 个权限点 |
| MANAGER | `board:view`、`requirement:view/create/edit/cancel` |
| USER | `board:view`、`requirement:view` |

**权限点总览**（共 27 个）：

| 菜单 | 子权限点 |
|------|---------|
| `board:view` 看板 | `requirement:view` 查看需求、`requirement:create` 新增、`requirement:edit` 编辑、`requirement:cancel` 取消、`requirement:delete` 删除 |
| `system:dept` 需求方部门 | `dept:view` 查看、`dept:create` 新增、`dept:edit` 编辑、`dept:delete` 删除 |
| `system:module` 需求模块 | `module:view` 查看、`module:create` 新增、`module:edit` 编辑、`module:delete` 删除 |
| `system:user` 用户管理 | `user:view` 查看、`user:create` 新增、`user:edit` 编辑、`user:delete` 删除 |
| `system:login-log` 登录日志 | — |
| `system:role` 角色管理 | `role:create` 新增、`role:edit` 编辑、`role:delete` 删除 |

**关键设计**：
- 自定义角色权限存 `sys_role_permission` 表（只存叶子节点 BUTTON 权限 ID）
- `findCodesByRoleId` 用 UNION 查询自动补上父级 MENU code，无需额外存储
- 角色管理页：内置角色权限树禁用，不可修改
- `dept:view` / `module:view` 仅控制管理弹窗的表格显示，GET 列表接口对所有登录用户开放（用于筛选下拉框）

**用户与角色关联**：
- 内置角色：`user.role` 字段存 `ADMIN/MANAGER/USER`
- 自定义角色：`user.role` 为 NULL，`user.role_id` 关联 `sys_role.id`

### 系统管理
- **需求方部门维护**：部门字典增删改，排序可配置，`sys_department.name` 有唯一约束
- **需求模块维护**：模块字典增删改，支持背景色配置
- **用户管理**：非 IOA 用户增删改，可设置内置角色或自定义角色，不可删除当前登录账号
- **登录日志**：记录账号名、登录方式（账号密码/IOA）、IP、User-Agent、登录时间、退出时间、停留时长
- **角色管理**：自定义角色增删改，通过权限树分配权限点

---

## 数据库结构（dashboard_db）

```sql
-- 用户表
CREATE TABLE `user` (
    `id`       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50)  NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `role`     VARCHAR(20)  DEFAULT NULL  COMMENT 'ADMIN/MANAGER/USER，自定义角色为NULL',
    `role_id`  BIGINT       DEFAULT NULL  COMMENT '自定义角色ID'
);

-- 需求表（字段略，见 init.sql）

-- 附件表
CREATE TABLE `attachment` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`  BIGINT       NOT NULL,
    `file_name`       VARCHAR(255) NOT NULL,
    `file_url`        VARCHAR(500) NOT NULL COMMENT '/uploads/xxx',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 需求方部门字典
CREATE TABLE `sys_department` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL UNIQUE,  -- 有唯一约束
    `sort_order` INT          DEFAULT 0,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 需求模块字典
CREATE TABLE `sys_module` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL,
    `sort_order` INT          DEFAULT 0,
    `bg_color`   VARCHAR(20)  DEFAULT NULL,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE `sys_role` (
    `id`       BIGINT      PRIMARY KEY AUTO_INCREMENT,
    `name`     VARCHAR(50) NOT NULL,
    `code`     VARCHAR(50) NOT NULL UNIQUE,
    `built_in` TINYINT     NOT NULL DEFAULT 0 COMMENT '1=内置',
    `remark`   VARCHAR(200),
    `created_at` DATETIME  DEFAULT CURRENT_TIMESTAMP
);

-- 权限点表（27条，id固定）
CREATE TABLE `sys_permission` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL,
    `code`       VARCHAR(100) NOT NULL UNIQUE,
    `type`       VARCHAR(20)  NOT NULL COMMENT 'MENU/BUTTON',
    `parent_id`  BIGINT       DEFAULT NULL,
    `sort_order` INT          DEFAULT 0,
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联（只存 BUTTON 权限，MENU 权限由 UNION 查询自动补）
CREATE TABLE `sys_role_permission` (
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`)
);

-- 登录日志表
CREATE TABLE `login_log` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`         VARCHAR(50)  NOT NULL,
    `login_type`       VARCHAR(20)  NOT NULL COMMENT '账号密码/IOA',
    `login_ip`         VARCHAR(50),
    `user_agent`       VARCHAR(255),
    `login_time`       DATETIME     NOT NULL,
    `logout_time`      DATETIME,
    `duration_minutes` INT,
    `status`           VARCHAR(10)  NOT NULL DEFAULT '在线'
);
```

---

## API 接口一览

| 方法 | 路径 | 说明 | 所需权限 |
|------|------|------|---------|
| POST | `/api/auth/login` | 账号密码登录 | 公开 |
| POST | `/api/auth/ioa/login` | IOA 一键登录 | 公开 |
| POST | `/api/auth/logout` | 登出 | 已登录 |
| GET  | `/api/requirements` | 需求列表 | `requirement:view` |
| GET  | `/api/requirements/tab-counts` | 各 Tab 数量 | 已登录 |
| GET  | `/api/requirements/stats` | 统计数据 | 已登录 |
| POST | `/api/requirements` | 新增需求 | `requirement:create` |
| PUT  | `/api/requirements/{id}` | 编辑需求 | `requirement:edit` |
| DELETE | `/api/requirements/{id}` | 删除需求 | `requirement:delete` |
| GET  | `/api/requirements/{id}/logs` | 操作日志 | 已登录 |
| POST | `/api/upload` | 上传图片（PNG/JPG，≤10MB，最多5张）| 已登录 |
| GET  | `/api/requirements/{id}/attachments` | 附件列表 | 已登录 |
| POST | `/api/requirements/{id}/attachments` | 关联附件 | 已登录 |
| DELETE | `/api/attachments/{id}` | 删除附件 | 已登录 |
| GET  | `/api/dict/departments` | 部门列表（筛选用）| 已登录 |
| POST | `/api/dict/departments` | 新增部门 | `dept:create` |
| PUT  | `/api/dict/departments/{id}` | 编辑部门 | `dept:edit` |
| DELETE | `/api/dict/departments/{id}` | 删除部门 | `dept:delete` |
| GET  | `/api/dict/modules` | 模块列表（筛选用）| 已登录 |
| POST | `/api/dict/modules` | 新增模块 | `module:create` |
| PUT  | `/api/dict/modules/{id}` | 编辑模块 | `module:edit` |
| DELETE | `/api/dict/modules/{id}` | 删除模块 | `module:delete` |
| GET  | `/api/system/users` | 用户列表 | `user:view` |
| POST | `/api/system/users` | 新增用户 | `user:create` |
| PUT  | `/api/system/users/{id}` | 编辑用户 | `user:edit` |
| DELETE | `/api/system/users/{id}` | 删除用户 | `user:delete` |
| GET  | `/api/system/login-logs` | 登录日志 | `system:login-log` |
| GET  | `/api/system/roles` | 角色列表 | `system:role` 或 `user:create/edit` |
| POST | `/api/system/roles` | 新增角色 | `role:create` |
| PUT  | `/api/system/roles/{id}` | 编辑角色 | `role:edit` |
| DELETE | `/api/system/roles/{id}` | 删除角色 | `role:delete` |
| GET  | `/api/system/permissions/tree` | 权限树 | `system:role` |
| GET  | `/api/system/permissions/role/{id}` | 角色已有权限 | `system:role` |
| POST | `/api/system/permissions/role/{id}` | 保存角色权限 | `role:edit` |

---

## 关键文件

| 文件 | 说明 |
|------|------|
| `backend/src/main/resources/application.yml` | 数据库 + IOA + multipart 配置 |
| `backend/src/main/resources/sql/init.sql` | 本地开发建表 + 初始数据 |
| `backend/src/main/resources/sql/init_prod.sql` | 生产环境建表脚本 |
| `backend/src/main/resources/sql/migrate_v5.2.sql` | V5.2 生产增量迁移脚本 |
| `backend/src/main/resources/mapper/RequirementMapper.xml` | 核心查询 SQL |
| `backend/.../config/AuthInterceptor.java` | Token 验证拦截器 |
| `backend/.../controller/AuthController.java` | 登录/登出/IOA登录 |
| `backend/.../controller/RequirementController.java` | 需求 CRUD + 日志 + 统计 |
| `backend/.../controller/UploadController.java` | 图片上传（PNG/JPG，≤10MB）|
| `backend/.../controller/AttachmentController.java` | 附件 CRUD |
| `backend/.../controller/DictController.java` | 部门/模块字典 CRUD |
| `backend/.../controller/UserController.java` | 用户管理 CRUD |
| `backend/.../controller/LoginLogController.java` | 登录日志查询 |
| `backend/.../controller/RoleController.java` | 角色管理 CRUD（V5.2）|
| `backend/.../controller/PermissionController.java` | 权限树 + 角色权限分配（V5.2）|
| `backend/.../service/PermissionService.java` | 内置角色权限硬编码 + 权限树构建（V5.2）|
| `backend/.../service/impl/AuthServiceImpl.java` | Token 存储 + 登录日志 |
| `backend/.../mapper/SysPermissionMapper.java` | 权限点查询（UNION 自动补 MENU code）|
| `frontend/src/views/Board.vue` | 主看板页（所有功能全在此）|
| `frontend/src/views/Login.vue` | 登录页（IOA/账号密码双Tab）|
| `frontend/src/stores/auth.js` | Pinia 登录状态（token/role/permissions/roleId）|
| `frontend/src/api/role.js` | 角色管理接口（V5.2）|
| `frontend/src/api/permission.js` | 权限接口（V5.2）|
| `deploy.sh` | 一键部署脚本 |

---

## IOA 登录配置

```yaml
ioa:
  host: https://ioa.wawo.cc:27800
  appid: f22a6b50-f44b-4ae3-81d8-f9100a36405a
  secret-key: $2a$10$7in4LrZc71uDlZ7MdxRWquHlu7rLYFdR4bLYeYJ3izh1pTq8pNgX6
  secret-id: fcd51120db1a11f086c175cafcfa317a
  api-version: 2022-06-01
  sso-url: https://sso.wawo.cc:54339/api/public/clientlogin/auth_login
```

- IOA CheckTicket 响应结构：`{Code:0, Message:"Success", Data:{VerifyResult:true}}`
- 内网服务器使用自签名证书，后端用 TrustAll RestTemplate 跳过验证

---

## 生产部署（阿里云 47.103.56.254）

- **部署方式**：手动 build → scp → 重启后端 → reload Nginx（deploy.sh 仅供参考，生产启动命令见下）
- **后端启动**：`cd /opt/dashboard && nohup java -jar dashboard-backend-1.0.0.jar --server.port=8080 > app.log 2>&1 &`
- **前端**：静态文件在 `/opt/dashboard/frontend/`
- **Nginx 配置**：`/etc/nginx/conf.d/dashboard.conf`，`client_max_body_size 10m`，`/api/` 和 `/uploads/` 反代到 `127.0.0.1:8080`
- **数据库**：MySQL 8.0.44，首次执行 `init_prod.sql`，V5.2 升级执行 `migrate_v5.2.sql`；V5.3 无数据库变更
- **注意**：`ALTER TABLE user ADD COLUMN IF NOT EXISTS` 在生产 MySQL 上语法报错，需直接用 `ADD COLUMN`
- SSH 已配置密钥登录（免密码），账号 root@47.103.56.254
