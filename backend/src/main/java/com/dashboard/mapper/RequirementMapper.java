package com.dashboard.mapper;

import com.dashboard.entity.Requirement;
import com.dashboard.dto.RequirementQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RequirementMapper {
    List<Requirement> findList(RequirementQueryDTO query);
    Requirement findById(Long id);
    int insert(Requirement requirement);
    int update(Requirement requirement);
    int deleteById(Long id);
    List<Map<String, Object>> countByStatus(RequirementQueryDTO query);
    List<Map<String, Object>> statsByDepartment();
    List<Map<String, Object>> statsByPriority(@Param("department") String department);
    List<Map<String, Object>> statsByStatus(@Param("department") String department);
}
