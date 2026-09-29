package com.back.nbe12142team06.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientProfileRequest(
        @NotBlank(message = "보호자 이름은 필수 항목입니다.")
        @Size(max = 50, message = "보호자 이름은 50자 이하여야 합니다.")
        String emergencyContactName,

        @NotBlank(message = "보호자 전화번호는 필수 항목입니다.")
        @Size(max = 20, message = "보호자 전화번호는 20자 이하여야 합니다.")
        String emergencyContactPhone,

        @Size(max = 500, message = "특이사항은 500자 이하여야 합니다.")
        String careNote
) {
}
