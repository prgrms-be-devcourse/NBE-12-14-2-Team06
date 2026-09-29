package com.back.nbe12142team06.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EscortGrade {
    SEED("씨앗"),
    SPROUT("새싹"),
    FLOWER("꽃"),
    EGGPLANT("가지");

    private final String description;
}