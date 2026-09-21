package com.back.nbe12142team06.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record ClientProfileRequest(
        @NotBlank(message = "보호자 이름은 필수 항목입니다.")
        String emergencyContactName,

        @NotBlank(message = "보호자 전화번호는 필수 항목입니다.")
        String emergencyContactPhone,

        String careNote
) {
}
