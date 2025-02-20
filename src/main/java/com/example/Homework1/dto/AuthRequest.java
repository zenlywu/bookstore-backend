package com.example.Homework1.dto;

import com.example.Homework1.entity.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String username;
    private String password;
    private String fullname;
    private String phone;
    private String email;
    private Role role;
}
