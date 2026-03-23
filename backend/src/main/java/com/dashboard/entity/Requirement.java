package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Requirement {
    private Long id;
    private String functionName;
    private String moduleName;
    private String requestDepartment;
    private String requestOwner;
    private String productOwner;
    private String priority;
    private LocalDate plannedStartTime;
    private LocalDate plannedEndTime;
    private LocalDate actualStartTime;
    private LocalDate actualEndTime;
    private String status;
    private String description;
    private LocalDate expectedOnlineDate;
    private String submissionStatus;
    private String submittedBy;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
