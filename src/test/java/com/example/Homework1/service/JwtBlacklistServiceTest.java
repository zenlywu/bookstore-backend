package com.example.Homework1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

class JwtBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtBlacklistService jwtBlacklistService;

    private static final long EXPIRATION_TIME = 60 * 60; // 1 小時（秒）
    private static final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testAddToBlacklist() {
        jwtBlacklistService.addToBlacklist(TOKEN, EXPIRATION_TIME);

        // ✅ **確保 `set()` 方法有被正確呼叫**
        verify(valueOperations, times(1))
                .set(eq("blacklist:" + TOKEN), eq("true"), eq(EXPIRATION_TIME), eq(TimeUnit.SECONDS));
    }

    @Test
    void testIsBlacklisted_ReturnsTrueIfExists() {
        // ✅ **確保 `valueOperations.get()` 正確返回**
        when(valueOperations.get(eq("blacklist:" + TOKEN))).thenReturn("true");

        boolean isBlacklisted = jwtBlacklistService.isBlacklisted(TOKEN);

        assertTrue(isBlacklisted, "Token 應該已經在黑名單中");

        // ✅ **確保 `get()` 方法有被呼叫**
        verify(valueOperations, times(1)).get(eq("blacklist:" + TOKEN));
    }

    @Test
    void testIsBlacklisted_ReturnsFalseIfNotExists() {
        // ✅ **確保 `valueOperations.get()` 正確返回 null**
        when(valueOperations.get(eq("blacklist:" + TOKEN))).thenReturn(null);

        boolean isBlacklisted = jwtBlacklistService.isBlacklisted(TOKEN);

        assertFalse(isBlacklisted, "Token 不應該在黑名單中");

        // ✅ **確保 `get()` 方法有被呼叫**
        verify(valueOperations, times(1)).get(eq("blacklist:" + TOKEN));
    }

    @Test
    void testRemoveFromBlacklist() {
        // ✅ 使用 `doAnswer()` 來 Mock `delete()` 方法
        doAnswer(invocation -> {
            System.out.println("Mock: Token 被移除：" + invocation.getArgument(0));
            return null;
        }).when(redisTemplate).delete(anyString());

        jwtBlacklistService.removeFromBlacklist(TOKEN);

        // ✅ **確保 `delete()` 方法有被正確呼叫**
        verify(redisTemplate, times(1)).delete(eq("blacklist:" + TOKEN));
    }
}
