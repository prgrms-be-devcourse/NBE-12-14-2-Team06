package com.back.nbe12142team06.global.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBean {

    @Bean
    // 비밀번호 암호화 메서드
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
