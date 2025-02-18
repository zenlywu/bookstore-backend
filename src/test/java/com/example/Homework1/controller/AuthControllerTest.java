package com.example.Homework1.controller;

import com.example.Homework1.dto.AuthRequest;
import com.example.Homework1.dto.AuthResponse;
import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.User;
import com.example.Homework1.entity.Role;
import com.example.Homework1.service.JwtBlacklistService;
import com.example.Homework1.service.PasswordResetService;
import com.example.Homework1.service.UserService;
import com.example.Homework1.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // ✅ 自動回滾測試資料
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtBlacklistService blacklistService;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDto testUserDto;
    private String fakeToken = "fake.jwt.token";

    /** ✅ **第一步：測試註冊** **/
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ Mock `passwordEncoder.encode()`
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("hashedPassword"); // ✅ 確保密碼加密
        testUser.setFullname("Test User");
        testUser.setPhone("0912345678");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.EMPLOYEE);

        testUserDto = new UserDto();
        testUserDto.setUsername("testUser");
        testUserDto.setFullname("Test User");
        testUserDto.setPhone("0912345678");
        testUserDto.setEmail("test@example.com");
        testUserDto.setRole(Role.EMPLOYEE);

        // ✅ Mock `userService.findByUsername()` **確保 `findByUsername()` 正確回傳**
        when(userService.findByUsername("testUser")).thenReturn(testUser);

        // ✅ Mock `passwordEncoder.matches()` **確保密碼驗證可以通過**
        when(passwordEncoder.matches(eq("testPassword"), eq("hashedPassword"))).thenReturn(true);

        // ✅ Mock `jwtUtil.generateToken()`
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(fakeToken);

        // ✅ 修正 `userService.saveUser()` **允許無回傳值的 Mock**
        when(userService.saveUser(any(User.class), eq("EMPLOYEE"))).thenReturn(testUserDto);

    }

    /** ✅ **第二步：測試登入** **/
    @Test
    void testLogin_Success() throws Exception {
        String loginJson = """
        {
            "username": "testUser",
            "password": "testPassword"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.message").value("登入成功！"));

        // ✅ 確保 Mock 方法正確執行
        verify(userService, times(1)).findByUsername("testUser");
        verify(passwordEncoder, times(1)).matches("testPassword", "hashedPassword");
        verify(jwtUtil, times(1)).generateToken("testUser", "EMPLOYEE");
    }
}

