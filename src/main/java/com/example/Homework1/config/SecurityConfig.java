package com.example.Homework1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Configuration
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SecurityConfig {

    private final JwtFilter jwtFilter; // JWT 過濾器
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 關閉 CSRF（因為我們用 JWT）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 允許未登入用戶訪問 `register`、`login`、`refresh-token`
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh-token","/api/auth/forgot-password","/api/auth/logout").permitAll()
                // `ADMIN` 可以管理 `HR_MANAGER` 和 `EMPLOYEE`
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAnyAuthority("ADMIN", "HR_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ADMIN", "HR_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("ADMIN", "HR_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ADMIN", "HR_MANAGER")

                // `books` API：`ADMIN` 和 `HR_MANAGER` 可以 CRUD，`EMPLOYEE` 只能讀取
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyAuthority("ADMIN", "HR_MANAGER","EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyAuthority("ADMIN", "HR_MANAGER","EMPLOYEE")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyAuthority("ADMIN", "HR_MANAGER","EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyAuthority("ADMIN", "HR_MANAGER", "EMPLOYEE")

                .requestMatchers("/**").permitAll() //允許所有請求，包含 `OPTIONS`

                // 其他 API 需要身份驗證
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); //在驗證之前加入 JWT 過濾器
            System.out.println("✅ [DEBUG] SecurityConfig 已載入，權限配置完成");

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}

