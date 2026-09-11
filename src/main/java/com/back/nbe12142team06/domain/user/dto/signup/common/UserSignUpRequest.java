package com.back.nbe12142team06.domain.user.dto.signup.common;

import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
// TODO: 제한 걸기
public record UserSignUpRequest(
        @NotBlank(message = "ID는 필수 항목입니다.")
        String username,
        String password,
        String email,
        String name,
        Role role,
        Gender gender,
        LocalDate birthDate,
        String phoneNum,
        String region
        ) {
}
