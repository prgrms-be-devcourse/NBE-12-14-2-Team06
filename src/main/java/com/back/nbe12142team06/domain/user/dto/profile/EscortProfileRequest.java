package com.back.nbe12142team06.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record EscortProfileRequest(
        String intro,
        @NotBlank(message = "은행 이름을 입력해주세요.")
        String bankName,
        @NotBlank(message = "예금주 명을 입력해주세요.")
        String accountHolder,
        @NotBlank(message = "계좌 번호를 입력해주세요.")
        String accountNumber
) {
}
