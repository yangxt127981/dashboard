package com.dashboard.mapper;

import com.dashboard.entity.SysModule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysModuleMapper {

    @Select("SELECT id, name, sort_order, bg_color, created_at FROM sys_module ORDER BY sort_order, id")
    List<SysModule> findAll();

    @Insert("INSERT INTO sys_module (name, sort_order, bg_color) VALUES (#{name}, #{sortOrder}, #{bgColor})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysModule module);

    @Update("UPDATE sys_module SET name=#{name}, sort_order=#{sortOrder}, bg_color=#{bgColor} WHERE id=#{id}")
    void update(SysModule module);

    @Delete("DELETE FROM sys_module WHERE id=#{id}")
    void deleteById(Long id);
}
