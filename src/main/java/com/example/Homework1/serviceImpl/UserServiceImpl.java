package com.example.Homework1.serviceImpl;

import com.example.Homework1.service.UserService;
import com.example.Homework1.dao.UserRepository;
import com.example.Homework1.dto.UserDto;
import com.example.Homework1.entity.User;
import com.example.Homework1.entity.Role;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 確保有這個

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto) 
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public UserDto saveUser(User user, String requestRole) {
        // ✅ `HR_MANAGER` 只能新增 `EMPLOYEE`
        
        if (requestRole.equals("HR_MANAGER") && !user.getRole().equals(Role.EMPLOYEE)) {
            throw new SecurityException("HR_MANAGER 只能創建 EMPLOYEE，不能創建 ADMIN 或 HR_MANAGER");
        }
    
        // 🔥 **修正 `findByUsername()` 判斷，確保不會 `null`**
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
    
        // ✅ 確保密碼加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    


    @Override
    public UserDto updateUser(Long id, UserDto userDto,String requestRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")); // 確保使用者存在

        // ✅ `HR_MANAGER` 不能修改 `ADMIN` 或 `HR_MANAGER`
        if (requestRole.equals("HR_MANAGER") && (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.HR_MANAGER))) {
            throw new SecurityException("HR_MANAGER 不能修改 ADMIN 或 HR_MANAGER");
        }
        // 更新欄位
        user.setUsername(userDto.getUsername());
        user.setFullname(userDto.getFullname() != null ? userDto.getFullname() : user.getFullname());
        user.setPhone(userDto.getPhone() != null ? userDto.getPhone() : user.getPhone());
        user.setEmail(userDto.getEmail() != null ? userDto.getEmail() : user.getEmail());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : user.getRole());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public User saveresUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ 加密密碼
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteUser(Long id,String requestRole) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // ✅ `HR_MANAGER` 不能刪除 `ADMIN` 或 `HR_MANAGER`
        if (requestRole.equals("HR_MANAGER") && (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.HR_MANAGER))) {
            throw new SecurityException("HR_MANAGER 不能刪除 ADMIN 或 HR_MANAGER");
        }
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("無法轉換 User，因為它是 null");
        }
        return UserDto.builder()
                .id(user.getId() != null ? user.getId() : 0L) // 避免 NullPointerException
                .username(user.getUsername() != null ? user.getUsername() : "未知")
                .fullname(user.getFullname() != null ? user.getFullname() : "N/A")  
                .phone(user.getPhone() != null ? user.getPhone() : "N/A")
                .email(user.getEmail() != null ? user.getEmail() : "N/A")
                .role(user.getRole() != null ? user.getRole() : Role.EMPLOYEE)  // 預設為 EMPLOYEE
                .build();
    }
    

    @Override
    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("找不到該用戶");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // ✅ 加密新密碼
        userRepository.save(user);
    }

}
