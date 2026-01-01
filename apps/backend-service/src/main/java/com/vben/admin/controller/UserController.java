package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.AuthService;
import com.vben.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户控制器
 *
 * @author vben
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    @GetMapping("/info")
    public BaseResult<UserVO> getInfo() {
        UserVO userInfo = authService.getCurrentUserInfo();
        return new BaseResult<>(userInfo);
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/password")
    public BaseResult<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO passwordDTO) {
        userService.changePassword(passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return new BaseResult<>(true);
    }

    /**
     * 修改密码DTO
     */
    @Data
    public static class ChangePasswordDTO {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度必须在6-50之间")
        private String newPassword;
    }
}
