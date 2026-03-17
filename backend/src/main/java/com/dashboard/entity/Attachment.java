package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Attachment {
    private Long id;
    private Long requirementId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime createdAt;
}
