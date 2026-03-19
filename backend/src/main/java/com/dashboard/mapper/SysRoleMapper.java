package com.dashboard.mapper;

import com.dashboard.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    @Select("SELECT id, name, code, built_in, remark, created_at FROM sys_role ORDER BY id")
    List<SysRole> findAll();

    @Select("SELECT id, name, code, built_in, remark, created_at FROM sys_role WHERE id = #{id}")
    SysRole findById(Long id);

    @Insert("INSERT INTO sys_role (name, code, built_in, remark) VALUES (#{name}, #{code}, 0, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysRole role);

    @Update("UPDATE sys_role SET name=#{name}, code=#{code}, remark=#{remark} WHERE id=#{id} AND built_in=0")
    void update(SysRole role);

    @Delete("DELETE FROM sys_role WHERE id=#{id} AND built_in=0")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_role WHERE code=#{code}")
    int countByCode(String code);

    @Select("SELECT COUNT(*) FROM sys_role WHERE code=#{code} AND id != #{excludeId}")
    int countByCodeExclude(@Param("code") String code, @Param("excludeId") Long excludeId);
}
