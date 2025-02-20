package com.example.Homework1.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken("testUser", "EMPLOYEE");

        assertNotNull(token, "Token 不能為 null");
        assertEquals("testUser", jwtUtil.extractUsername(token));
        assertEquals("EMPLOYEE", jwtUtil.extractRole(token));
        assertFalse(jwtUtil.isTokenExpired(token), "Token 應該是有效的");

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration, "Token 過期時間不能為 null");
    }

    @Test
    void testGenerateExpiredToken() {
        String expiredToken = jwtUtil.generateExpiredToken();

        assertNotNull(expiredToken);
        assertTrue(jwtUtil.isTokenExpired(expiredToken), "Token 應該是過期的");
    }

    @Test
    void testExtractAllClaimsWithInvalidToken() {
        String malformedToken = "invalid.token.value"; // 格式錯誤的 Token
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtUtil.extractAllClaims(malformedToken);
        }, "應該拋出 MalformedJwtException，因為 Token 格式錯誤");

        String fakeSignedToken = Jwts.builder()
                .subject("testUser")
                .claim("role", "EMPLOYEE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 小時後過期
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))) // ❌ 錯誤的密鑰
                .compact();

        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            jwtUtil.extractAllClaims(fakeSignedToken);
        }, "應該拋出 SignatureException，因為 Token 簽名驗證失敗");
    }

    @Test
    void testTokenExpirationTime() {
        long expectedExpiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        String token = jwtUtil.generateToken("testUser", "EMPLOYEE");

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration, "Token 過期時間不能為 null");
        assertTrue(expiration.getTime() <= expectedExpiration, "Token 過期時間應符合設定");
    }
}
