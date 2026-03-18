# IOA 登录集成方案

## 流程概述

```
[前端] 点击"IOA 一键登录"
    → 调用本机 IOA 客户端接口（sso.wawo.cc:54339）获取 Ticket
    → 将 Ticket + UserID 发送给我们的后端
[后端] 调用 IOA 验签接口（ioa.wawo.cc:27800）校验 Ticket
    → 按工号查本地 user 表
    → 找不到 → 自动创建（role 默认 USER）
    → 生成 UUID token → 返回前端
[前端] 拿到 token/username/role → 进入看板
```

---

## 不需要改动的部分

- `AuthInterceptor`、`AuthService`（token 机制不变）
- 现有账号密码登录（两种方式并存）
- 前端 axios 拦截器、auth store

---

## 实施任务

### Task 1：后端 — 配置文件

`application.yml` 新增 IOA 配置项：

```yaml
ioa:
  host: https://ioa.wawo.cc:27800
  appid: f22a6b50-f44b-4ae3-81d8-f9100a36405a
  secret-key: $2a$10$7in4LrZc71uDlZ7MdxRWquHlu7rLYFdR4bLYeYJ3izh1pTq8pNgX6
  secret-id: fcd51120db1a11f086c175cafcfa317a
  api-version: 2022-06-01
```

---

### Task 2：后端 — IoaService

新建 `IoaService`，封装 IOA 接口调用：

- `checkTicket(userId, ticket)`：调用 `/OpenApi/V1/ClientLogin/SSO/CheckTicket`，返回验证是否通过
- `getUserInfo(userId)`（可选）：获取用户姓名、部门，用于自动创建账号时填充

参考实现：

```java
public boolean checkTicket(String userId, String ticket) {
    JSONObject json = new JSONObject();
    json.set("Ticket", ticket);
    json.set("UserID", userId);
    json.set("AppID", appId);

    String result = callApi("/OpenApi/V1/ClientLogin/SSO/CheckTicket", json.toString());
    return result.contains("true");
}
```

---

### Task 3：后端 — AuthController 新增接口

```
POST /api/auth/ioa/login
Body: { "userId": "工号", "ticket": "xxx" }
```

处理逻辑：

1. 调 `IoaService.checkTicket()` → 失败返回 401
2. 按 `username = userId` 查本地 `user` 表
3. 找不到 → 自动 INSERT（role = USER）
4. 生成 UUID token → 写内存 tokenStore → 返回 `{ token, username, role }`

---

### Task 4：数据库

User 表**无需新增字段**，直接用现有 `username` 字段存储工号，与 IOA 的 `UserID` 对应。

> 前提：确认现有 `username` 字段长度（VARCHAR 64 以上）及 IOA 工号与本地账号是否同一套体系。

---

### Task 5：前端 — Login.vue

在密码登录下方新增「IOA 一键登录」按钮，点击执行以下逻辑：

1. 请求本机 IOA 客户端获取 Ticket：
   ```
   GET https://sso.wawo.cc:54339/api/public/clientlogin/auth_login?app_id=xxx&sole_id=xxx
   ```
2. 解析返回 ticket 中的 `sub` 字段（即工号 UserID）
3. 调用后端接口：`POST /api/auth/ioa/login`
4. 成功 → 写入 localStorage → 跳转 `/board`

---

### Task 6：前端 — 错误处理

| 场景 | 用户提示 |
|---|---|
| 未安装或未登录 IOA 客户端 | "请先登录 IOA 客户端" |
| IOA 验签失败 | "IOA 验证失败，请重试" |
| 工号不在系统中 | 自动创建账号后正常登录 |
| 网络超时 | "网络错误，请稍后重试" |

---

## 涉及文件清单

| 文件 | 变更类型 | 说明 |
|---|---|---|
| `backend/.../resources/application.yml` | 修改 | 新增 IOA 配置项 |
| `backend/.../service/IoaService.java` | 新增 | IOA API 封装（checkTicket 等）|
| `backend/.../controller/AuthController.java` | 修改 | 新增 `/api/auth/ioa/login` 接口 |
| `frontend/src/views/Login.vue` | 修改 | 新增 IOA 一键登录按钮及逻辑 |

---

## 待确认事项

1. `sso.wawo.cc:54339` 是否为员工本机 IOA 客户端地址？前端直接 fetch 是否存在 CORS 限制？
2. IOA 返回的 `UserID`（工号）与现有 `user` 表的 `username` 是否为同一套账号体系？
