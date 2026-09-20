package com.back.nbe12142team06.domain.user.dto.user;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Locale;

public record AdminUserProfileUpdateRequest(
        @NotBlank(message = "이메일은 필수 항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")  // 이메일 형식 검증
        String email,

        @NotBlank(message = "이름은 필수 항목입니다.")
        String name,

        @NotNull(message = "생년월일은 필수 항목입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @NotBlank(message = "전화번호는 필수 항목입니다.")
        String phoneNum,

        @NotBlank(message = "지역은 필수 항목입니다.")
        @Size(max= 50)
        String region
) {
    public AdminUserProfileUpdateRequest {
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
