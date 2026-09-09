package com.back.nbe12142team06.domain.ride.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RideStatus {
    PROCESSING("이동수단 선택 중"),
    ACCEPTED("이동수단 선택 완료"),
    ARRIVING("택시 도착"),
    IN_PROGRESS("택시 이동 중"),
    COMPLETED("이동 완료"),
    DRIVER_CANCELED("운전자 취소"),
    RIDER_CANCELED("탑승자 취소"),
    NO_DRIVERS_AVAILABLE("운전자 부족");

    private final String description;
}
