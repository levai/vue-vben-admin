package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.validation.ValidId;
import com.vben.admin.model.dto.RoleDTO;
import com.vben.admin.model.dto.RoleOptionQueryDTO;
import com.vben.admin.model.vo.RoleVO;
import com.vben.admin.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理控制器（管理端）
 *
 * @author vben
 */
@Tag(name = "系统角色管理")
@RestController
@RequestMapping("/system/role")
@Validated
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表", description = "获取角色列表（支持分页）")
    @GetMapping
    public BaseResult<PageResult<RoleVO>> getList(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词（模糊查询角色名称和ID，优先级高于 name/id）")
            @RequestParam(required = false) String search,
            @Parameter(description = "角色名称（模糊查询，与 search 互斥）")
            @RequestParam(required = false) String name,
            @Parameter(description = "角色ID（模糊查询，与 search 互斥）")
            @RequestParam(required = false) String id,
            @Parameter(description = "备注（模糊查询）")
            @RequestParam(required = false) String remark,
            @Parameter(description = "状态（0-禁用，1-启用）")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间（格式：yyyy-MM-dd）")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间（格式：yyyy-MM-dd）")
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
    public BaseResult<Boolean> update(@ValidId(message = "角色ID不能为空或无效值") @PathVariable String id, @Validated(RoleDTO.Update.class) @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(id, roleDTO);
        return new BaseResult<>(true);
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@ValidId(message = "角色ID不能为空或无效值") @PathVariable String id) {
        roleService.deleteRole(id);
        return new BaseResult<>(true);
    }

    @Operation(summary = "获取角色选项列表", description = "获取角色选项列表（用于下拉选项，支持分页或 limit 限制，支持条件查询，返回完整角色信息，前端自行处理 label 和 value）。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制（默认 1000）")
    @GetMapping("/options")
    public BaseResult<PageResult<RoleVO>> getOptions(RoleOptionQueryDTO queryDTO) {
        PageResult<RoleVO> result = roleService.getRoleOptions(queryDTO);
        return new BaseResult<>(result);
    }
}
