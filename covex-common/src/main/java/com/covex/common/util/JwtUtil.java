package com.covex.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类 - 生成 / 解析 / 验证 Token
 */
@Component
public class JwtUtil {

    @Value("${covex.jwt.secret}")
    private String secret;

    @Value("${covex.jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username, List<String> roles, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("tenantId", tenantId != null ? tenantId : 0L)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token，返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 获取 userId
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 获取 username
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 获取 roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", List.class);
    }

    /**
     * 从 Token 获取 tenantId
     */
    public Long getTenantId(String token) {
        Claims claims = parseToken(token);
        return claims.get("tenantId", Long.class);
    }
}
