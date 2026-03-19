package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.User;
import com.dashboard.mapper.SysPermissionMapper;
import com.dashboard.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final SysPermissionMapper permissionMapper;

    public PermissionController(PermissionService permissionService, SysPermissionMapper permissionMapper) {
        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }

    /** 获取完整权限树（用于角色授权页面） */
    @GetMapping("/tree")
    public Result<?> tree(HttpServletRequest request) {
        if (!hasPermission(request, "system:role")) return Result.error(403, "无权限操作");
        return Result.success(permissionService.getPermissionTree());
    }

    /** 查询某角色已勾选的权限 ID 列表 */
    @GetMapping("/role/{roleId}")
    public Result<?> rolePermissions(@PathVariable Long roleId, HttpServletRequest request) {
        if (!hasPermission(request, "system:role")) return Result.error(403, "无权限操作");
        return Result.success(permissionMapper.findPermissionIdsByRoleId(roleId));
    }

    /** 保存角色权限（全量覆盖） */
    @PostMapping("/role/{roleId}")
    public Result<?> saveRolePermissions(@PathVariable Long roleId,
                                         @RequestBody Map<String, List<Long>> body,
                                         HttpServletRequest request) {
        if (!hasPermission(request, "role:edit")) return Result.error(403, "无权限操作");
        List<Long> permissionIds = body.get("permissionIds");
        permissionMapper.deleteRolePermissions(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissionMapper.insertRolePermissions(roleId, permissionIds);
        }
        return Result.success();
    }

    private boolean hasPermission(HttpServletRequest request, String code) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && user.getPermissions() != null && user.getPermissions().contains(code);
    }
}
