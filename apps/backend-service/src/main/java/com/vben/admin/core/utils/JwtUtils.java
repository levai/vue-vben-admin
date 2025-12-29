package com.vben.admin.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author vben
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:vben-admin-secret-key-for-jwt-token-generation-minimum-256-bits}")
    private String secret;

    @Value("${jwt.access-token-expiration:7200000}")
    private Long accessTokenExpiration; // 2小时，单位：毫秒

    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration; // 7天，单位：毫秒

    /**
     * 生成 AccessToken
     *
     * @param claims 载荷数据
     * @return token
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * 生成 RefreshToken
     *
     * @param claims 载荷数据
     * @return token
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * 生成 Token
     *
     * @param claims     载荷数据
     * @param expiration 过期时间（毫秒）
     * @return token
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Claims jwtClaims = new DefaultClaims(claims);
        jwtClaims.setIssuedAt(now);
        jwtClaims.setExpiration(expiryDate);

        return Jwts.builder()
                .setClaims(jwtClaims)
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
    }

    /**
     * 从 token 中获取 Claims
     *
     * @param token token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 token 中获取用户名
     *
     * @param token token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从 token 中获取用户ID
     *
     * @param token token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("userId");
    }

    /**
     * 验证 token 是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 token 是否过期
     *
     * @param token token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
