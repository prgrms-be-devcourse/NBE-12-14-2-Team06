package com.back.nbe12142team06.domain.user.dto.login.common;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "아이디는 필수 항목입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password) {
}
