## ADDED Requirements

### Requirement: 需求对接人维护
ADMIN/MANAGER 可管理需求对接人字典（sys_request_owner 表）。

#### Scenario: 增删改对接人
- **WHEN** 有 system:requestowner 权限的用户新增/编辑/删除对接人
- **THEN** 调用对应 /api/dict/request-owners 接口，列表实时更新

#### Scenario: 对接人重名
- **WHEN** 用户提交已存在的对接人名称
- **THEN** 后端返回友好提示，拒绝保存

---

### Requirement: 需求方部门维护
ADMIN/MANAGER 可管理部门字典（sys_department 表），支持排序配置。

#### Scenario: 增删改部门
- **WHEN** 有 dept:create/edit/delete 权限的用户操作
- **THEN** 调用 /api/dict/departments 对应接口

#### Scenario: 部门名称唯一约束
- **WHEN** 用户提交已存在的部门名称
- **THEN** 数据库唯一约束触发，前端显示错误提示

---

### Requirement: 需求模块维护
ADMIN/MANAGER 可管理模块字典（sys_module 表），支持背景色配置。

#### Scenario: 增删改模块
- **WHEN** 有 module:create/edit/delete 权限的用户操作
- **THEN** 调用 /api/dict/modules 对应接口，背景色以色块预览

---

### Requirement: 用户管理
ADMIN 可管理非 IOA 用户，包括分配角色。

#### Scenario: 新增/编辑用户
- **WHEN** ADMIN 新增或编辑用户时
- **THEN** 可选择内置角色（ADMIN/MANAGER/USER）或自定义角色（role_id 关联 sys_role）

#### Scenario: 不可删除当前登录账号
- **WHEN** ADMIN 尝试删除当前已登录的账号
- **THEN** 后端拒绝并返回错误提示

---

### Requirement: 角色管理
ADMIN 可创建自定义角色并通过权限树分配权限点。

#### Scenario: 新建自定义角色
- **WHEN** ADMIN 创建角色并勾选权限点
- **THEN** 叶子节点 BUTTON 权限存入 sys_role_permission，MENU 权限由 UNION 查询自动补全

#### Scenario: 内置角色权限不可修改
- **WHEN** ADMIN 查看内置角色（ADMIN/MANAGER/USER）的权限树
- **THEN** 权限树所有节点显示为禁用状态，不可勾选

---

### Requirement: 登录日志
ADMIN/MANAGER 可查看所有用户的登录记录。

#### Scenario: 查看登录日志
- **WHEN** 有 system:login-log 权限的用户访问登录日志页
- **THEN** 显示：账号名、登录方式、IP、User-Agent、登录时间、退出时间、停留时长、状态（在线/离线）

---

### Requirement: 产品对接人维护菜单入口
系统管理侧边栏 SHALL 新增"产品对接人维护"菜单项，ADMIN 和 MANAGER 角色可见。

#### Scenario: 菜单项可见性
- **WHEN** ADMIN 或 MANAGER 角色用户登录
- **THEN** 侧边栏系统管理分组下显示"产品对接人维护"菜单项，点击跳转至 /system/product-owners

#### Scenario: USER 角色不可见
- **WHEN** USER 角色用户登录
- **THEN** 侧边栏不显示"产品对接人维护"菜单项

---

### Requirement: system:productowner 权限点
系统 SHALL 新增 system:productowner 权限点，ADMIN 和 MANAGER 内置角色均拥有该权限。

#### Scenario: MANAGER 拥有权限
- **WHEN** MANAGER 角色用户访问产品对接人相关接口
- **THEN** 后端鉴权通过，允许增删改查操作

#### Scenario: USER 无权限
- **WHEN** USER 角色用户直接调用 POST/PUT/DELETE /api/dict/product-owners
- **THEN** 后端返回 403
