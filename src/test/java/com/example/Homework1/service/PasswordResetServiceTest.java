package com.example.Homework1.service;

import com.example.Homework1.entity.Role;
import com.example.Homework1.entity.User;
import com.example.Homework1.dao.UserRepository;
import com.example.Homework1.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UserServiceImpl userService;  // 🔥 Mock `UserServiceImpl`，用來查找用戶

    @Mock
    private UserRepository userRepository;  // 🔥 Mock `UserRepository
    
    @Mock
    private PasswordEncoder passwordEncoder; // 🔥 Mock `PasswordEncoder`

    @InjectMocks
    private PasswordResetService passwordResetService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_NEW_PASSWORD = "newSecurePassword";
    private static final String TEST_TOKEN = "reset-token-123";

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword("hashedPassword"); 
        testUser.setFullname("Test User");
        testUser.setEmail(TEST_EMAIL);
        testUser.setPhone("0912345678");
        testUser.setRole(Role.EMPLOYEE);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(testUser);

        // ✅ Mock `passwordEncoder.encode()`
        when(passwordEncoder.encode(TEST_NEW_PASSWORD)).thenReturn("hashedNewPassword");
        
        // ✅ **Mock `redisTemplate.opsForValue()`**
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }



    @Test
    void testGenerateResetToken() {
        // ✅ Mock `valueOperations.set()`
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        String token = passwordResetService.generateResetToken(TEST_USERNAME);

        assertNotNull(token, "Token 不應為 null");
        verify(valueOperations, times(1)).set(eq("reset:" + token), eq(TEST_USERNAME), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void testValidateResetToken() {
        // ✅ Mock `valueOperations.get()`
        when(valueOperations.get("reset:" + TEST_TOKEN)).thenReturn(TEST_USERNAME);

        String username = passwordResetService.validateResetToken(TEST_TOKEN);

        assertEquals(TEST_USERNAME, username, "應該返回對應的用戶名");
        verify(valueOperations, times(1)).get(eq("reset:" + TEST_TOKEN));
    }



    // ✅ **測試 Token 無效時應返回 null**
    @Test
    void testValidateResetToken_Invalid() {
        when(valueOperations.get("reset:" + TEST_TOKEN)).thenReturn(null);

        String username = passwordResetService.validateResetToken(TEST_TOKEN);

        assertNull(username, "無效的 Token 應該返回 null");
    }

    //**測試成功重置密碼**
    @Test
    void testResetPassword_Success() {
        when(valueOperations.get("reset:" + TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(testUser);

        // ✅ Mock `saveresUser()`，確保 `passwordEncoder.encode()` 在內部被呼叫
        doAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setPassword(passwordEncoder.encode(savedUser.getPassword())); // **模擬加密**
            return savedUser;
        }).when(userService).saveresUser(any(User.class));

        boolean result = passwordResetService.resetPassword(TEST_TOKEN, TEST_NEW_PASSWORD);

        assertTrue(result, "密碼重置應該成功");

        // ✅ **檢查 `passwordEncoder.encode()` 是否真的被呼叫**
        verify(passwordEncoder, times(1)).encode(TEST_NEW_PASSWORD);

        // ✅ **確保 `testUser.getPassword()` 被更新**
        assertEquals("hashedNewPassword", testUser.getPassword(), "密碼應該被加密");

        verify(userService, times(1)).saveresUser(testUser);
        verify(redisTemplate, times(1)).delete(eq("reset:" + TEST_TOKEN));
    }
    

    //**測試 Token 無效時應該失敗**
    @Test
    void testResetPassword_Fail_InvalidToken() {
        when(valueOperations.get("reset:" + TEST_TOKEN)).thenReturn(null);

        boolean result = passwordResetService.resetPassword(TEST_TOKEN, TEST_NEW_PASSWORD);

        assertFalse(result, "無效的 Token 應該無法重置密碼");
        verify(userService, never()).saveUser(any(), anyString());
    }

    //**測試用戶不存在時應該失敗**
    @Test
    void testResetPassword_Fail_UserNotFound() {
        when(valueOperations.get("reset:" + TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(null);

        boolean result = passwordResetService.resetPassword(TEST_TOKEN, TEST_NEW_PASSWORD);

        assertFalse(result, "用戶不存在時應該無法重置密碼");
        verify(userService, never()).saveUser(any(), anyString());
    }
}
