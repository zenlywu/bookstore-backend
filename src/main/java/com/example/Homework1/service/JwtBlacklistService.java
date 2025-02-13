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
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", expirationMillis, TimeUnit.MILLISECONDS);
    }

    // ✅ 檢查 Token 是否在黑名單內
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
