package com.back.nbe12142team06.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 진료 보고서 작성용
public record ReportWriteRequest(

        @NotBlank(message = "제목은 필수 항목입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "진료 내용은 필수 항목입니다.")
        String originContent
) {
}