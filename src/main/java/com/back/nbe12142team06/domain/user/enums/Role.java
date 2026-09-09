package com.back.nbe12142team06.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"),
    CLIENT("의뢰인"),
    ESCORT("동행인");

    private final String description;
}
