package com.covex.web.config;

import com.covex.common.util.JwtUtil;
import com.covex.common.util.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器 — 从 Authorization Header 解析 Token 并设置 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(AUTH_HEADER);
            if (StringUtils.isNotBlank(header) && header.startsWith(BEARER_PREFIX)) {
                String token = header.substring(BEARER_PREFIX.length());
                try {
                    if (jwtUtil.validateToken(token)) {
                        Claims claims = jwtUtil.parseToken(token);
                        Long userId = claims.get("userId", Long.class);
                        String username = claims.getSubject();
                        @SuppressWarnings("unchecked")
                        List<String> roles = claims.get("roles", List.class);

                        // 提取 tenantId 并设置到 TenantContext
                        Long tenantId = claims.get("tenantId", Long.class);
                        TenantContext.setCurrentTenantId(tenantId != null ? tenantId : 0L);

                        List<SimpleGrantedAuthority> authorities = roles != null
                                ? roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList())
                                : List.of();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        // 将 username 存入 details
                        authentication.setDetails(username);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    log.warn("JWT authentication failed: {}", e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
