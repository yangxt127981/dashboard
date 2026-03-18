package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.dto.IoaLoginDTO;
import com.dashboard.dto.LoginDTO;
import com.dashboard.entity.User;
import com.dashboard.mapper.UserMapper;
import com.dashboard.service.AuthService;
import com.dashboard.service.IoaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Set<String> MANAGER_USERS = Set.of("yangxiaotong", "zhaoyiqun", "dingying", "liuqiushi", "m81496", "m20506", "m00828");

    private final AuthService authService;
    private final IoaService ioaService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, IoaService ioaService, UserMapper userMapper) {
        this.authService = authService;
        this.ioaService = ioaService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        Map<String, Object> data = authService.login(dto.getUsername(), dto.getPassword());
        if (data == null) {
            return Result.error(400, "用户名或密码错误");
        }
        return Result.success(data);
    }

    @PostMapping("/ioa/login")
    public Result<?> ioaLogin(@RequestBody IoaLoginDTO dto) {
        if (dto.getUserId() == null || dto.getTicket() == null) {
            return Result.error(400, "参数缺失");
        }

        boolean valid = ioaService.checkTicket(dto.getUserId(), dto.getTicket());
        if (!valid) {
            return Result.error(401, "IOA 验证失败，请确认已登录 IOA 客户端");
        }

        // 查本地用户，不存在则自动创建
        User user = userMapper.findByUsername(dto.getUserId());
        if (user == null) {
            userMapper.insertIoaUser(dto.getUserId());
            user = userMapper.findByUsername(dto.getUserId());
        }

        // 特定 IOA 用户升级为 MANAGER（可编辑不可删除），仅影响内存中的 token，不修改 DB
        if (MANAGER_USERS.contains(dto.getUserId().toLowerCase()) && "USER".equals(user.getRole())) {
            User managerUser = new User();
            managerUser.setId(user.getId());
            managerUser.setUsername(user.getUsername());
            managerUser.setPassword(user.getPassword());
            managerUser.setRole("MANAGER");
            user = managerUser;
        }

        // 复用现有 token 机制
        String token = UUID.randomUUID().toString();
        authService.storeToken(token, user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return Result.success();
    }
}
