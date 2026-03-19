package com.dashboard.mapper;

import com.dashboard.entity.SysDepartment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysDepartmentMapper {

    @Select("SELECT id, name, sort_order, created_at FROM sys_department ORDER BY sort_order, id")
    List<SysDepartment> findAll();

    @Insert("INSERT INTO sys_department (name, sort_order) VALUES (#{name}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysDepartment dept);

    @Update("UPDATE sys_department SET name=#{name}, sort_order=#{sortOrder} WHERE id=#{id}")
    void update(SysDepartment dept);

    @Delete("DELETE FROM sys_department WHERE id=#{id}")
    void deleteById(Long id);
}
