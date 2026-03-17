# 项目计划看板系统 - Claude 上下文

## 项目概述

内部项目需求看板系统，供信息部管理多部门需求，实现进度透明化。

- **代码仓库**：https://github.com/yangxt127981/dashboard（当前 Tag: V1）
- **本地路径**：/Users/hansonyang/Project/dashboard

---

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.3 + MyBatis + PageHelper |
| 语言 | Java 17 |
| 数据库 | MySQL（库名 dashboard_db）|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router 4 + Axios |

---

## 启动方式

```bash
# 1. 启动 MySQL（每次重启电脑后需执行）
mysqld_safe --datadir=/opt/homebrew/var/mysql &

# 2. 启动后端（必须指定 Java 17，系统默认 Java 25 会报错）
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

---

## 已实现功能（V1）

- 登录/登出（自有 Token 鉴权，UUID 存内存）
- 需求列表：分页、按需求名称/部门/状态筛选
- 表格列：需求名称、所属模块、需求方部门、需求对接人、产品对接人、优先级、计划开始、计划完成、实际开始、实际完成、状态、需求描述（无 ID 列）
- 后端全局排序：计划完成、实际完成（日期升降序）、优先级（紧急→高→中→低）
- 新增/编辑需求（弹窗表单，所有字段）
- 删除需求（二次确认）
- 操作按钮仅 ADMIN 可见，后端也有权限校验
- 状态/优先级用彩色 Tag 显示

---

## 数据库核心表

```sql
-- 需求表（主表）
requirement: id, function_name, module_name, request_department,
             request_owner, product_owner, priority, planned_start_time,
             planned_end_time, actual_start_time, actual_end_time,
             status, description, created_at, updated_at

-- 用户表（V1 自有，未来计划替换为公司统一用户体系）
user: id, username, password, role
```

---

## 关键文件

| 文件 | 说明 |
|------|------|
| backend/src/main/resources/application.yml | 数据库配置（root 密码为空）|
| backend/src/main/resources/sql/init.sql | 建表 + 初始数据 |
| backend/src/main/resources/mapper/RequirementMapper.xml | 查询 SQL + 排序逻辑 |
| backend/src/main/java/com/dashboard/config/AuthInterceptor.java | Token 验证拦截器 |
| backend/src/main/java/com/dashboard/dto/RequirementQueryDTO.java | 查询参数（含排序字段）|
| frontend/src/views/Board.vue | 主看板页（列表、筛选、弹窗全在这里）|
| frontend/src/views/Login.vue | 登录页 |
| frontend/src/api/axios.js | Axios 实例 + 全局拦截器 |
| frontend/src/stores/auth.js | Pinia 登录状态管理 |

---

## 未来计划（尚未实施）

### 系统集成
计划将 dashboard 集成到公司现有系统（Spring Boot + Vue + JWT + RBAC），复用用户/部门/角色/权限。
- 方案：JWT Token 共享 + RBAC 权限打通
- 需移除自有登录模块，改为解析公司统一 JWT
- 新增权限标识：dashboard:view、dashboard:manage
- 部门下拉改为调用公司部门接口
- **待确认**：公司 JWT secret/payload 结构、部门 API、是否同域部署

### V2 功能
> 待用户提供设计文档
