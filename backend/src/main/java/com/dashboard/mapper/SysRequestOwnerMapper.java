package com.dashboard.mapper;

import com.dashboard.entity.SysRequestOwner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRequestOwnerMapper {

    @Select("SELECT id, name, sort_order, created_at FROM sys_request_owner ORDER BY sort_order, id")
    List<SysRequestOwner> findAll();

    @Insert("INSERT INTO sys_request_owner (name, sort_order) VALUES (#{name}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysRequestOwner owner);

    @Update("UPDATE sys_request_owner SET name=#{name}, sort_order=#{sortOrder} WHERE id=#{id}")
    void update(SysRequestOwner owner);

    @Delete("DELETE FROM sys_request_owner WHERE id=#{id}")
    void deleteById(Long id);
}
