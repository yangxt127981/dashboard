package com.dashboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class RequirementQueryDTO {
    private String functionName;
    private String moduleName;
    private String requestDepartment;
    private String requestOwner;
    private String productOwner;
    private List<String> status;
    private List<String> priority;
    private String submissionStatus;
    private String submittedBy;
    private int page = 1;
    private int size = 10;
    private String sortField;
    private String sortOrder;
}
