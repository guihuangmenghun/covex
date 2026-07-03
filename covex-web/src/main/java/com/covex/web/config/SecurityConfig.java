package com.covex.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.covex.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置 — JWT 认证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 登录接口不需要认证
                .requestMatchers("/api/user/login").permitAll()
                // Swagger / OpenAPI 文档放行
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                        "/v3/api-docs/**", "/webjars/**", "/favicon.ico").permitAll()
                // 健康检查放行（仅限 health 和 info，不暴露 prometheus/env/beans 等）
                .requestMatchers("/api/health", "/actuator/health", "/actuator/info").permitAll()
                // Druid 监控面板需要 ADMIN 权限
                .requestMatchers("/druid/**").hasRole("ADMIN")
                // 其他所有 /api/** 接口需要 JWT 认证
                .requestMatchers("/api/**").authenticated()
                // 其余请求默认拒绝（fail-closed），防止新增路径意外暴露
                .anyRequest().denyAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Result<Void> result = Result.fail(401, "未认证，请先登录");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    Result<Void> result = Result.fail(403, "无权限访问");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
