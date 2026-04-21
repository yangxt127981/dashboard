## ADDED Requirements

### Requirement: 账号密码登录
用户可通过用户名和密码登录系统，登录成功后获得 UUID Token，Token 存储在服务端内存中。

#### Scenario: 登录成功
- **WHEN** 用户提交正确的用户名和密码到 POST /api/auth/login
- **THEN** 返回 token、username、role、permissions[] 等信息，前端存入 Pinia + localStorage

#### Scenario: 登录失败
- **WHEN** 用户提交错误的用户名或密码
- **THEN** 返回 401，前端提示"用户名或密码错误"

---

### Requirement: IOA 一键登录
公司内网用户可通过 IOA SSO 登录，无需手动输入密码。

#### Scenario: IOA 首次登录
- **WHEN** 用户点击 IOA 登录，前端从本机 IOA 客户端（sso.wawo.cc:54339）获取 Ticket，发送到 POST /api/auth/ioa/login
- **THEN** 后端调用 IOA CheckTicket API 验证，验证通过后自动创建 USER 角色账号并返回 token

#### Scenario: IOA 指定用户升级为 MANAGER
- **WHEN** IOA 账号在 MANAGER 白名单中（AuthController.java 硬编码，大小写不敏感）
- **THEN** 账号自动拥有 MANAGER 角色权限

---

### Requirement: 登出
用户登出后 Token 立即失效。

#### Scenario: 登出成功
- **WHEN** 用户调用 POST /api/auth/logout
- **THEN** 服务端从内存移除 Token，前端清除 Pinia + localStorage，跳转登录页

---

### Requirement: 接口鉴权拦截
所有受保护接口在未登录时拒绝访问。

#### Scenario: 未携带 Token 访问接口
- **WHEN** 请求 /api/** 路径（除 /api/auth/**）时未携带有效 Token
- **THEN** AuthInterceptor 返回 401

---

### Requirement: RBAC 权限控制
权限按角色分配，前端和后端双重校验。

#### Scenario: 前端按钮隐藏
- **WHEN** 用户登录后，permissions[] 中不包含某操作的权限点
- **THEN** 对应按钮/菜单通过 v-if 隐藏，不渲染到 DOM

#### Scenario: 后端接口拦截
- **WHEN** 用户直接调用需要特定权限的接口但角色不满足
- **THEN** 后端返回 403

---

### Requirement: 登录日志记录
每次登录和登出均记录日志，供管理员审计。

#### Scenario: 记录登录日志
- **WHEN** 用户登录成功（账号密码或 IOA 方式）
- **THEN** 在 login_log 表记录：用户名、登录方式、IP、User-Agent、登录时间，状态为"在线"

#### Scenario: 记录登出时间
- **WHEN** 用户主动登出或 Token 过期
- **THEN** 更新 logout_time、duration_minutes，状态改为"离线"
