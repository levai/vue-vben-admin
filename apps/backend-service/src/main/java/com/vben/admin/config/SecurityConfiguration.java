package com.vben.admin.config;

import com.vben.admin.core.exception.CustomAccessDeniedHandler;
import com.vben.admin.core.exception.CustomAuthenticationEntryPoint;
import com.vben.admin.core.filter.JwtAuthenticationFilter;
import com.vben.admin.core.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security 配置
 *
 * @author vben
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           AuthenticationEntryPoint authenticationEntryPoint,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {
        // 需要排除校验的接口
        Set<RequestMatcher> ignoreUrls = this.getIgnoreUrls();
        httpSecurity
                .sessionManagement(sessionManage -> {
                    // 禁用 Session
                    sessionManage.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(requestMatcherRegistry -> {
                    requestMatcherRegistry.requestMatchers(ignoreUrls.toArray(new RequestMatcher[0])).permitAll();
                    requestMatcherRegistry.anyRequest().authenticated();
                })
                .exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint);
                    exceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler);
                })
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        // 添加 JWT 过滤器
        httpSecurity.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtils, authenticationEntryPoint, accessDeniedHandler, ignoreUrls),
                UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * 获取所有忽略登录的 url
     *
     * @return 忽略的 url
     */
    private Set<RequestMatcher> getIgnoreUrls() {
        Set<RequestMatcher> ignoreUrls = new HashSet<>(16);
        // 白名单：登录、退出登录、刷新token、API文档、测试接口
        ignoreUrls.add(new AntPathRequestMatcher("/auth/login"));
        ignoreUrls.add(new AntPathRequestMatcher("/auth/logout")); // 退出登录允许无token访问
        ignoreUrls.add(new AntPathRequestMatcher("/auth/refresh"));
        ignoreUrls.add(new AntPathRequestMatcher("/test/**")); // 测试接口（仅开发环境）
        ignoreUrls.add(new AntPathRequestMatcher("/doc.html"));
        ignoreUrls.add(new AntPathRequestMatcher("/v3/api-docs/**"));
        ignoreUrls.add(new AntPathRequestMatcher("/swagger-ui/**"));
        ignoreUrls.add(new AntPathRequestMatcher("/swagger-ui.html"));
        ignoreUrls.add(new AntPathRequestMatcher("/webjars/**"));
        return ignoreUrls;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * CORS 配置源
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有域名跨域（开发环境）
        configuration.addAllowedOriginPattern("*");
        // 允许所有请求头
        configuration.addAllowedHeader("*");
        // 允许所有请求方法
        configuration.addAllowedMethod("*");
        // 允许携带凭证（Cookie、Authorization 等）
        configuration.setAllowCredentials(true);
        // 预检请求的有效期，单位为秒（1小时）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
