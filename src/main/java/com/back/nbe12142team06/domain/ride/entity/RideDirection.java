package com.back.nbe12142team06.domain.ride.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RideDirection {
    TO_HOSPITAL("병원 이동"),
    TO_HOME("집 이동");

    private final String description;
}
