package com.example.Homework1.dto;

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
}
