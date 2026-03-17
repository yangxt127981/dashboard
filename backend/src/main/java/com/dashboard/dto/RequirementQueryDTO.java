package com.dashboard.dto;

import lombok.Data;

@Data
public class RequirementQueryDTO {
    private String functionName;
    private String requestDepartment;
    private String status;
    private int page = 1;
    private int size = 10;
}
