package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.model.dto.LoginDTO;
import com.vben.admin.model.vo.LoginResultVO;
import com.vben.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 *
 * @author vben
 */
@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "用户登录接口")
    @PostMapping("/login")
    public BaseResult<LoginResultVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResultVO result = authService.login(loginDTO);
        return new BaseResult<>(result);
    }

    @Operation(summary = "退出登录", description = "用户退出登录接口")
    @PostMapping("/logout")
    public BaseResult<Boolean> logout() {
        authService.logout();
        return new BaseResult<>(true);
    }

    @Operation(summary = "刷新Token", description = "刷新AccessToken接口")
    @PostMapping("/refresh")
    public BaseResult<String> refresh(HttpServletRequest request) {
        // 从Cookie中获取RefreshToken（这里简化处理，实际应该从Cookie获取）
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken == null) {
            refreshToken = request.getParameter("refreshToken");
        }
        String newAccessToken = authService.refreshToken(refreshToken);
        return new BaseResult<>(newAccessToken);
    }

    @Operation(summary = "获取权限码列表", description = "获取当前用户的权限码列表")
    @GetMapping("/codes")
    public BaseResult<List<String>> getCodes() {
        List<String> codes = authService.getAccessCodes();
        return new BaseResult<>(codes);
    }
}
