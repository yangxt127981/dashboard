package com.dashboard.service;

import com.dashboard.entity.SysPermission;
import com.dashboard.entity.User;
import com.dashboard.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionService {

    private static final Set<String> ALL_PERMISSIONS = Set.of(
        "board:view",
        "requirement:view", "requirement:create", "requirement:edit", "requirement:cancel", "requirement:delete",
        "system:dept",        "dept:view",    "dept:create",   "dept:edit",   "dept:delete",
        "system:module",      "module:view",  "module:create", "module:edit", "module:delete",
        "system:user",        "user:view",    "user:create",   "user:edit",   "user:delete",
        "system:login-log",
        "system:role",        "role:create",  "role:edit",     "role:delete",
        "system:requestowner"
    );

    private static final Set<String> MANAGER_PERMISSIONS = Set.of(
        "board:view",
        "requirement:view", "requirement:create", "requirement:edit", "requirement:cancel",
        "system:requestowner",
        "system:module", "module:view", "module:create", "module:edit", "module:delete",
        "system:dept", "dept:view", "dept:create", "dept:edit", "dept:delete",
        "system:login-log"
    );

    private static final Set<String> USER_PERMISSIONS = Set.of("board:view", "requirement:view");

    private final SysPermissionMapper permissionMapper;

    public PermissionService(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public Set<String> getPermissions(User user) {
        if ("ADMIN".equals(user.getRole())) return ALL_PERMISSIONS;
        if ("MANAGER".equals(user.getRole())) return MANAGER_PERMISSIONS;
        if ("USER".equals(user.getRole())) return USER_PERMISSIONS;
        if (user.getRoleId() != null) {
            return permissionMapper.findCodesByRoleId(user.getRoleId());
        }
        return Collections.emptySet();
    }

    /** 构建权限树（用于前端角色管理页） */
    public List<SysPermission> getPermissionTree() {
        List<SysPermission> all = permissionMapper.findAll();
        Map<Long, SysPermission> map = new LinkedHashMap<>();
        for (SysPermission p : all) {
            p.setChildren(new ArrayList<>());
            map.put(p.getId(), p);
        }
        List<SysPermission> roots = new ArrayList<>();
        for (SysPermission p : all) {
            if (p.getParentId() == null) {
                roots.add(p);
            } else {
                SysPermission parent = map.get(p.getParentId());
                if (parent != null) parent.getChildren().add(p);
            }
        }
        return roots;
    }
}
