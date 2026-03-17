package com.dashboard.mapper;

import com.dashboard.entity.RequirementLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RequirementLogMapper {
    int insert(RequirementLog log);
    List<RequirementLog> findByRequirementId(Long requirementId);
}
