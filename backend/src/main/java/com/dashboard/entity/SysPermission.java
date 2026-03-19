package com.dashboard.entity;

import lombok.Data;
import java.util.List;

@Data
public class SysPermission {
    private Long id;
    private String name;
    private String code;
    private String type;       // MENU / BUTTON
    private Long parentId;
    private Integer sortOrder;
    // 树形结构用，不映射数据库
    private List<SysPermission> children;
}
