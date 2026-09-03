package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "配置键不能为空")
    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    @Column(name = "config_value", nullable = false)
    private String configValue;

    private String description;
}