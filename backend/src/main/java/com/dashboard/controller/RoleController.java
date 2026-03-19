package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.SysRole;
import com.dashboard.entity.User;
import com.dashboard.mapper.SysRoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final SysRoleMapper roleMapper;

    public RoleController(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public Result<?> list(HttpServletRequest request) {
        // 有角色管理权限，或有用户管理权限（需要角色列表来分配用户角色）
        if (!hasPermission(request, "system:role")
                && !hasPermission(request, "user:create")
                && !hasPermission(request, "user:edit")) {
            return Result.error(403, "无权限操作");
        }
        return Result.success(roleMapper.findAll());
    }

    @PostMapping
    public Result<?> create(@RequestBody SysRole role, HttpServletRequest request) {
        if (!hasPermission(request, "role:create")) return Result.error(403, "无权限操作");
        if (blank(role.getName())) return Result.error(400, "角色名称不能为空");
        if (blank(role.getCode())) return Result.error(400, "角色编码不能为空");
        if (roleMapper.countByCode(role.getCode()) > 0) return Result.error(400, "角色编码已存在");
        roleMapper.insert(role);
        return Result.success(role.getId());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysRole role, HttpServletRequest request) {
        if (!hasPermission(request, "role:edit")) return Result.error(403, "无权限操作");
        if (blank(role.getName())) return Result.error(400, "角色名称不能为空");
        if (blank(role.getCode())) return Result.error(400, "角色编码不能为空");
        if (roleMapper.countByCodeExclude(role.getCode(), id) > 0) return Result.error(400, "角色编码已被占用");
        role.setId(id);
        roleMapper.update(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "role:delete")) return Result.error(403, "无权限操作");
        SysRole role = roleMapper.findById(id);
        if (role == null) return Result.error(404, "角色不存在");
        if (role.getBuiltIn() != null && role.getBuiltIn() == 1) return Result.error(400, "内置角色不可删除");
        roleMapper.deleteById(id);
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
