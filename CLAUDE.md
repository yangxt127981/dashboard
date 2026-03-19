# ONE家项目计划看板 - Claude 上下文

## 项目概述

内部项目需求看板系统（ONE家项目计划看板），供信息部管理多部门需求，实现进度透明化。

- **代码仓库**：https://github.com/yangxt127981/dashboard（当前版本: V5.2）
- **本地路径**：/Users/hansonyang/Project/dashboard
- **生产地址**：http://47.103.56.254（阿里云）

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

## 已实现功能（V5.2 完整版）

### 登录鉴权
- **账号密码登录**：POST `/api/auth/login`，返回 UUID Token
- **IOA 一键登录**：POST `/api/auth/ioa/login`
  - 前端从本机 IOA 客户端（`sso.wawo.cc:54339`）拿 Ticket
  - 后端调 IOA CheckTicket API（`ioa.wawo.cc:27800`）验证
  - 首次登录自动创建 USER 角色账号
  - 特定 IOA 用户自动升级为 MANAGER 角色（见下方权限说明）
- **登出**：POST `/api/auth/logout`，Token 失效
- Token 存内存 `ConcurrentHashMap`，由 `AuthInterceptor` 拦截所有 `/api/**`（除 `/api/auth/**`）
- **三级权限**：ADMIN 可增删改查，MANAGER 可增改查（无删除），USER 只读；前端隐藏按钮 + 后端双重校验

### 需求列表
- 分页（PageHelper），默认每页 10 条
- **Tab 页**：全部 / 进行中（设计中/开发中/测试中）/ 未开始 / 已上线 / 已取消，Tab 页签实时显示各状态记录数量
- **筛选**：需求名称模糊搜索、部门下拉、所属模块筛选、优先级多选、状态多选（仅"全部"Tab 可用）、产品对接人模糊搜索；高级查询可折叠/展开
- **排序**：优先级（紧急→高→中→低）、计划完成时间、实际完成时间、计划开始时间、实际开始时间（升降序），后端全局排序
- **列配置**：可隐藏/显示列，配置持久化到 `localStorage`
- 列表右上角有刷新按钮（Icon 形式）

### 需求字段
需求名称、所属模块、需求方部门、需求对接人、产品对接人、优先级、计划开始、计划完成、实际开始、实际完成、状态、需求描述

优先级枚举：`紧急 / 高 / 中 / 低`
状态枚举：`未开始 / 设计中 / 开发中 / 测试中 / 已上线 / 已取消`

部门列表：信息部、产品部、运营部、市场部、财务部、商品选品部、其他

### 需求 CRUD
- 新增需求：弹窗表单，所有字段，ADMIN / MANAGER 可用
- 编辑需求：弹窗表单，数据回显，ADMIN / MANAGER 可用
- 取消需求：状态改为"已取消"，ADMIN / MANAGER 可用
- 删除需求：二次确认弹窗，**仅 ADMIN**
- 需求名称（`functionName`）有非空校验，空值返回 400

### 附件
- 上传：POST `/api/upload`，保存到 `uploads/` 目录，限 10MB
- 附件关联需求：`attachment` 表，支持多附件
- 接口：GET/POST `/api/requirements/{id}/attachments`，DELETE `/api/attachments/{id}`
- 生产环境 Nginx 配置了 `/uploads/` 反代到后端

### 操作日志
- 每次创建、编辑、删除自动记录日志
- 日志含：操作人、操作类型、变更前/后内容（JSON）
- 接口：GET `/api/requirements/{id}/logs`

### 数据统计看板
- 折叠/展开面板
- 三维度饼图：部门分布、优先级分布、状态分布
- 三图联动：点击部门图，另两图随之过滤；再次点击取消联动

### 权限管理（V5.2 新增）
基于 RBAC 模型，内置三个角色权限不可修改，支持自定义角色。

- **权限点**：菜单和按钮均作为权限点（共 22 个），权限码如 `requirement:create`、`dept:delete` 等
- **内置角色权限**（硬编码，不走数据库）：
  - ADMIN：全部权限
  - MANAGER：`board:view`、`requirement:create/edit/cancel`
  - USER：`board:view`
- **自定义角色**：在"角色管理"页面创建，可通过权限树勾选任意权限点
- **用户关联角色**：内置角色通过 `user.role` 字段存储；自定义角色通过 `user.role_id` 关联 `sys_role`
- **登录时权限下发**：后端计算权限集后随 token 一同返回 `permissions[]`，前端存入 Pinia + localStorage
- **前端鉴权**：`authStore.hasPermission('code')` 控制按钮/菜单显隐；后端同步校验每个接口
- **系统管理入口**：动态显示，用户拥有任意系统管理权限时显示对应菜单项

### 系统管理（V5.1 新增）
- **需求方部门维护**：部门字典增删改，排序可配置，下拉选项动态加载自数据库
- **需求模块维护**：模块字典增删改，支持背景色配置
- **用户管理**：非 IOA 用户增删改，可设置角色（ADMIN/MANAGER/USER），不可删除当前登录账号
- **登录日志**：记录账号名、登录方式（账号密码/IOA）、IP、User-Agent、登录时间、退出时间、停留时长

### 登录页（V5.1 更新）
- 改为 IOA / 账号密码 双 Tab 切换，默认显示 IOA 登录

---

## 数据库结构（dashboard_db）

```sql
-- 用户表
CREATE TABLE `user` (
    `id`       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名 / IOA 工号',
    `password` VARCHAR(100) NOT NULL        COMMENT '密码（IOA 登录固定为 IOA_SSO）',
    `role`     VARCHAR(20)  NOT NULL        COMMENT 'ADMIN / MANAGER / USER'
);

-- 需求表
CREATE TABLE `requirement` (
    `id`                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `function_name`       VARCHAR(100) NOT NULL,
    `module_name`         VARCHAR(100),
    `request_department`  VARCHAR(100),
    `request_owner`       VARCHAR(50),
    `product_owner`       VARCHAR(50),
    `priority`            VARCHAR(20)  COMMENT '紧急/高/中/低',
    `planned_start_time`  DATE,
    `planned_end_time`    DATE,
    `actual_start_time`   DATE,
    `actual_end_time`     DATE,
    `status`              VARCHAR(20)  NOT NULL DEFAULT '未开始' COMMENT '未开始/设计中/开发中/测试中/已上线/已取消',
    `description`         TEXT,
    `created_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 附件表
CREATE TABLE `attachment` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`  BIGINT       NOT NULL,
    `file_name`       VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url`        VARCHAR(500) NOT NULL COMMENT '访问路径 /uploads/xxx',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE `requirement_log` (
    `id`               BIGINT      PRIMARY KEY AUTO_INCREMENT,
    `requirement_id`   BIGINT      NOT NULL,
    `operator`         VARCHAR(50) NOT NULL,
    `operation_type`   VARCHAR(20) NOT NULL COMMENT '创建/编辑/删除',
    `before_content`   TEXT        COMMENT '变更前 JSON',
    `after_content`    TEXT        COMMENT '变更后 JSON',
    `created_at`       DATETIME    DEFAULT CURRENT_TIMESTAMP
);

-- 用户登录日志表（V5.1 新增）
CREATE TABLE `login_log` (
    `id`               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`         VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `login_type`       VARCHAR(20)  NOT NULL COMMENT '登录方式：账号密码/IOA',
    `login_ip`         VARCHAR(50)           COMMENT '登录IP',
    `user_agent`       VARCHAR(255)          COMMENT '浏览器信息',
    `login_time`       DATETIME     NOT NULL COMMENT '登录时间',
    `logout_time`      DATETIME              COMMENT '退出时间',
    `duration_minutes` INT                   COMMENT '停留时长（分钟）',
    `status`           VARCHAR(10)  NOT NULL DEFAULT '在线' COMMENT '在线/已退出'
);

-- 需求方部门字典表（V5.1 新增）
CREATE TABLE `sys_department` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '部门名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 需求模块字典表（V5.1 新增）
CREATE TABLE `sys_module` (
    `id`         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`       VARCHAR(100) NOT NULL COMMENT '模块名称',
    `sort_order` INT          DEFAULT 0 COMMENT '排序',
    `bg_color`   VARCHAR(20)  DEFAULT NULL COMMENT '背景色',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

---

## API 接口一览

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 账号密码登录 | 公开 |
| POST | `/api/auth/ioa/login` | IOA 一键登录 | 公开 |
| POST | `/api/auth/logout` | 登出 | 已登录 |
| GET  | `/api/requirements` | 需求列表（分页+筛选+排序）| 已登录 |
| POST | `/api/requirements` | 新增需求 | ADMIN / MANAGER |
| PUT  | `/api/requirements/{id}` | 编辑需求 | ADMIN / MANAGER |
| DELETE | `/api/requirements/{id}` | 删除需求 | ADMIN 专属 |
| GET  | `/api/requirements/{id}/logs` | 操作日志 | 已登录 |
| GET  | `/api/requirements/stats` | 统计数据（三图）| 已登录 |
| POST | `/api/upload` | 上传附件（≤10MB）| 已登录 |
| GET  | `/api/requirements/{id}/attachments` | 附件列表 | 已登录 |
| POST | `/api/requirements/{id}/attachments` | 关联附件 | 已登录 |
| DELETE | `/api/attachments/{id}` | 删除附件 | 已登录 |
| GET    | `/api/dict/departments` | 部门字典列表 | 已登录 |
| POST   | `/api/dict/departments` | 新增部门 | ADMIN |
| PUT    | `/api/dict/departments/{id}` | 编辑部门 | ADMIN |
| DELETE | `/api/dict/departments/{id}` | 删除部门 | ADMIN |
| GET    | `/api/dict/modules` | 模块字典列表 | 已登录 |
| POST   | `/api/dict/modules` | 新增模块 | ADMIN |
| PUT    | `/api/dict/modules/{id}` | 编辑模块 | ADMIN |
| DELETE | `/api/dict/modules/{id}` | 删除模块 | ADMIN |
| GET    | `/api/system/users` | 用户列表（非IOA）| ADMIN |
| POST   | `/api/system/users` | 新增用户 | ADMIN |
| PUT    | `/api/system/users/{id}` | 编辑用户 | ADMIN |
| DELETE | `/api/system/users/{id}` | 删除用户 | ADMIN |
| GET    | `/api/system/login-logs` | 登录日志（分页+筛选）| ADMIN |

---

## 关键文件

| 文件 | 说明 |
|------|------|
| `backend/src/main/resources/application.yml` | 数据库 + IOA 配置 |
| `backend/src/main/resources/sql/init.sql` | 本地开发建表 + 初始数据 |
| `backend/src/main/resources/sql/init_prod.sql` | 生产环境建表脚本（无测试数据）|
| `backend/src/main/resources/mapper/RequirementMapper.xml` | 核心查询 SQL + 动态筛选 + 排序逻辑 |
| `backend/.../config/AuthInterceptor.java` | Token 验证拦截器 |
| `backend/.../config/WebConfig.java` | 拦截器注册 + CORS + 静态资源 `/uploads/` |
| `backend/.../controller/AuthController.java` | 登录/登出/IOA登录 |
| `backend/.../controller/RequirementController.java` | 需求 CRUD + 日志 + 统计 |
| `backend/.../controller/UploadController.java` | 文件上传 |
| `backend/.../controller/AttachmentController.java` | 附件 CRUD |
| `backend/.../controller/DictController.java` | 部门/模块字典 CRUD（V5.1）|
| `backend/.../controller/UserController.java` | 用户管理 CRUD（V5.1）|
| `backend/.../controller/LoginLogController.java` | 登录日志查询（V5.1）|
| `backend/.../service/IoaService.java` | IOA CheckTicket（含 TrustAll SSL）|
| `backend/.../service/impl/AuthServiceImpl.java` | Token 存储 + 登录日志记录（ConcurrentHashMap）|
| `backend/.../service/impl/RequirementServiceImpl.java` | 业务逻辑 + 日志记录 |
| `backend/.../mapper/SysDepartmentMapper.java` | 部门字典 Mapper（V5.1）|
| `backend/.../mapper/SysModuleMapper.java` | 模块字典 Mapper（V5.1）|
| `backend/.../mapper/LoginLogMapper.java` | 登录日志 Mapper（V5.1）|
| `frontend/src/views/Board.vue` | 主看板页（列表/筛选/Tab/弹窗/统计图/系统管理全在此）|
| `frontend/src/views/Login.vue` | 登录页（IOA/账号密码双Tab）|
| `frontend/src/api/auth.js` | login / ioaLogin / logout |
| `frontend/src/api/dict.js` | 部门/模块字典接口（V5.1）|
| `frontend/src/api/user.js` | 用户管理接口（V5.1）|
| `frontend/src/api/loginLog.js` | 登录日志接口（V5.1）|
| `frontend/src/api/axios.js` | Axios 实例 + 全局 401 拦截 |
| `frontend/src/stores/auth.js` | Pinia 登录状态（token/username/role）|
| `frontend/public/login-bg.svg` | 登录页背景图（浅蓝科技风 SVG）|
| `deploy.sh` | 一键部署脚本（build → scp → 启动后端 → Nginx）|

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

- **部署方式**：`bash deploy.sh`（自动 build → scp → 重启后端 → reload Nginx）
- **后端**：Java 进程，端口 8080，日志：`/opt/dashboard/app.log`
- **前端**：静态文件在 `/opt/dashboard/frontend/`
- **Nginx**：`/api/` 和 `/uploads/` 反代到 `127.0.0.1:8080`，其余 SPA 路由
- **数据库**：MySQL on server，执行 `init_prod.sql` 初始化
- SSH 已配置密钥登录（免密码），账号 root@47.103.56.254

---


