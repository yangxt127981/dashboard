package com.dashboard.mapper;

import com.dashboard.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(String username);

    @Insert("INSERT INTO `user` (username, password, role) VALUES (#{username}, 'IOA_SSO', 'USER')")
    void insertIoaUser(@Param("username") String username);

    // 查询非 IOA 用户（密码不为 IOA_SSO）
    @Select("SELECT id, username, role FROM `user` WHERE password != 'IOA_SSO' ORDER BY id")
    List<User> findNonIoaUsers();

    @Select("SELECT id, username, role FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO `user` (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("<script>UPDATE `user` SET username=#{username}, role=#{role}" +
            "<if test='password != null and password != \"\"'>, password=#{password}</if>" +
            " WHERE id=#{id}</script>")
    void update(User user);

    @Delete("DELETE FROM `user` WHERE id=#{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM `user` WHERE username=#{username} AND id != #{excludeId}")
    int countByUsernameExclude(@Param("username") String username, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM `user` WHERE username=#{username}")
    int countByUsername(@Param("username") String username);
}
