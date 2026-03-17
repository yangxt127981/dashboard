package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.dto.LoginDTO;
import com.dashboard.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        Map<String, Object> data = authService.login(dto.getUsername(), dto.getPassword());
        if (data == null) {
            return Result.error(400, "用户名或密码错误");
        }
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return Result.success();
    }
}
