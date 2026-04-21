## ADDED Requirements

### Requirement: 需求列表展示
需求进度看板以分页表格展示 submission_status 为 NULL 或"进入需求池"的需求。

#### Scenario: 分页加载
- **WHEN** 用户进入需求进度页面
- **THEN** 默认加载第 1 页，每页 10 条，显示总记录数

#### Scenario: Tab 页切换
- **WHEN** 用户点击 Tab（全部 / 进行中 / 未开始 / 已上线 / 已取消）
- **THEN** 列表过滤为对应状态，Tab 标签实时显示各状态记录数

---

### Requirement: 需求筛选
支持多条件组合筛选，高级查询区域可折叠展开。

#### Scenario: 按需求名称模糊搜索
- **WHEN** 用户在搜索框输入关键词
- **THEN** 返回 functionName 包含该关键词的记录

#### Scenario: 按部门/模块/优先级/状态/产品对接人筛选
- **WHEN** 用户在下拉框或多选框选择筛选条件
- **THEN** 后端组合过滤，返回匹配结果

---

### Requirement: 需求排序
支持多字段升降序排序，在后端执行。

#### Scenario: 点击列头排序
- **WHEN** 用户点击优先级、计划开始/完成时间、实际开始/完成时间、创建时间、更新时间列头
- **THEN** 后端按选中字段和方向排序后返回，优先级按 紧急→高→中→低 顺序

---

### Requirement: 列显示配置
用户可自定义显示哪些列，配置持久化到 localStorage。

#### Scenario: 隐藏/显示列
- **WHEN** 用户通过列配置面板勾选/取消某列
- **THEN** 表格立即更新，配置写入 localStorage，刷新后保持

---

### Requirement: 新增需求
有 requirement:create 权限的用户可新建需求。

#### Scenario: 新建成功
- **WHEN** 用户填写需求名称（必填）等字段并提交
- **THEN** 调用 POST /api/requirements，需求出现在列表

#### Scenario: 新建时自动预填需求对接人
- **WHEN** 当前登录用户名存在于需求对接人列表中
- **THEN** 新建表单的"需求对接人"字段自动预填为当前用户

#### Scenario: 需求名称为空时拒绝提交
- **WHEN** 用户提交空的需求名称
- **THEN** 后端返回 400，前端提示必填

---

### Requirement: 编辑需求
有 requirement:edit 权限的用户可编辑现有需求。

#### Scenario: 编辑成功
- **WHEN** 用户修改字段并提交
- **THEN** 调用 PUT /api/requirements/{id}，列表刷新显示最新数据

---

### Requirement: 取消需求
有 requirement:cancel 权限的用户可取消状态不为"已取消"的需求。

#### Scenario: 取消按钮可见性
- **WHEN** 需求状态已经是"已取消"
- **THEN** 操作栏不显示取消按钮

---

### Requirement: 删除需求
仅 ADMIN 角色（有 requirement:delete 权限）可删除需求。

#### Scenario: 删除成功
- **WHEN** ADMIN 用户点击删除并确认
- **THEN** 调用 DELETE /api/requirements/{id}，需求从列表移除

---

### Requirement: 需求详情
所有登录用户可查看需求详情，ADMIN/MANAGER 可从详情页快捷跳转编辑。

#### Scenario: 查看详情
- **WHEN** 用户点击"详情"按钮
- **THEN** 弹框展示全部字段及附件图片

---

### Requirement: 操作日志
每次增删改均自动记录操作日志，包含变更前后 JSON 差异对比。

#### Scenario: 查看操作日志
- **WHEN** 用户点击"日志"按钮
- **THEN** 弹框展示该需求的全部操作记录，含操作人、操作类型、变更内容

---

### Requirement: 图片附件
每个需求支持上传最多 5 张图片（PNG/JPG，≤10MB）。

#### Scenario: 上传图片
- **WHEN** 用户选择或粘贴图片（Ctrl+V / Cmd+V）
- **THEN** 图片上传到服务端 /uploads/ 目录，关联到需求；已上传 5 张时隐藏上传按钮

#### Scenario: 格式或大小不合规
- **WHEN** 用户上传非 PNG/JPG 或超过 10MB 的文件
- **THEN** 前端 beforeUpload 校验拒绝，后端也校验 ContentType

---

### Requirement: 数据统计看板
以三个饼图展示需求分布统计，支持联动过滤。

#### Scenario: 展示饼图
- **WHEN** 用户展开统计看板区域
- **THEN** 显示"部门分布 / 优先级分布 / 状态分布"三个饼图，状态图中心显示需求总数

#### Scenario: 三图联动
- **WHEN** 用户点击部门分布图中某个部门
- **THEN** 优先级图和状态图随之过滤为该部门的数据；再次点击取消联动
