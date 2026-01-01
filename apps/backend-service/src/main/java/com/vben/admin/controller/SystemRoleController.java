package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.dto.RoleOptionQueryDTO;
import com.vben.admin.model.vo.RoleVO;
import com.vben.admin.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理控制器
 *
 * @author vben
 */
@Tag(name = "系统角色管理", description = "系统角色管理接口")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SystemRoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表", description = "获取角色列表（支持分页）")
    @GetMapping
    public BaseResult<PageResult<RoleVO>> getList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<RoleVO> result = roleService.getRoleList(page, pageSize, search, name, id, remark, status, startTime, endTime);
        return new BaseResult<>(result);
    }

    @Operation(summary = "创建角色", description = "创建新角色")
    @PostMapping
    public BaseResult<String> create(@Validated(RoleDTO.Create.class) @RequestBody RoleDTO roleDTO) {
        String id = roleService.createRole(roleDTO);
        return new BaseResult<>(id);
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/{id}")
    public BaseResult<Boolean> update(@PathVariable String id, @Validated(RoleDTO.Update.class) @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(id, roleDTO);
        return new BaseResult<>(true);
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@PathVariable String id) {
        roleService.deleteRole(id);
        return new BaseResult<>(true);
    }

    @Operation(summary = "获取角色选项列表", description = "获取角色选项列表（用于下拉选项，支持 limit 限制，支持条件查询，返回完整角色信息，前端自行处理 label 和 value）")
    @GetMapping("/options")
    public BaseResult<PageResult<RoleVO>> getOptions(RoleOptionQueryDTO queryDTO) {
        PageResult<RoleVO> result = roleService.getRoleOptions(queryDTO);
        return new BaseResult<>(result);
    }
}
