package com.example.Homework1.controller;

import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.User;
import com.example.Homework1.entity.Role;
import com.example.Homework1.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Tag(name = "User API", description = "員工管理 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder; 
    @Operation(summary = "取得所有使用者")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestRole = authentication.getAuthorities().iterator().next().getAuthority(); // ✅ 取得當前使用者角色

        List<UserDto> users;
        if (requestRole.equals("HR_MANAGER")) {
            // ✅ HR_MANAGER 只能獲取 EMPLOYEE
            users = userService.getUsersByRole("EMPLOYEE");
        } else {
            // ✅ ADMIN 獲取所有使用者
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "根據 ID 取得使用者")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestRole = authentication.getAuthorities().iterator().next().getAuthority();

        Optional<UserDto> user = userService.getUserById(id);

        //HR_MANAGER 只能查看 EMPLOYEE
        if (requestRole.equals("HR_MANAGER") && user.isPresent() && !user.get().getRole().equals(Role.EMPLOYEE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "創建新使用者")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestRole = authentication.getAuthorities().iterator().next().getAuthority(); // ✅ 取得當前使用者角色
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getRole() == null) {
                user.setRole(Role.EMPLOYEE);
            }
            UserDto savedUser = userService.saveUser(user,requestRole);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
        }
    }
    @Operation(summary = "更改使用者資料")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestRole = authentication.getAuthorities().iterator().next().getAuthority(); // ✅ 取得當前使用者角色
        
        try {
            UserDto updatedUser = userService.updateUser(id, userDto, requestRole);
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user: " + e.getMessage());
        }
    }


    @Operation(summary = "刪除使用者")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestRole = authentication.getAuthorities().iterator().next().getAuthority(); // ✅ 取得當前使用者角色

        try {
            userService.deleteUser(id, requestRole);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }
}
