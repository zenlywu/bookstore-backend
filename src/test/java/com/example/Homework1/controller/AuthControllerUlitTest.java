package com.example.Homework1.controller;

import com.example.Homework1.dto.AuthRequest;
import com.example.Homework1.dto.AuthResponse;
import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.Role;
import com.example.Homework1.entity.User;
import com.example.Homework1.service.JwtBlacklistService;
import com.example.Homework1.service.PasswordResetService;
import com.example.Homework1.service.UserService;
import com.example.Homework1.utils.JwtUtil;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

class AuthControllerUlitTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtBlacklistService blacklistService;
    
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testRegister_Success() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("newUser");
        request.setPassword("plainPassword");
        request.setFullname("New User");
        request.setPhone("0911223344");
        request.setEmail("newuser@example.com");
        request.setRole(Role.EMPLOYEE);

        // 模擬密碼加密行為
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        // 模擬保存用戶後回傳的 DTO
        UserDto savedUserDto = new UserDto();
        savedUserDto.setUsername("newUser");
        savedUserDto.setFullname("New User");
        savedUserDto.setPhone("0911223344");
        savedUserDto.setEmail("newuser@example.com");
        savedUserDto.setRole(Role.EMPLOYEE);

        when(userService.saveUser(any(User.class), eq("EMPLOYEE"))).thenReturn(savedUserDto);

        // Act
        ResponseEntity<String> response = authController.register(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("註冊成功！", response.getBody());
        verify(userService, times(1)).saveUser(any(User.class), eq("EMPLOYEE"));
    }
    /** 測試登入成功 */
    @SuppressWarnings("deprecation")
    @Test
    void testLogin_Success() {
        // Arrange
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("testPassword");
        // 註冊時其他欄位非必要，這裡只需 username 與 password

        // 準備一個 User 物件，模擬資料庫中存在此使用者
        User existingUser = new User();
        existingUser.setUsername("testUser");
        existingUser.setPassword("hashedPassword");
        existingUser.setRole(Role.EMPLOYEE);

        when(userService.findByUsername("testUser")).thenReturn(existingUser);
        // 模擬密碼驗證通過
        when(passwordEncoder.matches("testPassword", "hashedPassword")).thenReturn(true);
        // 模擬產生 JWT token
        when(jwtUtil.generateToken("testUser", "EMPLOYEE")).thenReturn("fakeToken");

        // Act
        ResponseEntity<AuthResponse> loginResponse = authController.login(loginRequest);

        // Assert
        assertEquals(200, loginResponse.getStatusCodeValue());
        AuthResponse authResponse = loginResponse.getBody();
        assertEquals("fakeToken", authResponse.getToken());
        assertEquals("登入成功！", authResponse.getMessage());
    }
    /** 測試登出成功，使 Token 失效 */
    @SuppressWarnings("deprecation")
    @Test
    void testLogout_Success() {
        String authHeader = "Bearer fakeToken";
        // 模擬 jwtUtil.extractExpiration 回傳一個未來的日期
        Date futureDate = new Date(System.currentTimeMillis() + 1000L);
        when(jwtUtil.extractExpiration("fakeToken")).thenReturn(futureDate);

        ResponseEntity<String> logoutResponse = authController.logout(authHeader);

        // verify jwtBlacklistService 呼叫 addToBlacklist，因為 expirationMillis 會因時間變動，所以以 anyLong() 來驗證
        verify(blacklistService, times(1)).addToBlacklist(eq("fakeToken"), anyLong());
        assertEquals(200, logoutResponse.getStatusCodeValue());
        assertEquals("登出成功，Token 已失效", logoutResponse.getBody());
    }

    /** 測試登出時缺少或無效 Token */
    @SuppressWarnings("deprecation")
    @Test
    void testLogout_MissingOrInvalidToken() {
        // 情境1：AuthHeader為 null
        ResponseEntity<String> responseNull = authController.logout(null);
        assertEquals(400, responseNull.getStatusCodeValue());
        assertEquals("缺少有效的 Token", responseNull.getBody());

        // 情境2：AuthHeader沒有 "Bearer " 前綴
        ResponseEntity<String> responseInvalid = authController.logout("InvalidToken");
        assertEquals(400, responseInvalid.getStatusCodeValue());
        assertEquals("缺少有效的 Token", responseInvalid.getBody());
    }

    /** 測試 JWT 重新簽發成功 */
    @SuppressWarnings("deprecation")
    @Test
    void testRefreshToken_Success() {
        String authHeader = "Bearer validToken";
        // 模擬 extractAllClaims 回傳 Claims
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testUser");
        when(claims.get("role", String.class)).thenReturn("EMPLOYEE");
        
        when(jwtUtil.extractAllClaims("validToken")).thenReturn(claims);
        // 模擬 Token 未過期
        when(jwtUtil.isTokenExpired("validToken")).thenReturn(false);
        // 模擬重新簽發新 Token
        when(jwtUtil.generateToken("testUser", "EMPLOYEE")).thenReturn("newToken");

        ResponseEntity<AuthResponse> response = authController.refreshToken(authHeader);
        
        assertEquals(200, response.getStatusCodeValue());
        AuthResponse responseBody = response.getBody();
        assertEquals("newToken", responseBody.getToken());
        assertEquals("Token 重新簽發成功", responseBody.getMessage());
    }

    /** 測試：請求忘記密碼成功 */
    @SuppressWarnings("deprecation")
    @Test
    void testForgotPassword_Success() {
        String username = "testUser";
        // 模擬產生 Reset Token
        when(passwordResetService.generateResetToken(username)).thenReturn("resetToken");

        ResponseEntity<String> response = authController.forgotPassword(username);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("請使用此 Token 來重設密碼：resetToken", response.getBody());
    }

    /** 測試：重設密碼成功 */
    @SuppressWarnings("deprecation")
    @Test
    void testResetPassword_Success() {
        String token = "validResetToken";
        String newPassword = "newPassword";
        
        // 模擬密碼重置成功（resetPassword 回傳 true）
        when(passwordResetService.resetPassword(token, newPassword)).thenReturn(true);

        ResponseEntity<String> response = authController.resetPassword(token, newPassword);
        
        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("密碼重置成功！", response.getBody());
    }

    /** 測試：重設密碼失敗，回傳錯誤訊息 */
    @SuppressWarnings("deprecation")
    @Test
    void testResetPassword_Failure() {
        String token = "invalidResetToken";
        String newPassword = "newPassword";
        
        // 模擬密碼重置失敗（resetPassword 回傳 false）
        when(passwordResetService.resetPassword(token, newPassword)).thenReturn(false);

        ResponseEntity<String> response = authController.resetPassword(token, newPassword);
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("無效的 Token 或使用者", response.getBody());
    }

}