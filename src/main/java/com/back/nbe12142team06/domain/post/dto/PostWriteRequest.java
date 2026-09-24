package com.back.nbe12142team06.domain.post.dto;

import com.back.nbe12142team06.domain.ride.entity.RideSelect;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//공고 등록용
public record PostWriteRequest(

        @NotBlank(message = "제목은 필수 항목입니다.")
        String title,
        @NotBlank(message = "내용은 필수 항목입니다.")
        String content,
        @NotBlank(message = "지역은 필수 항목입니다.")
        String region,
        @NotBlank(message = "병원명은 필수 항목입니다.")
        String hospitalName,
        @NotBlank(message = "병원 주소는 필수 항목입니다.")
        String hospitalAddress,
        @NotNull(message = "병원 위도는 필수 항목입니다.")
        BigDecimal hospitalLat,
        @NotNull(message = "병원 경도는 필수 항목입니다.")
        BigDecimal hospitalLng,
        @NotBlank(message = "픽업 주소는 필수 항목입니다.")
        String pickupAddress,
        @NotNull(message = "픽업 위도는 필수 항목입니다.")
        BigDecimal pickupLat,
        @NotNull(message = "픽업 경도는 필수 항목입니다.")
        BigDecimal pickupLng,
        @Min(value = 1, message = "시급은 1원 이상이어야 합니다.")
        int hourlyPay,
        @NotNull(message = "모집 시작 시간은 필수 항목입니다.")
        LocalDateTime recruitStartAt,
        @NotNull(message = "모집 마감 시간은 필수 항목입니다.")
        LocalDateTime recruitEndAt,
        @NotNull(message = "동행 시작 시간은 필수 항목입니다.")
        LocalDateTime escortStartAt,
        @NotNull(message = "동행 종료 시간은 필수 항목입니다.")
        LocalDateTime escortEndAt,
        @NotNull(message = "집 -> 병원 이동 수단은 필수 항목입니다.")
        RideSelect rideSelectToHospital,
        @NotNull(message = "병원 -> 집 이동 수단은 필수 항목입니다.")
        RideSelect rideSelectToHome,
        String patientNote,       // nullable - 선택 항목
        boolean reportRequired    // 기본값 true
) {

}
