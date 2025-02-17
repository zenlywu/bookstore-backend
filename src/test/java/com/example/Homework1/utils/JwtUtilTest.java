package com.example.Homework1.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken("testUser", "ADMIN");
        assertNotNull(token);

        String username = jwtUtil.extractUsername(token);
        assertEquals("testUser", username);

        String role = jwtUtil.extractRole(token);
        assertEquals("ADMIN", role);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
    }

    @Test
    void testGenerateExpiredToken() {
        // ✅ 產生已過期的 Token（設定過期時間為當前時間 - 10 秒）
        String expiredToken = Jwts.builder()
                .subject("testUser")
                .claim("role", "EMPLOYEE")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))  // 10 秒前發出
                .expiration(new Date(System.currentTimeMillis() - 50000)) // 5 秒前過期
                .signWith(jwtUtil.getKey())  // 確保 JwtUtil 有 `getKey()` 方法
                .compact();

        assertNotNull(expiredToken);  // ✅ 確保 Token 生成成功

        // ✅ 確保 `isTokenExpired()` 可以識別過期 Token
        assertTrue(jwtUtil.isTokenExpired(expiredToken));
    }

}
