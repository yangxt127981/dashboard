package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysRole {
    private Long id;
    private String name;
    private String code;
    private Integer builtIn;
    private String remark;
    private LocalDateTime createdAt;
}
