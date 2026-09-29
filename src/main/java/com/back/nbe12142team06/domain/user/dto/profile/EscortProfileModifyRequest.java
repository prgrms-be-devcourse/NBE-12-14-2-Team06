package com.back.nbe12142team06.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscortProfileModifyRequest(
        @NotBlank(message = "자기소개는 필수 항목입니다.")
        @Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
        String intro,

        @NotBlank(message = "은행 이름은 필수 항목입니다.")
        @Size(max = 20, message = "은행 이름은 20자 이하여야 합니다.")
        String bankName,

        @NotBlank(message = "예금주 명은 필수 항목입니다.")
        @Size(max = 50, message = "예금주 명은 50자 이하여야 합니다.")
        String accountHolder,

        @NotBlank(message = "계좌 번호는 필수 항목입니다.")
        @Size(max = 30, message = "계좌 번호는 30자 이하여야 합니다.")
        String accountNumber
) {
}
