package com.dashboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginLog {
    private Long id;
    private String username;
    private String loginType;       // 账号密码 / IOA
    private String loginIp;
    private String userAgent;       // 浏览器/客户端
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private Integer durationMinutes; // 停留时长（分钟）
    private String status;          // 在线 / 已退出
}
