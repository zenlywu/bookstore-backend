package com.example.Homework1.config;

import com.example.Homework1.service.JwtBlacklistService;
import com.example.Homework1.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtBlacklistService blacklistService; // 新增黑名單檢查

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // ✅ 檢查 Token 是否在黑名單內
            if (blacklistService.isBlacklisted(token)) {
                System.out.println("❌ JwtFilter - Token 在黑名單內，拒絕存取！");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token 已失效，請重新登入");
                return;
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            System.out.println("❌ JwtFilter - 無效的 Token：" + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "無效的 Token");
            return;
        }

        chain.doFilter(request, response);
    }
}
