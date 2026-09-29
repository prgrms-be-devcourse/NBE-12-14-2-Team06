package com.back.nbe12142team06.domain.education.dto;

import com.back.nbe12142team06.domain.education.entity.EducationProgress;

public record WatchProgressLogCreateResponse(
        // 서버가 인정한 재생 위치
        double maxWatchedSec,
        // 해당 영상 시청 완료 여부
        boolean completed,
        // 교육 이수(인증) 여부
        boolean verified
) {
    public WatchProgressLogCreateResponse(EducationProgress progress) {
        this(
                progress.getMaxWatchedSec(),
                progress.isCompleted(),
                progress.getEscortProfile().getVerified()
        );
    }
}