package com.dashboard.service.impl;

import com.dashboard.entity.LoginLog;
import com.dashboard.entity.User;
import com.dashboard.mapper.LoginLogMapper;
import com.dashboard.mapper.UserMapper;
import com.dashboard.service.AuthService;
import com.dashboard.service.PermissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final PermissionService permissionService;
    private final ConcurrentHashMap<String, User> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tokenToLogId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> tokenToLoginTime = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserMapper userMapper, LoginLogMapper loginLogMapper, PermissionService permissionService) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.permissionService = permissionService;
    }

    @Override
    public Map<String, Object> login(String username, String password, String ip, String userAgent) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        user.setPermissions(permissionService.getPermissions(user));
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user);
        recordLogin(token, user.getUsername(), "账号密码", ip, userAgent);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("roleId", user.getRoleId());
        result.put("permissions", user.getPermissions());
        return result;
    }

    @Override
    public void logout(String token) {
        Long logId = tokenToLogId.remove(token);
        LocalDateTime loginTime = tokenToLoginTime.remove(token);
        if (logId != null) {
            LocalDateTime logoutTime = LocalDateTime.now();
            LoginLog log = new LoginLog();
            log.setId(logId);
            log.setLogoutTime(logoutTime);
            log.setDurationMinutes(loginTime != null ? (int) ChronoUnit.MINUTES.between(loginTime, logoutTime) : null);
            loginLogMapper.updateLogout(log);
        }
        tokenStore.remove(token);
    }

    @Override
    public User getUserByToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public void storeToken(String token, User user, String ip, String loginType, String userAgent) {
        user.setPermissions(permissionService.getPermissions(user));
        tokenStore.put(token, user);
        recordLogin(token, user.getUsername(), loginType, ip, userAgent);
    }

    private void recordLogin(String token, String username, String loginType, String ip, String userAgent) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setLoginType(loginType);
        log.setLoginIp(ip);
        log.setUserAgent(userAgent != null && userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent);
        log.setLoginTime(LocalDateTime.now());
        log.setStatus("在线");
        loginLogMapper.insert(log);
        tokenToLogId.put(token, log.getId());
        tokenToLoginTime.put(token, log.getLoginTime());
    }
}
