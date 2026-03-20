package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysRequestOwner {
    private Long id;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
