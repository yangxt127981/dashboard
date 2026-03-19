package com.dashboard.mapper;

import com.dashboard.entity.SysPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysPermissionMapper {

    @Select("SELECT id, name, code, type, parent_id, sort_order FROM sys_permission ORDER BY sort_order, id")
    List<SysPermission> findAll();

    @Select("SELECT p.code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} " +
            "UNION " +
            "SELECT parent.code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_permission parent ON p.parent_id = parent.id " +
            "WHERE rp.role_id = #{roleId}")
    Set<String> findCodesByRoleId(Long roleId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> findPermissionIdsByRoleId(Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(Long roleId);

    @Insert("<script>INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permissionIds' item='pid' separator=','>" +
            "(#{roleId}, #{pid})" +
            "</foreach></script>")
    void insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
