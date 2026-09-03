package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Column(name = "real_name")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String department;

    private String role;

    private String status;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}