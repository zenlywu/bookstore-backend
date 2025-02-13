package com.example.Homework1.dto;

import com.example.Homework1.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullname;
    private String phone;
    private String email;
    private Role role;
}
