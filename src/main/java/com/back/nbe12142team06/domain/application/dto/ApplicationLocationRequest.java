package com.back.nbe12142team06.domain.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ApplicationLocationRequest(
        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal lat,
        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal lng,
        @PositiveOrZero
        BigDecimal accuracy   // 선택 값(없어도 됨)
) {
}