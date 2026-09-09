package com.back.nbe12142team06.domain.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {

    OPEN("모집 중"),
    MATCHED("매칭 완료"),
    IN_PROGRESS("동행 진행 중"),
    COMPLETED("동행 완료"),
    CANCELED("취소됨"),
    EXPIRED("마감 기한 초과");

    private final String description;
}