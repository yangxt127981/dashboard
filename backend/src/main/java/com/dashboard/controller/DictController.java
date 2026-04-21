package com.dashboard.controller;

import com.dashboard.common.Result;
import com.dashboard.entity.SysDepartment;
import com.dashboard.entity.SysModule;
import com.dashboard.entity.SysProductOwner;
import com.dashboard.entity.SysRequestOwner;
import com.dashboard.entity.User;
import com.dashboard.mapper.SysDepartmentMapper;
import com.dashboard.mapper.SysModuleMapper;
import com.dashboard.mapper.SysProductOwnerMapper;
import com.dashboard.mapper.SysRequestOwnerMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final SysDepartmentMapper deptMapper;
    private final SysModuleMapper moduleMapper;
    private final SysRequestOwnerMapper requestOwnerMapper;
    private final SysProductOwnerMapper productOwnerMapper;

    public DictController(SysDepartmentMapper deptMapper, SysModuleMapper moduleMapper, SysRequestOwnerMapper requestOwnerMapper, SysProductOwnerMapper productOwnerMapper) {
        this.deptMapper = deptMapper;
        this.moduleMapper = moduleMapper;
        this.requestOwnerMapper = requestOwnerMapper;
        this.productOwnerMapper = productOwnerMapper;
    }

    // ===== 部门 =====

    @GetMapping("/departments")
    public Result<?> listDepts() {
        return Result.success(deptMapper.findAll());
    }

    @PostMapping("/departments")
    public Result<?> createDept(@RequestBody SysDepartment dept, HttpServletRequest request) {
        if (!hasPermission(request, "dept:create")) return Result.error(403, "无权限操作");
        if (dept.getName() == null || dept.getName().isBlank()) return Result.error(400, "部门名称不能为空");
        deptMapper.insert(dept);
        return Result.success(dept.getId());
    }

    @PutMapping("/departments/{id}")
    public Result<?> updateDept(@PathVariable Long id, @RequestBody SysDepartment dept, HttpServletRequest request) {
        if (!hasPermission(request, "dept:edit")) return Result.error(403, "无权限操作");
        if (dept.getName() == null || dept.getName().isBlank()) return Result.error(400, "部门名称不能为空");
        dept.setId(id);
        deptMapper.update(dept);
        return Result.success();
    }

    @DeleteMapping("/departments/{id}")
    public Result<?> deleteDept(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "dept:delete")) return Result.error(403, "无权限操作");
        deptMapper.deleteById(id);
        return Result.success();
    }

    // ===== 模块 =====

    @GetMapping("/modules")
    public Result<?> listModules() {
        return Result.success(moduleMapper.findAll());
    }

    @PostMapping("/modules")
    public Result<?> createModule(@RequestBody SysModule module, HttpServletRequest request) {
        if (!hasPermission(request, "module:create")) return Result.error(403, "无权限操作");
        if (module.getName() == null || module.getName().isBlank()) return Result.error(400, "模块名称不能为空");
        moduleMapper.insert(module);
        return Result.success(module.getId());
    }

    @PutMapping("/modules/{id}")
    public Result<?> updateModule(@PathVariable Long id, @RequestBody SysModule module, HttpServletRequest request) {
        if (!hasPermission(request, "module:edit")) return Result.error(403, "无权限操作");
        if (module.getName() == null || module.getName().isBlank()) return Result.error(400, "模块名称不能为空");
        module.setId(id);
        moduleMapper.update(module);
        return Result.success();
    }

    @DeleteMapping("/modules/{id}")
    public Result<?> deleteModule(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "module:delete")) return Result.error(403, "无权限操作");
        moduleMapper.deleteById(id);
        return Result.success();
    }

    // ===== 需求对接人 =====

    @GetMapping("/request-owners")
    public Result<?> listRequestOwners() {
        return Result.success(requestOwnerMapper.findAll());
    }

    @PostMapping("/request-owners")
    public Result<?> createRequestOwner(@RequestBody SysRequestOwner owner, HttpServletRequest request) {
        if (!hasPermission(request, "system:requestowner")) return Result.error(403, "无权限操作");
        if (owner.getName() == null || owner.getName().isBlank()) return Result.error(400, "名称不能为空");
        try {
            requestOwnerMapper.insert(owner);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error(400, "对接人「" + owner.getName() + "」已存在");
            }
            throw e;
        }
        return Result.success(owner.getId());
    }

    @PutMapping("/request-owners/{id}")
    public Result<?> updateRequestOwner(@PathVariable Long id, @RequestBody SysRequestOwner owner, HttpServletRequest request) {
        if (!hasPermission(request, "system:requestowner")) return Result.error(403, "无权限操作");
        if (owner.getName() == null || owner.getName().isBlank()) return Result.error(400, "名称不能为空");
        owner.setId(id);
        try {
            requestOwnerMapper.update(owner);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error(400, "对接人「" + owner.getName() + "」已存在");
            }
            throw e;
        }
        return Result.success();
    }

    @DeleteMapping("/request-owners/{id}")
    public Result<?> deleteRequestOwner(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "system:requestowner")) return Result.error(403, "无权限操作");
        requestOwnerMapper.deleteById(id);
        return Result.success();
    }

    // ===== 产品对接人 =====

    @GetMapping("/product-owners")
    public Result<?> listProductOwners() {
        return Result.success(productOwnerMapper.findAll());
    }

    @PostMapping("/product-owners")
    public Result<?> createProductOwner(@RequestBody SysProductOwner owner, HttpServletRequest request) {
        if (!hasPermission(request, "system:productowner")) return Result.error(403, "无权限操作");
        if (owner.getName() == null || owner.getName().isBlank()) return Result.error(400, "名称不能为空");
        try {
            productOwnerMapper.insert(owner);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error(400, "产品对接人「" + owner.getName() + "」已存在");
            }
            throw e;
        }
        return Result.success(owner.getId());
    }

    @PutMapping("/product-owners/{id}")
    public Result<?> updateProductOwner(@PathVariable Long id, @RequestBody SysProductOwner owner, HttpServletRequest request) {
        if (!hasPermission(request, "system:productowner")) return Result.error(403, "无权限操作");
        if (owner.getName() == null || owner.getName().isBlank()) return Result.error(400, "名称不能为空");
        owner.setId(id);
        try {
            productOwnerMapper.update(owner);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error(400, "产品对接人「" + owner.getName() + "」已存在");
            }
            throw e;
        }
        return Result.success();
    }

    @DeleteMapping("/product-owners/{id}")
    public Result<?> deleteProductOwner(@PathVariable Long id, HttpServletRequest request) {
        if (!hasPermission(request, "system:productowner")) return Result.error(403, "无权限操作");
        productOwnerMapper.deleteById(id);
        return Result.success();
    }

    private boolean hasPermission(HttpServletRequest request, String code) {
        User user = (User) request.getAttribute("currentUser");
        return user != null && user.getPermissions() != null && user.getPermissions().contains(code);
    }
}
