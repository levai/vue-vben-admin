package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.dto.UserDTO;
import com.vben.admin.model.dto.UserOptionQueryDTO;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户管理控制器（管理端）
 *
 * @author vben
 */
@Tag(name = "系统用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户列表", description = "获取用户列表（支持分页和搜索）")
    @GetMapping
    public BaseResult<PageResult<UserVO>> getList(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词（模糊查询用户名和真实姓名，优先级高于 username/realName）")
            @RequestParam(required = false) String search,
            @Parameter(description = "用户名（模糊查询，与 search 互斥）")
            @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名（模糊查询，与 search 互斥）")
            @RequestParam(required = false) String realName,
            @Parameter(description = "部门ID")
            @RequestParam(required = false) String deptId,
            @Parameter(description = "状态（0-禁用，1-启用）")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间（格式：yyyy-MM-dd）")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间（格式：yyyy-MM-dd）")
            @RequestParam(required = false) String endTime) {
        PageResult<UserVO> result = userService.getUserList(page, pageSize, search, username, realName, deptId, status, startTime, endTime);
        return new BaseResult<>(result);
    }

    @Operation(summary = "获取用户信息", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    public BaseResult<UserVO> getById(@PathVariable String id) {
        UserVO user = userService.getUserById(id);
        return new BaseResult<>(user);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public BaseResult<String> create(@Validated(UserDTO.Create.class) @RequestBody UserDTO userDTO) {
        String id = userService.createUser(userDTO);
        return new BaseResult<>(id);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public BaseResult<Boolean> update(@PathVariable String id, @Validated(UserDTO.Update.class) @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return new BaseResult<>(true);
    }

    @Operation(summary = "删除用户", description = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public BaseResult<Boolean> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return new BaseResult<>(true);
    }

    @Operation(summary = "启用/禁用用户", description = "更新用户状态")
    @PutMapping("/{id}/status")
    public BaseResult<Boolean> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusDTO statusDTO) {
        userService.updateUserStatus(id, statusDTO.getStatus());
        return new BaseResult<>(true);
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PutMapping("/{id}/password")
    public BaseResult<Boolean> resetPassword(
            @PathVariable String id,
            @Valid @RequestBody ResetPasswordDTO passwordDTO) {
        userService.resetPassword(id, passwordDTO.getPassword());
        return new BaseResult<>(true);
    }

    @Operation(summary = "获取用户选项列表", description = "获取用户选项列表（用于下拉选项，支持分页或 limit 限制，支持条件查询，返回完整用户信息，前端自行处理 label 和 value）。如果传了 page 和 pageSize，则使用分页查询；否则使用 limit 限制（默认 1000）")
    @GetMapping("/options")
    public BaseResult<PageResult<UserVO>> getOptions(UserOptionQueryDTO queryDTO) {
        PageResult<UserVO> result = userService.getUserOptions(queryDTO);
        return new BaseResult<>(result);
    }

    /**
     * 更新状态DTO
     */
    @Data
    @Schema(description = "更新状态DTO")
    public static class UpdateStatusDTO {
        @Schema(description = "状态：0-禁用，1-启用", example = "1")
        @NotNull(message = "状态不能为空")
        private Integer status;
    }

    /**
     * 重置密码DTO
     */
    @Data
    @Schema(description = "重置密码DTO")
    public static class ResetPasswordDTO {
        @Schema(description = "新密码", example = "newpassword123")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50之间")
        private String password;
    }
}
