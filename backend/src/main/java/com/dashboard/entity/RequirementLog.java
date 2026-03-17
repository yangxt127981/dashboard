package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RequirementLog {
    private Long id;
    private Long requirementId;
    private String operator;
    private String operationType;  // 创建 / 编辑 / 删除
    private String beforeContent;
    private String afterContent;
    private LocalDateTime createdAt;
}
