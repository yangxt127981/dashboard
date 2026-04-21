## ADDED Requirements

### Requirement: 需求提报列表
独立页面展示用户提报的需求，支持分页、筛选、排序。

#### Scenario: 数据权限过滤
- **WHEN** USER 角色用户访问 /api/inbox
- **THEN** 只返回 submitted_by = 当前用户名 的记录

#### Scenario: ADMIN/MANAGER 查看全部
- **WHEN** ADMIN 或 MANAGER 角色用户访问 /api/inbox
- **THEN** 返回所有人的提报记录

#### Scenario: Tab 状态切换
- **WHEN** 用户点击 Tab（全部 / 已创建 / 待评估 / 已驳回 / 进入需求池 / 已取消）
- **THEN** 列表过滤为对应提报状态，Tab 实时显示各状态数量

#### Scenario: 按产品对接人筛选
- **WHEN** 用户选择产品对接人筛选项
- **THEN** 列表过滤为该产品对接人负责的提报需求

---

### Requirement: 新建提报需求
所有登录用户可新建提报需求，MANAGER 角色不显示新建按钮。

#### Scenario: 新建成功
- **WHEN** 用户填写需求名称（必填）、需求价值（必填）并提交
- **THEN** 调用 POST /api/inbox，需求以"已创建"状态出现在提报列表

#### Scenario: 重名校验
- **WHEN** 用户提交的需求名称与已存在的需求重名
- **THEN** 后端返回 400，前端提示"需求已经存在"

#### Scenario: 新建时自动预填需求对接人
- **WHEN** 当前登录用户名存在于需求对接人列表中
- **THEN** 新建表单的"需求对接人"字段自动预填为当前用户

---

### Requirement: 提报状态流转
提报需求按固定流程流转，不可跳步。

#### Scenario: 提交评估（已创建 → 待评估）
- **WHEN** 非 MANAGER 用户对"已创建"状态的需求点击"提交"
- **THEN** 调用 POST /api/inbox/{id}/submit，状态变为"待评估"

#### Scenario: 撤回（待评估 → 已创建）
- **WHEN** 非 MANAGER 用户对"待评估"状态的需求点击"撤销"
- **THEN** 调用 POST /api/inbox/{id}/withdraw，状态回到"已创建"

#### Scenario: 评估通过（待评估 → 进入需求池）
- **WHEN** MANAGER/ADMIN 对"待评估"需求点击"需求评估"并选择通过
- **THEN** 状态变为"进入需求池"，自动弹出"完善需求信息"对话框

#### Scenario: 完善需求信息后进入需求进度列表
- **WHEN** MANAGER/ADMIN 在"完善需求信息"对话框填写并确认
- **THEN** 调用 PUT /api/requirements/{id} 更新需求信息，需求在需求进度列表可见

#### Scenario: 评估驳回（待评估 → 已驳回）
- **WHEN** MANAGER/ADMIN 选择驳回但未填写驳回意见
- **THEN** 前端阻止提交，提示驳回意见为必填

#### Scenario: 评估驳回成功
- **WHEN** MANAGER/ADMIN 填写驳回意见并确认驳回
- **THEN** 状态变为"已驳回"，rejectReason 保存到数据库

#### Scenario: 取消（已驳回 → 已取消）
- **WHEN** 用户对"已驳回"需求点击"取消"
- **THEN** 调用 POST /api/inbox/{id}/archive，状态变为"已取消"

---

### Requirement: 驳回意见展示
已驳回状态的需求在列表中直接显示驳回原因。

#### Scenario: 悬浮显示驳回意见
- **WHEN** 用户将鼠标悬停在"已驳回"状态标签旁的图标上
- **THEN** Tooltip 显示该需求的驳回意见内容

---

### Requirement: 按钮权限矩阵
不同提报状态下显示不同的操作按钮。

#### Scenario: 已创建状态按钮
- **WHEN** 需求处于"已创建"状态
- **THEN** 显示：详情、日志、编辑、提交（非MANAGER）、删除；ADMIN 始终可见编辑和删除

#### Scenario: 待评估状态按钮
- **WHEN** 需求处于"待评估"状态
- **THEN** 显示：详情、日志、撤销（非MANAGER）、需求评估（MANAGER/ADMIN）

#### Scenario: 已驳回状态按钮
- **WHEN** 需求处于"已驳回"状态
- **THEN** 显示：详情、日志、编辑、提交、取消

#### Scenario: 进入需求池/已取消状态按钮
- **WHEN** 需求处于"进入需求池"或"已取消"状态
- **THEN** 只显示：详情、日志

#### Scenario: ADMIN 特权
- **WHEN** 当前用户为 ADMIN 角色
- **THEN** 任意提报状态下均可看到编辑和删除按钮

---

### Requirement: 提报操作日志
每次状态变更和数据修改均记录日志。

#### Scenario: 查看操作日志
- **WHEN** 用户点击"日志"按钮
- **THEN** 弹框展示该提报需求的操作历史，含：创建、编辑、提交、撤回、评估通过/驳回、取消
