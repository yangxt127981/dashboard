package com.dashboard.service.impl;

import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.entity.RequirementLog;
import com.dashboard.mapper.RequirementLogMapper;
import com.dashboard.mapper.RequirementMapper;
import com.dashboard.service.RequirementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequirementServiceImpl implements RequirementService {

    private final RequirementMapper requirementMapper;
    private final RequirementLogMapper requirementLogMapper;
    private final ObjectMapper objectMapper;

    public RequirementServiceImpl(RequirementMapper requirementMapper,
                                   RequirementLogMapper requirementLogMapper,
                                   ObjectMapper objectMapper) {
        this.requirementMapper = requirementMapper;
        this.requirementLogMapper = requirementLogMapper;
        this.objectMapper = objectMapper;
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
    public void create(Requirement requirement, String operator) {
        if (requirement.getFunctionName() == null || requirement.getFunctionName().isBlank()) {
            throw new IllegalArgumentException("需求名称不能为空");
        }
        requirement.setStatus(requirement.getStatus() == null ? "未开始" : requirement.getStatus());
        requirementMapper.insert(requirement);
        writeLog(requirement.getId(), operator, "创建", null, requirement);
    }

    @Override
    public void update(Requirement requirement, String operator) {
        Requirement before = requirementMapper.findById(requirement.getId());
        requirementMapper.update(requirement);
        Requirement after = requirementMapper.findById(requirement.getId());
        writeLog(requirement.getId(), operator, "编辑", before, after);
    }

    @Override
    public void delete(Long id, String operator) {
        Requirement before = requirementMapper.findById(id);
        requirementMapper.deleteById(id);
        writeLog(id, operator, "删除", before, null);
    }

    @Override
    public List<RequirementLog> getLogs(Long requirementId) {
        return requirementLogMapper.findByRequirementId(requirementId);
    }

    private void writeLog(Long requirementId, String operator, String type,
                          Requirement before, Requirement after) {
        try {
            RequirementLog log = new RequirementLog();
            log.setRequirementId(requirementId);
            log.setOperator(operator);
            log.setOperationType(type);
            log.setBeforeContent(before == null ? null : objectMapper.writeValueAsString(before));
            log.setAfterContent(after == null ? null : objectMapper.writeValueAsString(after));
            requirementLogMapper.insert(log);
        } catch (Exception ignored) {}
    }
}
