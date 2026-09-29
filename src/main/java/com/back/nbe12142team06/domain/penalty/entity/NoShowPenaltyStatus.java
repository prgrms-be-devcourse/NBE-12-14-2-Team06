package com.back.nbe12142team06.domain.penalty.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoShowPenaltyStatus {
    PENDING("적용전"),
    APPLIED("완료");

    private final String description;
}
