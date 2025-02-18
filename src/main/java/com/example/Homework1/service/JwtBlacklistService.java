package com.example.Homework1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // ✅ 把 Token 加入 Redis 黑名單
    public void addToBlacklist(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", expirationMillis, TimeUnit.SECONDS);
    }

    // ✅ 檢查 Token 是否在黑名單內
    public boolean isBlacklisted(String token) {
        String result = redisTemplate.opsForValue().get("blacklist:" + token);
        return "true".equals(result);
    }
    
    
    // ✅ 刪除 Redis 黑名單中的 Token
    public void removeFromBlacklist(String token) {
        redisTemplate.delete(BLACKLIST_PREFIX + token);
    }
}
