package com.example.Homework1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    private String username;
    private String newPassword;
}
