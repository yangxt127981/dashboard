package com.dashboard.service;

import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.entity.RequirementLog;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface RequirementService {
    PageInfo<Requirement> getList(RequirementQueryDTO query);
    Requirement getById(Long id);
    void create(Requirement requirement, String operator);
    void update(Requirement requirement, String operator);
    void delete(Long id, String operator);
    List<RequirementLog> getLogs(Long requirementId);
}
