package com.back.nbe12142team06.domain.education.dto;

import com.back.nbe12142team06.domain.education.service.WatchLogResult;

public record WatchProgressLogCreateResponse(
        // 서버 인정 재생 시간
        double maxWatchedSec,
        // 영상 시청 완료 여부
        boolean completed,
        // 교육 이수(인증) 여부
        boolean verified
) {
    public WatchProgressLogCreateResponse(WatchLogResult r) {
        this(r.maxWatchedSec(), r.completed(), r.verified());
    }
}