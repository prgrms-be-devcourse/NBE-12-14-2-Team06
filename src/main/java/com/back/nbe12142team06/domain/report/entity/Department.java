package com.back.nbe12142team06.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 진료 과목. 화면(ReportWritePage)의 드롭다운 항목과 1:1 대응한다.
@Getter
@RequiredArgsConstructor
public enum Department {

    INTERNAL_MEDICINE("내과"),
    SURGERY("외과"),
    ORTHOPEDICS("정형외과"),
    RADIOLOGY("영상의학과"),
    DENTISTRY("치과"),
    OPHTHALMOLOGY("안과"),
    DERMATOLOGY("피부과"),
    ETC("기타");

    private final String description;  // 화면에 표시할 한글 과목명
}