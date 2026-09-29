package com.back.nbe12142team06.domain.user.dto.user;

import com.back.nbe12142team06.domain.user.entity.User;
import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        String username,
        String email,
        String name,
        Role role,
        Gender gender,
        LocalDate birthDate,
        String phoneNum,
        String region,
        LocalDateTime createdAt
) {
    public UserResponse(User user) {
        this(user.getUsername(), user.getEmail(), user.getName(),
                user.getRole(), user.getGender(), user.getBirthDate(),
                user.getPhoneNum(), user.getRegion(), user.getCreatedAt());
    }
}
