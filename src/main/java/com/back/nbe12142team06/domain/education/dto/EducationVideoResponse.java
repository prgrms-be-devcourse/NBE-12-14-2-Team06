package com.back.nbe12142team06.domain.education.dto;

import com.back.nbe12142team06.domain.education.service.VideoProgress;

public record EducationVideoResponse(
        Long id,
        String title,
        String url,
        int durationSec,
        boolean required,
        // 서버가 인정한 재생 위치 (이어보기 위치)
        double maxWatchedSec,
        // 해당 영상 시청 완료 여부
        boolean completed
) {
    public EducationVideoResponse(VideoProgress p) {
        this(
                p.video().getId(),
                p.video().getTitle(),
                p.video().getUrl(),
                p.video().getDurationSec(),
                p.video().isRequired(),
                p.maxWatchedSec(),
                p.completed()
        );
    }
}