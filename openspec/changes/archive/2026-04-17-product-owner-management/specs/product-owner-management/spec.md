## ADDED Requirements

### Requirement: 产品对接人列表展示
系统 SHALL 在系统管理模块下提供"产品对接人维护"页面，以表格形式展示所有产品对接人，支持按 sort_order 排序。

#### Scenario: 加载产品对接人列表
- **WHEN** 有 system:productowner 权限的用户进入产品对接人维护页面
- **THEN** 调用 GET /api/dict/product-owners，以表格展示：名称、排序、创建时间、操作列

---

### Requirement: 新增产品对接人
系统 SHALL 允许有 system:productowner 权限的用户新增产品对接人。

#### Scenario: 新增成功
- **WHEN** 用户填写名称（必填）和排序，点击确认
- **THEN** 调用 POST /api/dict/product-owners，列表刷新新增记录

#### Scenario: 名称重复
- **WHEN** 用户提交已存在的产品对接人名称
- **THEN** 后端返回错误提示，前端显示"产品对接人已存在"

#### Scenario: 名称为空
- **WHEN** 用户提交空名称
- **THEN** 前端表单校验阻止提交，提示名称为必填项

---

### Requirement: 编辑产品对接人
系统 SHALL 允许有 system:productowner 权限的用户编辑现有产品对接人的名称和排序。

#### Scenario: 编辑成功
- **WHEN** 用户修改名称或排序并确认
- **THEN** 调用 PUT /api/dict/product-owners/{id}，列表显示最新数据

---

### Requirement: 删除产品对接人
系统 SHALL 允许有 system:productowner 权限的用户删除产品对接人。

#### Scenario: 删除成功
- **WHEN** 用户点击删除并确认
- **THEN** 调用 DELETE /api/dict/product-owners/{id}，记录从列表移除

---

### Requirement: 产品对接人下拉动态加载
Board.vue 和 InboxBoard.vue 中的产品对接人下拉选项 SHALL 从接口动态加载，不再硬编码。

#### Scenario: 下拉选项加载
- **WHEN** 用户打开需求进度或需求提报页面
- **THEN** 前端调用 GET /api/dict/product-owners，将返回列表填充到产品对接人筛选下拉和表单下拉

#### Scenario: 新增产品对接人后即时生效
- **WHEN** 管理员在产品对接人维护页面新增一条记录
- **THEN** 刷新需求进度或需求提报页面后，新名称出现在产品对接人下拉选项中
