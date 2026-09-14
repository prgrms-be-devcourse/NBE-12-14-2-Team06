package com.back.nbe12142team06.domain.settlement.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("정산 전"),
    COMPLETED("정산 완료"),
    FAILED("정산 실패");

    private final String description;
}
