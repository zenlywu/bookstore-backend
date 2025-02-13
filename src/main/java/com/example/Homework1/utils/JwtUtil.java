package com.example.Homework1.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "RpvC+1lsyEgBUgtBoJ0+xExgVHqoBckfDazcMy+4Kxc="; // 🔑 Base64 金鑰
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 10 小時

    private final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

    // 生成 Token
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // 解析所有 Claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 解析 Token，獲取 `username`
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 解析 Token，獲取 `role`
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // 驗證 Token 是否過期
    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        Date now = new Date();
    
        System.out.println("✅ Token 過期時間：" + expiration);
        System.out.println("✅ 當前伺服器時間：" + now);
    
        return expiration.before(now);
    }
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    
}
