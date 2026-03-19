package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.User;
import com.dashboard.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<?> list(HttpServletRequest request) {
        if (!hasPermission(request, "user:view")) return Result.error(403, "无权限操作");
        return Result.success(userMapper.findNonIoaUsers());
    }

    @PostMapping
    public Result<?> create(@RequestBody User user, HttpServletRequest request) {
        if (!hasPermission(request, "user:create")) return Result.error(403, "无权限操作");
        if (blank(user.getUsername())) return Result.error(400, "用户名不能为空");
        if (blank(user.getPassword())) return Result.error(400, "密码不能为空");
        if (blank(user.getRole()) && user.getRoleId() == null) return Result.error(400, "角色不能为空");
        if (userMapper.countByUsername(user.getUsername()) > 0)
            return Result.error(400, "用户名已存在");
        userMapper.insert(user);
        return Result.success(user.getId());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        if (!hasPermission(request, "user:edit")) return Result.error(403, "无权限操作");
        if (blank(user.getUsername())) return Result.error(400, "用户名不能为空");
        if (userMapper.countByUsernameExclude(user.getUsername(), id) > 0)
            return Result.error(400, "用户名已被占用");
        user.setId(id);
        userMapper.update(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "user:delete")) return Result.error(403, "无权限操作");
        User current = (User) request.getAttribute("currentUser");
        if (current != null && current.getId().equals(id))
            return Result.error(400, "不能删除当前登录账号");
        userMapper.deleteById(id);
        return Result.success();
    }

    private boolean hasPermission(HttpServletRequest request, String code) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && user.getPermissions() != null && user.getPermissions().contains(code);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
