## Context

产品对接人目前硬编码为数组 `['刘秋诗', '赵轶群', '丁滢', 'Hanson', '张明洋', '邵森伟']`，分别在 Board.vue 和 InboxBoard.vue 中重复定义。新功能需将其迁移到数据库表，并提供管理界面，模式与已有的 `sys_request_owner`（需求对接人）完全一致。

## Goals / Non-Goals

**Goals**
- 产品对接人可在系统内动态维护，无需改代码
- Board.vue / InboxBoard.vue 下拉选项从接口加载
- ADMIN/MANAGER 拥有维护权限，USER 只读

**Non-Goals**
- 不支持产品对接人与 IOA 账号关联
- 不修改需求表 `productOwner` 字段类型（仍为字符串存储）

## Decisions

### 复用 sys_request_owner 模式
新建 `sys_product_owner` 表（id, name, sort_order, created_at），结构与 `sys_request_owner` 完全相同。后端在 `DictController.java` 中新增 `/api/dict/product-owners` 路由，复用相同的 CRUD 模式。

**理由**：已有 sys_request_owner 实现经过验证，代码路径清晰，复用可最小化变更量。

### 权限点复用 system:productowner
在 `PermissionService.java` 硬编码中，ADMIN 和 MANAGER 均加入 `system:productowner` 权限点，与 `system:requestowner` 保持一致。

### 前端组件复用 SystemRequestOwner.vue 模式
新建 `SystemProductOwner.vue`，结构与 `SystemRequestOwner.vue` 完全一致（表格 + 新增/编辑弹框 + 删除确认）。

### 下拉动态化
Board.vue 和 InboxBoard.vue 在 `onMounted` 时调用 `GET /api/dict/product-owners`，替换原有硬编码数组。新增 `frontend/src/api/dict.js` 中的 `getProductOwners` 方法。

## Risks / Trade-offs

- [风险] 生产数据库需执行迁移脚本，若遗漏则后端启动后接口报错 → 迁移脚本与部署文档一起提交

## Migration Plan

1. 执行 `migrate_v6.1.sql`（本地 + 生产），创建 `sys_product_owner` 表并插入初始数据
2. 部署后端（含新接口和权限点）
3. 部署前端（下拉改为动态加载）

## Open Questions

无
