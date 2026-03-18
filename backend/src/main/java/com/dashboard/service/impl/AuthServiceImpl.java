package com.dashboard.service.impl;

import com.dashboard.entity.User;
import com.dashboard.mapper.UserMapper;
import com.dashboard.service.AuthService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final ConcurrentHashMap<String, User> tokenStore = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public void logout(String token) {
        tokenStore.remove(token);
    }

    @Override
    public User getUserByToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public void storeToken(String token, User user) {
        tokenStore.put(token, user);
    }
}
