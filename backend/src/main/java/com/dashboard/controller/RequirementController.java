package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.entity.User;
import com.dashboard.service.RequirementService;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public Result<?> list(RequirementQueryDTO query) {
        PageInfo<Requirement> pageInfo = requirementService.getList(query);
        return Result.success(pageInfo);
    }

    @PostMapping
    public Result<?> create(@RequestBody Requirement requirement, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        requirementService.create(requirement);
        return Result.success(requirement.getId());
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Requirement requirement, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        requirement.setId(id);
        requirementService.update(requirement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(403, "无权限操作");
        }
        requirementService.delete(id);
        return Result.success();
    }

    private boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}
