package com.back.nbe12142team06.domain.report.dto;

import com.back.nbe12142team06.domain.report.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 진료 보고서 작성용
// 제목은 화면에서 받지 않고 서비스에서 자동 생성한다.
public record ReportWriteRequest(

        @NotNull(message = "진료 과목은 필수 항목입니다.")
        Department department,

        @NotBlank(message = "진료 목적은 필수 항목입니다.")
        @Size(max = 200, message = "진료 목적은 200자 이하여야 합니다.")
        String purpose,

        @NotBlank(message = "진료 내용은 필수 항목입니다.")
        String originContent,

        @Size(max = 2000, message = "특이사항은 2000자 이하여야 합니다.")
        String notes  // 선택 입력
) {
}