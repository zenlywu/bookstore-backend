package com.example.Homework1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // 可以用User.builder()來創建對象
public class User {

    @Id // 設定為主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length=50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "fullname",length = 100)
    private String fullname;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING) //讓role儲存為String (ADMIN or EMPLOYEE)
    @Column(nullable = false)
    private Role role;

}