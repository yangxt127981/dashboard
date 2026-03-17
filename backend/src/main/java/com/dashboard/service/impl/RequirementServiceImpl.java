package com.dashboard.service.impl;

import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.mapper.RequirementMapper;
import com.dashboard.service.RequirementService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequirementServiceImpl implements RequirementService {

    private final RequirementMapper requirementMapper;

    public RequirementServiceImpl(RequirementMapper requirementMapper) {
        this.requirementMapper = requirementMapper;
    }

    @Override
    public PageInfo<Requirement> getList(RequirementQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getSize());
        List<Requirement> list = requirementMapper.findList(query);
        return new PageInfo<>(list);
    }

    @Override
    public Requirement getById(Long id) {
        return requirementMapper.findById(id);
    }

    @Override
    public void create(Requirement requirement) {
        requirement.setStatus(requirement.getStatus() == null ? "未开始" : requirement.getStatus());
        requirementMapper.insert(requirement);
    }

    @Override
    public void update(Requirement requirement) {
        requirementMapper.update(requirement);
    }

    @Override
    public void delete(Long id) {
        requirementMapper.deleteById(id);
    }
}
