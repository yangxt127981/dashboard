package com.dashboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Collections;
import java.util.Set;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role;
    private Long roleId;   // 自定义角色ID，内置角色为null

    // 不映射数据库，登录时由 PermissionService 填充，不序列化到 API 响应
    @JsonIgnore
    private Set<String> permissions = Collections.emptySet();
}
