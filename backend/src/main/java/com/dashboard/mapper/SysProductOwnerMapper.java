package com.dashboard.mapper;

import com.dashboard.entity.SysProductOwner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysProductOwnerMapper {

    @Select("SELECT id, name, sort_order, created_at FROM sys_product_owner ORDER BY sort_order, id")
    List<SysProductOwner> findAll();

    @Insert("INSERT INTO sys_product_owner (name, sort_order) VALUES (#{name}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysProductOwner owner);

    @Update("UPDATE sys_product_owner SET name=#{name}, sort_order=#{sortOrder} WHERE id=#{id}")
    void update(SysProductOwner owner);

    @Delete("DELETE FROM sys_product_owner WHERE id=#{id}")
    void deleteById(Long id);
}
