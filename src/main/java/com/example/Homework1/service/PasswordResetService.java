package com.example.Homework1.service;

import com.example.Homework1.entity.Role;
import com.example.Homework1.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service; 
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private static final long EXPIRATION_TIME = 15 * 60; // 15 分鐘

    // 🔹 產生重置 Token 並存入 Redis
    public String generateResetToken(String username) {
        String token = UUID.randomUUID().toString();  // 產生唯一 Token
        redisTemplate.opsForValue().set("reset:" + token, username, EXPIRATION_TIME, TimeUnit.SECONDS);
        return token;
    }

    // 🔹 驗證 Token 是否有效
    public String validateResetToken(String token) {
        return redisTemplate.opsForValue().get("reset:" + token);
    }

    // 🔹 重設密碼
    public boolean resetPassword(String token, String newPassword) {
        String username = validateResetToken(token);
        if (username == null) return false; // Token 無效
    
        User user = userService.findByUsername(username);
        if (user == null) return false;
    
        // ✅ 直接從 `User` 取得 `role`
        String originalRole = user.getRole().name();
        System.out.println("✅ [DEBUG] 重設密碼 - 角色: " + originalRole);
        user.setPassword(newPassword);
        user.setRole(Role.valueOf(originalRole)); // **確保角色不變**
        userService.saveresUser(user);
    
        redisTemplate.delete("reset:" + token); // ✅ 刪除 Token，防止重複使用
        return true;
    }
    
    
}
