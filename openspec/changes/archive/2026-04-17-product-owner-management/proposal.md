## Why

产品对接人选项目前硬编码在 Board.vue 和 InboxBoard.vue 中，每次新增或修改产品对接人都需要改代码重新部署。新增"产品对接人维护"管理页面，让 ADMIN/MANAGER 可在系统内直接维护，无需开发介入。

## What Changes

- 新增数据库表 `sys_product_owner`，存储产品对接人名称和排序
- 新增后端接口 `/api/dict/product-owners`（GET/POST/PUT/DELETE）
- 新增前端管理页面 `SystemProductOwner.vue`，支持增删改
- Board.vue 和 InboxBoard.vue 的产品对接人下拉选项改为从接口动态加载，不再硬编码
- 侧边栏系统管理分组新增"产品对接人维护"菜单项（ADMIN/MANAGER 可见）
- 新增权限点 `system:productowner`，ADMIN 和 MANAGER 角色均拥有该权限

## Capabilities

### New Capabilities
- `product-owner-management`: 产品对接人的增删改查管理，以及需求表单中产品对接人下拉选项动态化

### Modified Capabilities
- `system-management`: 新增产品对接人维护菜单入口和权限点

## Impact

- **后端**：新增 `SysProductOwner` 实体、Mapper、Controller；`DictController.java` 或新建 `ProductOwnerController.java`；`PermissionService.java` 新增权限点
- **前端**：新增 `SystemProductOwner.vue`；`AppLayout.vue` 添加菜单项；`Board.vue`、`InboxBoard.vue` 改为 API 动态加载产品对接人；`frontend/src/api/dict.js` 新增接口方法
- **数据库**：新增 `sys_product_owner` 表，初始数据：刘秋诗、赵轶群、丁滢、Hanson、张明洋、邵森伟
- **迁移脚本**：需要新增 `migrate_v6.1.sql`
