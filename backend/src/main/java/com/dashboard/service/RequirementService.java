package com.dashboard.service;

import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.github.pagehelper.PageInfo;

public interface RequirementService {
    PageInfo<Requirement> getList(RequirementQueryDTO query);
    Requirement getById(Long id);
    void create(Requirement requirement);
    void update(Requirement requirement);
    void delete(Long id);
}
