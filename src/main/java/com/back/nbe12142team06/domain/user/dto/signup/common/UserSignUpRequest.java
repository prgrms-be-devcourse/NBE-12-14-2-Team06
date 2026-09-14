package com.back.nbe12142team06.domain.user.dto.signup.common;

import com.back.nbe12142team06.domain.user.enums.Gender;
import com.back.nbe12142team06.domain.user.enums.Role;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserSignUpRequest(
        @NotBlank(message = "아이디는 필수 항목입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password,

        @NotBlank(message = "이메일은 필수 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")  // 이메일 형식 검증
        String email,

        @NotBlank(message = "이름은 필수 항목입니다.")
        String name,

        @NotNull(message = "동행인, 의뢰인 중 역할을 골라주세요.")
        Role role,

        @NotNull(message = "성별은 필수 항목입니다.")
        Gender gender,

        @NotNull(message = "생년월일은 필수 항목입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @NotBlank(message = "전화번호는 필수 항목입니다.")
        String phoneNum,

        @NotBlank(message = "지역은 필수 항목입니다.")
        @Size(max= 50)
        String region
        ) {
}
