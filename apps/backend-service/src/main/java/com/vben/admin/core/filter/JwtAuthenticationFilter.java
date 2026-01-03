package com.vben.admin.core.filter;

import com.vben.admin.core.exception.CustomAccessDeniedHandler;
import com.vben.admin.core.exception.CustomAuthenticationEntryPoint;
import com.vben.admin.core.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * JWT 认证过滤器
 *
 * @author vben
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final Set<RequestMatcher> ignoreUrls;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   AccessDeniedHandler accessDeniedHandler,
                                   Set<RequestMatcher> ignoreUrls) {
        this.jwtUtils = jwtUtils;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.ignoreUrls = ignoreUrls;
    }

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 检查是否在白名单中
        boolean isIgnore = ignoreUrls.stream()
                .anyMatch(matcher -> matcher.matches(request));
        if (isIgnore) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        try {
            if (StringUtils.isNotEmpty(token)) {
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    String userId = jwtUtils.getUserIdFromToken(token);

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将用户信息存储到 request 属性中，方便后续使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                } else {
                    throw new AuthenticationServiceException("Token已失效, 请重新登录");
                }
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationServiceException e) {
            authenticationEntryPoint.commence(request, response, e);
        } catch (AccessDeniedException e) {
            accessDeniedHandler.handle(request, response, e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 从请求中获取 token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
