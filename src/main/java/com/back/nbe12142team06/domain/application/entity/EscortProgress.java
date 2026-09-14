package com.back.nbe12142team06.domain.application.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EscortProgress {

    NOT_STARTED("동행 시작 전"),
    DEPARTED("출발"),
    TO_HOSPITAL("병원 이동 중"),
    AT_HOSPITAL("병원 도착"),
    TO_HOME("귀가 중"),
    ARRIVED_HOME("귀가 완료");

    private final String description;
}
