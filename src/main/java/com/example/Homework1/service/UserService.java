package com.example.Homework1.service;

 import com.example.Homework1.dto.UserDto;
 import com.example.Homework1.entity.User;

 import java.util.List;
 import java.util.Optional;

 public interface UserService {
 
    List<UserDto> getAllUsers(); //查詢所有使用者的資料
    Optional<UserDto> getUserById(Long id); //根據 id 查詢單一使用者 ，
    UserDto saveUser(User user, String requestRole);//儲存或更新使用者資訊
    UserDto updateUser(Long id,UserDto userDto,String requestRole);//根據id更改使用者資料
    User saveresUser(User user);
    User findByUsername(String username);
    void deleteUser(Long id,String requestRole);//刪除特定 id 的使用者
    void resetPassword(String username, String newPassword);
    List<UserDto> getUsersByRole(String role);


 }