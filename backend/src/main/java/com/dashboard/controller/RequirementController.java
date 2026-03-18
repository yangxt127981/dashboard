package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.entity.RequirementLog;
import com.dashboard.entity.User;
import com.dashboard.mapper.RequirementMapper;
import com.dashboard.service.RequirementService;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;
    private final RequirementMapper requirementMapper;

    public RequirementController(RequirementService requirementService, RequirementMapper requirementMapper) {
        this.requirementService = requirementService;
        this.requirementMapper = requirementMapper;
    }

    @GetMapping("/stats")
    public Result<?> stats(@RequestParam(required = false) String department) {
        Map<String, Object> result = new HashMap<>();
        result.put("department", requirementMapper.statsByDepartment());
        result.put("priority", requirementMapper.statsByPriority(department));
        result.put("status", requirementMapper.statsByStatus(department));
        return Result.success(result);
    }

    @GetMapping
    public Result<?> list(RequirementQueryDTO query) {
        PageInfo<Requirement> pageInfo = requirementService.getList(query);
        return Result.success(pageInfo);
    }

    @GetMapping("/{id}/logs")
    public Result<?> logs(@PathVariable Long id) {
        List<RequirementLog> logs = requirementService.getLogs(id);
        return Result.success(logs);
    }

    @PostMapping
    public Result<?> create(@RequestBody Requirement requirement, HttpServletRequest request) {
        if (!isAdminOrManager(request)) {
            return Result.error(403, "无权限操作");
        }
        try {
            requirementService.create(requirement, getOperator(request));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
        return Result.success(requirement.getId());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Requirement requirement, HttpServletRequest request) {
        if (!isAdminOrManager(request)) {
            return Result.error(403, "无权限操作");
        }
        requirement.setId(id);
        requirementService.update(requirement, getOperator(request));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        requirementService.delete(id, getOperator(request));
        return Result.success();
    }

    private String getOperator(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getUsername() : "unknown";
    }

    private boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    private boolean isAdminOrManager(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && ("ADMIN".equals(user.getRole()) || "MANAGER".equals(user.getRole()));
    }
}
