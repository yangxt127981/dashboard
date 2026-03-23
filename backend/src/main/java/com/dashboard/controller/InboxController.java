package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.dto.RequirementQueryDTO;
import com.dashboard.entity.Requirement;
import com.dashboard.entity.RequirementLog;
import com.dashboard.entity.User;
import com.dashboard.mapper.RequirementLogMapper;
import com.dashboard.mapper.RequirementMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inbox")
public class InboxController {

    private final RequirementMapper requirementMapper;
    private final RequirementLogMapper requirementLogMapper;
    private final ObjectMapper objectMapper;

    public InboxController(RequirementMapper requirementMapper,
                           RequirementLogMapper requirementLogMapper,
                           ObjectMapper objectMapper) {
        this.requirementMapper = requirementMapper;
        this.requirementLogMapper = requirementLogMapper;
        this.objectMapper = objectMapper;
    }

    /** 提报需求列表（分页） */
    @GetMapping
    public Result<?> list(RequirementQueryDTO query, HttpServletRequest request) {
        String scope = dataScope(request);
        if (scope != null) query.setSubmittedBy(scope);
        PageHelper.startPage(query.getPage(), query.getSize());
        List<Requirement> list = requirementMapper.findInboxList(query);
        return Result.success(new PageInfo<>(list));
    }

    /** 提报需求各状态数量 */
    @GetMapping("/tab-counts")
    public Result<?> tabCounts(HttpServletRequest request) {
        String scope = dataScope(request);
        List<Map<String, Object>> rows = requirementMapper.countInboxByStatus(scope);
        Map<String, Long> result = new HashMap<>();
        long total = 0;
        for (Map<String, Object> row : rows) {
            String status = (String) row.get("name");
            long cnt = ((Number) row.get("value")).longValue();
            result.put(status, cnt);
            total += cnt;
        }
        result.put("all", total);
        return Result.success(result);
    }

    /** 操作日志 */
    @GetMapping("/{id}/logs")
    public Result<?> logs(@PathVariable Long id) {
        return Result.success(requirementLogMapper.findByRequirementId(id));
    }

    /** 新建提报需求 */
    @PostMapping
    public Result<?> create(@RequestBody Requirement requirement, HttpServletRequest request) {
        String operator = getOperator(request);
        if (requirement.getFunctionName() == null || requirement.getFunctionName().isBlank()) {
            return Result.error(400, "需求名称不能为空");
        }
        if (requirementMapper.countByFunctionName(requirement.getFunctionName(), null) > 0) {
            return Result.error(400, "需求「" + requirement.getFunctionName() + "」已存在");
        }
        requirement.setSubmissionStatus("已创建");
        requirement.setSubmittedBy(operator);
        requirement.setStatus("未开始");
        requirementMapper.insert(requirement);
        writeLog(requirement.getId(), operator, "创建", null, requirement);
        return Result.success(requirement.getId());
    }

    /** 编辑提报需求（仅已创建/已驳回状态可编辑） */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Requirement requirement, HttpServletRequest request) {
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        User editUser = (User) request.getAttribute("currentUser");
        boolean isAdminEdit = editUser != null && "ADMIN".equals(editUser.getRole());
        if (!isAdminEdit && !"已创建".equals(existing.getSubmissionStatus()) && !"已驳回".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "当前状态不允许编辑");
        }
        if (!canOperate(request, existing)) return Result.error(403, "无权限操作");
        requirement.setId(id);
        requirement.setSubmissionStatus(null);
        requirement.setSubmittedBy(null);
        requirementMapper.update(requirement);
        Requirement after = requirementMapper.findById(id);
        writeLog(id, getOperator(request), "编辑", existing, after);
        return Result.success();
    }

    /** 删除提报需求（仅已创建状态） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        User deleteUser = (User) request.getAttribute("currentUser");
        boolean isAdmin = deleteUser != null && "ADMIN".equals(deleteUser.getRole());
        if (!isAdmin && !"已创建".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "只有「已创建」状态的提报需求可以删除");
        }
        if (!canOperate(request, existing)) return Result.error(403, "无权限操作");
        writeLog(id, getOperator(request), "删除", existing, null);
        requirementMapper.deleteById(id);
        return Result.success();
    }

    /** 提交评估：已创建/已驳回 → 待评估 */
    @PostMapping("/{id}/submit")
    public Result<?> submit(@PathVariable Long id, HttpServletRequest request) {
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        if (!"已创建".equals(existing.getSubmissionStatus()) && !"已驳回".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "当前状态不允许提交");
        }
        if (!canOperate(request, existing)) return Result.error(403, "无权限操作");
        updateSubmissionStatus(id, "待评估", getOperator(request), existing);
        return Result.success();
    }

    /** 撤回：待评估 → 已创建 */
    @PostMapping("/{id}/withdraw")
    public Result<?> withdraw(@PathVariable Long id, HttpServletRequest request) {
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        if (!"待评估".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "只有「待评估」状态可以撤回");
        }
        if (!canOperate(request, existing)) return Result.error(403, "无权限操作");
        updateSubmissionStatus(id, "已创建", getOperator(request), existing);
        return Result.success();
    }

    /** 产品评估：待评估 → 进入需求池 or 已驳回（仅 MANAGER/ADMIN） */
    @PostMapping("/{id}/evaluate")
    public Result<?> evaluate(@PathVariable Long id,
                              @RequestBody Map<String, String> body,
                              HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) return Result.error(401, "未登录");
        String role = user.getRole();
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            return Result.error(403, "无权限操作，需要 MANAGER 或 ADMIN 角色");
        }
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        if (!"待评估".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "只有「待评估」状态可以评估");
        }
        boolean pass = "true".equals(body.get("pass"));
        String newStatus = pass ? "进入需求池" : "已驳回";
        if (!pass) {
            String reason = body.get("rejectReason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error(400, "请填写驳回意见");
            }
            requirementMapper.updateRejectReason(id, reason.trim());
        }
        updateSubmissionStatus(id, newStatus, user.getUsername(), existing);
        return Result.success();
    }

    /** 取消：已驳回 → 已取消 */
    @PostMapping("/{id}/archive")
    public Result<?> archive(@PathVariable Long id, HttpServletRequest request) {
        Requirement existing = requirementMapper.findById(id);
        if (existing == null) return Result.error(404, "需求不存在");
        if (!"已驳回".equals(existing.getSubmissionStatus())) {
            return Result.error(400, "只有「已驳回」状态可以取消");
        }
        if (!canOperate(request, existing)) return Result.error(403, "无权限操作");
        updateSubmissionStatus(id, "已取消", getOperator(request), existing);
        return Result.success();
    }

    private void updateSubmissionStatus(Long id, String status, String operator, Requirement before) {
        requirementMapper.updateSubmissionStatus(id, status);
        Requirement after = requirementMapper.findById(id);
        writeLog(id, operator, "状态变更", before, after);
    }

    private void writeLog(Long requirementId, String operator, String type,
                          Requirement before, Requirement after) {
        try {
            RequirementLog log = new RequirementLog();
            log.setRequirementId(requirementId);
            log.setOperator(operator);
            log.setOperationType(type);
            log.setBeforeContent(before == null ? null : objectMapper.writeValueAsString(before));
            log.setAfterContent(after == null ? null : objectMapper.writeValueAsString(after));
            requirementLogMapper.insert(log);
        } catch (Exception ignored) {}
    }

    /** 返回数据范围限制：ADMIN/MANAGER 返回 null（不限），普通用户返回自己的 username */
    private String dataScope(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) return "unknown";
        String role = user.getRole();
        if ("ADMIN".equals(role) || "MANAGER".equals(role)) return null;
        return user.getUsername();
    }

    private String getOperator(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getUsername() : "unknown";
    }

    /** 创建人、MANAGER 或 ADMIN 可操作 */
    private boolean canOperate(HttpServletRequest request, Requirement existing) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) return false;
        if ("ADMIN".equals(user.getRole()) || "MANAGER".equals(user.getRole())) return true;
        return user.getUsername().equalsIgnoreCase(existing.getSubmittedBy());
    }
}
