package com.example.Homework1.controller;

import com.example.Homework1.dto.AuthRequest;
import com.example.Homework1.dto.AuthResponse;
import com.example.Homework1.dto.ForgotPasswordRequest;
import com.example.Homework1.entity.User;
import com.example.Homework1.entity.Role;
import com.example.Homework1.service.UserService;
import com.example.Homework1.utils.JwtUtil;
import com.example.Homework1.service.JwtBlacklistService;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth-Controller",description = "註冊員工")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtBlacklistService blacklistService; // ✅ 確保這行存在

    //註冊 API（所有人都可用，預設角色為 EMPLOYEE）
    @Operation(summary = "註冊(初始角色為 EMPLOYEE)")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        // 檢查基本資料
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        // 建立用戶，預設為 EMPLOYEE
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // 密碼加密
                .role(Role.EMPLOYEE) // 只能註冊 EMPLOYEE
                .fullname(request.getFullname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        try {
            userService.saveUser(user,"EMPLOYEE");
            return ResponseEntity.ok("註冊成功！");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
        }
    }

    //登入 API（回傳 JWT Token）
    @Operation(summary = "登入") 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userService.findByUsername(request.getUsername());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(new AuthResponse(null, "帳號或密碼錯誤！"));
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, "登入成功！"));
    }
    
    @Operation(summary = "登出並讓 Token 失效")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("缺少有效的 Token");
        }

        String token = authHeader.substring(7);
        long expirationMillis = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();

        blacklistService.addToBlacklist(token, expirationMillis);
        return ResponseEntity.ok("登出成功，Token 已失效");
    }

    //JWT 重新簽發（Token 過期時使用）
    @Operation(summary = "JWT 重新簽發") 
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "請提供有效的 Authorization Token"));
        }

        try {
            String token = authHeader.substring(7); //去掉 "Bearer "
            Claims claims = jwtUtil.extractAllClaims(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(403).body(new AuthResponse(null, "Token 已過期，請重新登入"));
            }

            //生成新的 JWT Token
            String newToken = jwtUtil.generateToken(username, role);

            return ResponseEntity.ok(new AuthResponse(newToken, "Token 重新簽發成功"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new AuthResponse(null, "無效的 Token"));
        }
    }
    @Operation(summary = "忘記密碼，重設密碼")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestUsername = authentication.getName(); //取得當前登入的使用者

        //只能修改自己的密碼
        if (!requestUsername.equals(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您無法更改其他使用者的密碼");
        }

        try {
            userService.resetPassword(request.getUsername(), request.getNewPassword());
            return ResponseEntity.ok("密碼已成功重設");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到該用戶");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("發生錯誤：" + e.getMessage());
        }
    }
}
