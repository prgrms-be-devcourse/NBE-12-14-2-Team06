package com.back.nbe12142team06.domain.ride.dto;

import jakarta.validation.constraints.NotBlank;

public record RideUpdateRequest(
        @NotBlank(message = "이동 수단을 선택해주세요.")
        String rideSelect) {
}
