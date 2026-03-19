package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.LoginLog;
import com.dashboard.entity.User;
import com.dashboard.mapper.LoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/login-logs")
public class LoginLogController {

    private final LoginLogMapper loginLogMapper;

    public LoginLogController(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String loginType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");
        if (user == null || user.getPermissions() == null || !user.getPermissions().contains("system:login-log")) {
            return Result.error(403, "无权限操作");
        }

        long total = loginLogMapper.count(username, loginType);
        List<LoginLog> list = loginLogMapper.findPage(username, loginType, (page - 1) * size, size);

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("list", list);
        return Result.success(data);
    }
}
