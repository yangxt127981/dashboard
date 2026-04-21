## 1. 数据库

- [x] 1.1 创建 `migrate_v6.1.sql`，包含 `sys_product_owner` 表建表语句及初始数据（刘秋诗、赵轶群、丁滢、Hanson、张明洋、邵森伟）
- [x] 1.2 在本地执行 migrate_v6.1.sql

## 2. 后端 — 实体与 Mapper

- [x] 2.1 新建 `SysProductOwner.java` 实体类（id, name, sortOrder, createdAt）
- [x] 2.2 新建 `SysProductOwnerMapper.java` 及对应 XML，实现 findAll / insert / update / deleteById / countByName

## 3. 后端 — 接口与权限

- [x] 3.1 在 `DictController.java` 新增 `/api/dict/product-owners` 的 GET/POST/PUT/DELETE 接口，复用 requestOwner 模式
- [x] 3.2 在 `PermissionService.java` MANAGER_PERMISSIONS 中新增 `system:productowner` 权限点
- [x] 3.3 在 `init.sql` 的 `sys_permission` 初始数据中新增 `system:productowner` 权限点记录

## 4. 前端 — API 与管理页面

- [x] 4.1 在 `frontend/src/api/dict.js` 新增 `getProductOwners / createProductOwner / updateProductOwner / deleteProductOwner` 方法
- [x] 4.2 新建 `frontend/src/views/SystemProductOwner.vue`，结构参照 SystemRequestOwner.vue（表格 + 弹框 + 删除确认）
- [x] 4.3 在 `frontend/src/router/index.js` 新增 `/system/product-owners` 路由，component 指向 SystemProductOwner.vue
- [x] 4.4 在 `AppLayout.vue` 系统管理侧边栏新增"产品对接人维护"菜单项，使用 `system:productowner` 权限控制显示

## 5. 前端 — 下拉动态化

- [x] 5.1 Board.vue：移除 `productOwnerOptions` 硬编码数组，改为 `ref([])` 并在 onMounted 调用 `getProductOwners` 填充
- [x] 5.2 InboxBoard.vue：同上，移除硬编码，改为接口动态加载

## 6. 验证

- [x] 6.1 本地测试：管理页面新增/编辑/删除产品对接人，验证重名提示
- [x] 6.2 本地测试：需求进度和需求提报页面下拉选项动态加载，新增产品对接人后刷新即可见
- [x] 6.3 本地测试：USER 角色无法访问管理接口（403）
