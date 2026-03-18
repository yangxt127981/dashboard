package com.dashboard.mapper;

import com.dashboard.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(String username);

    @Insert("INSERT INTO `user` (username, password, role) VALUES (#{username}, 'IOA_SSO', 'USER')")
    void insertIoaUser(@Param("username") String username);
}
