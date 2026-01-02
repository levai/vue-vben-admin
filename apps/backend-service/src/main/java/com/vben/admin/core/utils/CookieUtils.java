package com.vben.admin.core.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie 工具类
 * 用于处理 refreshToken 的 Cookie 操作
 *
 * @author vben
 */
public class CookieUtils {

    /**
     * RefreshToken Cookie 名称
     * 使用语义化的名称，便于理解和调试
     */
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    /**
     * Cookie 最大存活时间（秒）
     * 与 refreshToken 过期时间保持一致：7天 = 604800 秒
     */
    private static final int MAX_AGE = 7 * 24 * 60 * 60; // 7天

    /**
     * 设置 RefreshToken Cookie
     *
     * @param response     HTTP 响应
     * @param refreshToken refreshToken 值
     */
    public static void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true); // 防止 XSS 攻击
        // secure 属性：生产环境建议开启（HTTPS），开发环境可以关闭（HTTP）
        // 这里默认关闭，如果需要可以在配置文件中配置
        cookie.setSecure(false); // 开发环境使用 false，生产环境建议改为 true
        cookie.setPath("/"); // Cookie 路径
        cookie.setMaxAge(MAX_AGE); // 30天
        // SameSite 属性：防止 CSRF 攻击
        // 注意：Servlet API 4.0+ 才支持直接设置 SameSite，这里通过响应头设置
        response.addCookie(cookie);
        // 设置 SameSite=None; Secure（如果需要跨域，需要 Secure=true）
        // 如果不需要跨域，可以设置为 SameSite=Strict 或 Lax
        // 这里暂时不设置，由浏览器默认处理
    }

    /**
     * 从请求中获取 RefreshToken Cookie
     *
     * @param request HTTP 请求
     * @return refreshToken 值，如果不存在则返回 null
     */
    public static String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 清除 RefreshToken Cookie
     *
     * @param response HTTP 响应
     */
    public static void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 与 setRefreshTokenCookie 保持一致
        cookie.setPath("/");
        cookie.setMaxAge(0); // 立即过期
        response.addCookie(cookie);
    }
}
