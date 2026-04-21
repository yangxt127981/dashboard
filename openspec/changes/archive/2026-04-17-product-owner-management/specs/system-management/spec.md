## ADDED Requirements

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
