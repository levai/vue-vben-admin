package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.vo.UserVO;
import com.vben.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    @GetMapping("/info")
    public BaseResult<UserVO> getInfo() {
        UserVO userInfo = authService.getCurrentUserInfo();
        return new BaseResult<>(userInfo);
    }
}
