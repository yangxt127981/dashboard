package com.dashboard.mapper;

import com.dashboard.entity.LoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LoginLogMapper {

    @Insert("INSERT INTO login_log (username, login_type, login_ip, user_agent, login_time, status) " +
            "VALUES (#{username}, #{loginType}, #{loginIp}, #{userAgent}, #{loginTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LoginLog log);

    @Update("UPDATE login_log SET logout_time=#{logoutTime}, duration_minutes=#{durationMinutes}, status='已退出' WHERE id=#{id}")
    void updateLogout(LoginLog log);

    @Select("<script>SELECT COUNT(*) FROM login_log" +
            "<where>" +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%')</if>" +
            "<if test='loginType != null and loginType != \"\"'>AND login_type = #{loginType}</if>" +
            "</where></script>")
    long count(@Param("username") String username, @Param("loginType") String loginType);

    @Select("<script>SELECT * FROM login_log" +
            "<where>" +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%')</if>" +
            "<if test='loginType != null and loginType != \"\"'>AND login_type = #{loginType}</if>" +
            "</where>" +
            "ORDER BY login_time DESC LIMIT #{offset}, #{size}</script>")
    List<LoginLog> findPage(@Param("username") String username,
                            @Param("loginType") String loginType,
                            @Param("offset") int offset,
                            @Param("size") int size);
}
