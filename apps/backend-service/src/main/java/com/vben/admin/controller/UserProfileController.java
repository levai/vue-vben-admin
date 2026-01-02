package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.AuthService;
import com.vben.admin.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户个人中心控制器（管理端）
 *
 * @author vben
 */
@Api(tags = "用户个人中心")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

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

    @Operation(summary = "更新用户基础信息", description = "更新当前登录用户的基础信息（真实姓名、昵称、手机号、性别）")
    @PutMapping("/info")
    public BaseResult<Boolean> updateInfo(@Valid @RequestBody UpdateUserInfoDTO infoDTO) {
        com.vben.admin.model.dto.UserDTO userDTO = new com.vben.admin.model.dto.UserDTO();
        userDTO.setRealName(infoDTO.getRealName());
        userDTO.setNickname(infoDTO.getNickname());
        userDTO.setPhone(infoDTO.getPhone());
        userDTO.setGender(infoDTO.getGender());
        userService.updateCurrentUserInfo(userDTO);
        return new BaseResult<>(true);
    }

    /**
     * 修改密码DTO
     */
    @Data
    @Schema(description = "修改密码DTO")
    public static class ChangePasswordDTO {
        @Schema(description = "旧密码", example = "123456")
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @Schema(description = "新密码", example = "newpassword123")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度必须在6-50之间")
        private String newPassword;
    }

    /**
     * 更新用户基础信息DTO
     */
    @Data
    @Schema(description = "更新用户基础信息DTO")
    public static class UpdateUserInfoDTO {
        @Schema(description = "真实姓名", example = "张三")
        @Size(max = 50, message = "真实姓名长度不能超过50")
        private String realName;

        @Schema(description = "昵称", example = "小张")
        @Size(max = 50, message = "昵称长度不能超过50")
        private String nickname;

        @Schema(description = "手机号", example = "13800138000")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
        private Integer gender;
    }
}
