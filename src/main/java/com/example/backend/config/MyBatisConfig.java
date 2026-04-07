package com.example.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.backend.repository.mybatis")
public class MyBatisConfig {
}
