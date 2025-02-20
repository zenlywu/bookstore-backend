package com.example.Homework1.service;

import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.Role;
import com.example.Homework1.entity.User;
import com.example.Homework1.serviceImpl.UserServiceImpl;
import com.example.Homework1.dao.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserUlitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // 🔥 Mock `PasswordEncoder`

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ Mock `PasswordEncoder`
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // ✅ 設定測試用戶
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("hashedPassword");  // 假設密碼已經加密
        testUser.setFullname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("0912345678");
        testUser.setRole(Role.EMPLOYEE);
    }


    @Test
    void testSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto createdUser = userService.saveUser(testUser, "ADMIN");

        assertNotNull(createdUser);
        assertEquals("testUser", createdUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDto> foundUser = userService.getUserById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals(1L, foundUser.get().getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDto> users = userService.getAllUsers();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser() {
        UserDto userDto = new UserDto();
        userDto.setFullname("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto updatedUser = userService.updateUser(1L, userDto, "ADMIN");

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getFullname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L, "ADMIN");

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetUsersByRole() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDto> employees = userService.getUsersByRole("EMPLOYEE");

        assertFalse(employees.isEmpty());
        assertEquals(1, employees.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testSaveUser_DuplicateUsername() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(testUser, "ADMIN");
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(99L, "ADMIN");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).deleteById(99L);
    }

    @Test
    void testResetPassword() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.resetPassword("testUser", "newPassword");

        verify(userRepository, times(1)).save(any(User.class));
    }

}
