package com.dashboard.mapper;

import com.dashboard.entity.Requirement;
import com.dashboard.dto.RequirementQueryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RequirementMapper {
    List<Requirement> findList(RequirementQueryDTO query);
    Requirement findById(Long id);
    int insert(Requirement requirement);
    int update(Requirement requirement);
    int deleteById(Long id);
}
