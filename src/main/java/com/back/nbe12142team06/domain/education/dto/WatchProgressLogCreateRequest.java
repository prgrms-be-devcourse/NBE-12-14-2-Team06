package com.back.nbe12142team06.domain.education.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WatchProgressLogCreateRequest(
        @NotNull(message = "재생 위치는 필수입니다.")
        @PositiveOrZero(message = "재생 위치는 0 이상이어야 합니다.")
        Double positionSec
) {
}
