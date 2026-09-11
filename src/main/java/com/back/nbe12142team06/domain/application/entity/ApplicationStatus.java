package com.back.nbe12142team06.domain.application.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {

    PENDING("지원 대기"),
    PAYMENT_PENDING("결제 대기"),
    ACCEPTED("지원 승인"),
    REJECTED("지원 거절"),
    CANCELED("지원 취소"),
    NO_SHOW("노쇼");

    private final String description;
}
