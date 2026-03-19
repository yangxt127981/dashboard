package com.dashboard.service;

import com.dashboard.entity.User;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password, String ip, String userAgent);
    void logout(String token);
    User getUserByToken(String token);
    void storeToken(String token, User user, String ip, String loginType, String userAgent);
}
