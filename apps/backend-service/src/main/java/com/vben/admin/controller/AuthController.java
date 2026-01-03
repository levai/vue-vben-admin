package com.vben.admin.controller;

import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.utils.CookieUtils;
import com.vben.admin.model.dto.LoginDTO;
import com.vben.admin.model.vo.LoginResultVO;
import com.vben.admin.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器（管理端）
 *
 * @author vben
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "用户登录接口")
    @PostMapping("/login")
    public BaseResult<LoginResultVO> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletResponse response) {
        LoginResultVO result = authService.login(loginDTO);

        // 将 refreshToken 设置到 Cookie 中
        if (result.getRefreshToken() != null) {
            CookieUtils.setRefreshTokenCookie(response, result.getRefreshToken());
            // 清除返回结果中的 refreshToken，不返回给前端
            result.setRefreshToken(null);
        }

        return new BaseResult<>(result);
    }

    @Operation(summary = "退出登录", description = "用户退出登录接口")
    @PostMapping("/logout")
    public BaseResult<Boolean> logout(HttpServletResponse response) {
        authService.logout();
        // 清除 refreshToken Cookie
        CookieUtils.clearRefreshTokenCookie(response);
        return new BaseResult<>(true);
    }

    @Operation(summary = "刷新Token", description = "刷新AccessToken接口")
    @PostMapping(value = "/refresh", produces = "text/plain;charset=UTF-8")
    public String refresh(HttpServletRequest request) {
        // 从 Cookie 中获取 RefreshToken
        String refreshToken = CookieUtils.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new com.vben.admin.core.exception.BusinessException("RefreshToken 不存在，请重新登录");
        }

        // 刷新 token（生成新的 accessToken，继续使用原 refreshToken）
        // 直接返回 token 字符串，与 Mock 服务保持一致，前端无需解析 BaseResult
        return authService.refreshToken(refreshToken);
    }

    @Operation(summary = "获取权限码列表", description = "获取当前用户的权限码列表")
    @GetMapping("/codes")
    public BaseResult<List<String>> getCodes() {
        List<String> codes = authService.getAccessCodes();
        return new BaseResult<>(codes);
    }
}
